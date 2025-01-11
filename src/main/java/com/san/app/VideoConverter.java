package com.san.app;

import java.io.File;
import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.san.to.MediaConverterConfig;
import com.san.to.MediaMetaDataTO;
import com.san.to.ProcessStatusTO;
import com.san.util.CommonUtil;
import com.san.util.ProcessHelperUtil;

public class VideoConverter {

	static Logger logger = LoggerFactory.getLogger(VideoConverter.class);

	static String sourcePath = "C:\\Users\\anila\\Desktop\\Test";
	static String outPath = "C:\\Users\\anila\\Desktop\\TestOut";
	static String tempOutPath = "C:\\Users\\anila\\Desktop\\TestOutTemp";
	static int processTimeout = 5 * 60; // In Seconds

	static String ffprobe = "D:\\DBackup\\Softwares\\ffmpeg-20181213-e5a0013-win64-static\\ffmpeg-20181213-e5a0013-win64-static\\bin\\ffprobe.exe";
	static String ffmpeg = "D:\\DBackup\\Softwares\\ffmpeg-20181213-e5a0013-win64-static\\ffmpeg-20181213-e5a0013-win64-static\\bin\\ffmpeg.exe";
	static String vlc = "\"C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe\"";

	private static String fetchUniqueStringForFile(File file) {
		String uniquePath = file.getAbsolutePath();
		uniquePath = uniquePath.replaceAll(" ", "_");
		uniquePath = uniquePath.replaceAll(":", "_");
		uniquePath = uniquePath.replaceAll("/", "_");
		uniquePath = uniquePath.replaceAll("\\\\", "_");
		return uniquePath;
	}

	static String[] fetchAnalyzeCommand(File inFile) {
		String[] command = new String[] { //
				ffprobe, //
				"-v", //
				"error", //
				"-hide_banner", //
				"-show_format", //
				"-show_streams", //
				"-print_format", //
				"json", //
				"\"" + inFile.getAbsolutePath() + "\"" //
		};
		// String cmd = "ffprobe -v error -hide_banner -show_format -show_streams -print_format json C:\\Users\\anila\\Downloads\\test.MP4";
		return command;
	}

	static String[] fetchConverterVLCCommand(File inFile, File outFile, int height, int width) {
		String[] command = new String[] { //
				vlc, //
				"-I", //
				"dummy", //
				"\"file:///" + (inFile.getAbsolutePath().replaceAll("\\\\", "/")) + "\"", //
				":sout=#transcode{vcodec=h264,vb=2000,venc=x264{profile=baseline},width=" + width + ",height=" + height + ",acodec=mp3,ab=192,channels=2,samplerate=44100,scodec=none}:std{access=file{no-overwrite},mux=mp4,dst='\"" + outFile.getAbsolutePath() + "\"'}", //
				"vlc://quit"//
		};
		return command;
	}

	static String[] fetchConverterFFMPEGCommand(File inFile, File outFile, int height, int width) {
		String[] command = new String[] { //
				ffmpeg, //
				"-i", //
				"\"" + (inFile.getAbsolutePath().replaceAll("\\\\", "/")) + "\"", //
				"-vcodec", //
				"libx264", //
				"-acodec", //
				"mp3", //
				"-crf", //
				"28", //
				"\"" + outFile.getAbsolutePath() + "\"" //
		};
		return command;
	}

	static void processVideoFile(File inFile, File outFile) {
		ProcessStatusTO commandOutput = ProcessHelperUtil.executeCommandWithOutput(String.join(" ", fetchAnalyzeCommand(inFile)), processTimeout);
		MediaMetaDataTO mediaMetaDataTO = null;
		try {
			mediaMetaDataTO = CommonUtil.bindJSONToObject(String.join(" ", commandOutput.getStdOut()), MediaMetaDataTO.class);
			logger.debug("File [" + inFile.getAbsolutePath() + "] MetaData : " + CommonUtil.convertToJsonString(mediaMetaDataTO));
		} catch (Exception e) {
		}
		int width = 0;
		int height = 0;
		boolean videoStreamPresent = false;
		if (mediaMetaDataTO != null && mediaMetaDataTO.streams != null && mediaMetaDataTO.streams.size() > 0) {
			for (MediaMetaDataTO.Stream stream : mediaMetaDataTO.streams) {
				if (stream.codec_type != null && stream.codec_type.toLowerCase().equals("video")) {
					width = stream.width;
					height = stream.height;
					videoStreamPresent = true;
					break;
				}
			}
		} else {
			logger.debug("Unable to populate MetaData for File [" + inFile.getAbsolutePath() + "].");
		}
		if (!videoStreamPresent) {
			logger.info("No Video Stream found in file : " + inFile.getAbsolutePath() + ". Conversion not required.");
			return;
		}
		// Fetch height & width from above json Map
		if (width < height) {
			height = 1280;
			width = 720;
		} else {
			width = 1280;
			height = 720;
		}
		// String[] conversionCommand = fetchConverterVLCCommand(inFile, outFile, height, width);
		String[] conversionCommand = fetchConverterFFMPEGCommand(inFile, outFile, height, width);
		commandOutput = ProcessHelperUtil.executeCommandWithOutput(String.join(" ", conversionCommand), processTimeout);
		boolean processCompleted = false;
		int counter = 0;
		while (!processCompleted) {
			// Integer pid = ProcessHelperUtil.findAnyRunningProcess("vlc", conversionCommand[3]);
			Integer pid = ProcessHelperUtil.findAnyRunningProcess("ffmpeg", conversionCommand[2]);
			if (pid == null) {
				// Operation completed
				break;
			} else {
				// Process still in running state with PID : pid
				if ((counter % 10) == 0) {
					logger.info("Process still in running state with PID : " + pid);
				}
				try {
					Thread.sleep(5000l);
					counter++;
				} catch (InterruptedException e) {
				}
			}
		}
	}

	public static void processVideoFilesRecursively(File file, File outFolder) {
		if (file != null) {
			if (file.isDirectory()) {
				for (File child : file.listFiles()) {
					processVideoFilesRecursively(child, outFolder);
				}
			} else {
				if (file.canRead() && file.getName().toLowerCase().endsWith(".mp4")) {
					String absPath = file.getAbsolutePath();
					String uniquePath = fetchUniqueStringForFile(file);
					File tempFile = new File(tempOutPath + File.separator + uniquePath);
					// tempFile.delete();
					if (tempFile.exists()) {
						logger.info("File already processed [" + absPath + "] as temp file present on system [" + tempFile.getAbsolutePath() + "].");
					} else {
						File outFile = null;
						if (uniquePath.indexOf("Cinematic") >= 0) {
							outFile = new File(outFolder.getAbsolutePath() + File.separator + "Cinematic");
						} else {
							outFile = new File(outFolder.getAbsolutePath() + File.separator + "Traditional");
						}
						outFile.mkdirs();
						outFile = new File(outFile.getAbsolutePath() + File.separator + file.getName());
						logger.info("Processing file : " + absPath + ", outFile : " + outFile.getAbsolutePath());

						if (outFile.exists()) {
							logger.info("[Processing file : " + absPath + "] : outFile : " + outFile.getAbsolutePath() + " already present, remove it for new processing ...");
							outFile.delete();
						}
						// Process file and verify the same
						processVideoFile(file, outFile);
						boolean finalStatus = false;
						try {
							if (outFile.exists()) {
								logger.debug("Processing completed successfully for file : " + file.getAbsolutePath());
								finalStatus = tempFile.createNewFile();
							} else {
								logger.debug("Processing failed for file : " + file.getAbsolutePath());
							}
						} catch (IOException e) {
						}
						if (finalStatus) {
							logger.info("Video File : " + absPath + " processed successfully and stored at : " + outFile.getAbsolutePath());
						} else {
							logger.info("Video File : " + absPath + " processing failed.");
							try {
								outFile.delete();
							} catch (Exception e) {
							}
						}
					}
				}

			}
		}
	}

	public static void main(String args[]) {
		try {
			String jsonString = CommonUtil.fetchFileContentAsString("media-converter-config.json", true);
			if (jsonString != null && !jsonString.isEmpty()) {
				MediaConverterConfig mediaConverterConfig = CommonUtil.bindJSONToObject(jsonString, MediaConverterConfig.class);
				if (mediaConverterConfig != null) {
					if (mediaConverterConfig.getFfprobePath() != null && !mediaConverterConfig.getFfprobePath().isEmpty()) {
						ffprobe = mediaConverterConfig.getFfprobePath();
					}
					if (mediaConverterConfig.getFfmpegPath() != null && !mediaConverterConfig.getFfmpegPath().isEmpty()) {
						ffmpeg = mediaConverterConfig.getFfmpegPath();
					}
					if (mediaConverterConfig.getVlcPath() != null && !mediaConverterConfig.getVlcPath().isEmpty()) {
						vlc = mediaConverterConfig.getVlcPath();
					}
					if (mediaConverterConfig.getSourcePath() != null && !mediaConverterConfig.getSourcePath().isEmpty()) {
						sourcePath = mediaConverterConfig.getSourcePath();
					}
					if (mediaConverterConfig.getOutPath() != null && !mediaConverterConfig.getOutPath().isEmpty()) {
						outPath = mediaConverterConfig.getOutPath();
					}
					if (mediaConverterConfig.getTempOutPath() != null && !mediaConverterConfig.getTempOutPath().isEmpty()) {
						tempOutPath = mediaConverterConfig.getTempOutPath();
					}
					if (mediaConverterConfig.getProcessTimeout() > 0) {
						processTimeout = mediaConverterConfig.getProcessTimeout();
					}
				}
			}
		} catch (Exception e) {
		}
		File outPathFolder = new File(outPath);
		File tempOutPathFolder = new File(tempOutPath);
		File sourceFolder = new File(sourcePath);
		try {
			outPathFolder.mkdirs();
			tempOutPathFolder.mkdirs();
		} catch (Exception e) {
		}

		if (!sourceFolder.exists() || !sourceFolder.isDirectory()) {
			logger.info("Either SourceFolder : " + sourceFolder + " not exists or not a valid directory.");
			return;
		}
		if (!outPathFolder.exists() || !outPathFolder.isDirectory()) {
			logger.info("Either OutPath : " + outPath + " not exists or not a valid directory.");
			return;
		}
		if (!tempOutPathFolder.exists() || !tempOutPathFolder.isDirectory()) {
			logger.info("Either TempOutPath : " + tempOutPathFolder + " not exists or not a valid directory.");
			return;
		}

		for (File child : sourceFolder.listFiles()) {
			if (child.isDirectory()) {
				File outFolder = new File(outPath + File.separator + child.getName());
				processVideoFilesRecursively(child, outFolder);
			}
		}
	}

	public static void main1(String args[]) {
		Integer pid = ProcessHelperUtil.findAnyRunningProcess("vlc", "file:///C:/Users/anila/Desktop/Test/Event1/test.mp4");
		// PARROT,"C:\\Program Files (x86)\\VideoLAN\\VLC\\vlc.exe" -I dummy file:///C:/Users/anila/Desktop/Test/Event1/test.mp4
		// ":sout=#transcode{vcodec=h264,vb=2000,venc=x264{profile=baseline},width=1280,height=720,acodec=mp3,ab=192,channels=2,samplerate=44100,scodec=none}:std{access=file{no-overwrite},mux=mp4,dst='C:/Users/anila/Desktop/Test/Event1/C2968-temp123.mp4'}"
		// vlc://quit,24588
		logger.info(pid + "");
	}

}
