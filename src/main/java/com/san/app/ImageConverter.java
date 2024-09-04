package com.san.app;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.san.to.MediaConverterConfig;
import com.san.to.ProcessStatusTO;
import com.san.util.CommonUtil;
import com.san.util.ProcessHelperUtil;

public class ImageConverter {

	static Logger logger = LoggerFactory.getLogger(ImageConverter.class);

	static String sourcePath = "C:\\Users\\anila\\Desktop\\Test";
	static String outPath = "C:\\Users\\anila\\Desktop\\TestOut";
	static String tempOutPath = "C:\\Users\\anila\\Desktop\\TestOutTemp";
	static int processTimeout = 5 * 60; // In Seconds
	static List<String> imgExtensions = Arrays.asList(".jpeg", ".jpg", ".png");
	static List<String> includeFoldersWithGivenNamesOnly = Arrays.asList();
	static List<String> excludeFoldersWithGivenNames = Arrays.asList();

	static String ffmpeg = "D:\\DBackup\\Softwares\\ffmpeg-20181213-e5a0013-win64-static\\ffmpeg-20181213-e5a0013-win64-static\\bin\\ffmpeg.exe";
	static int compressionLevel = 5;

	private static String fetchUniqueStringForFile(File file) {
		String uniquePath = file.getAbsolutePath();
		uniquePath = uniquePath.replaceAll(" ", "_");
		uniquePath = uniquePath.replaceAll(":", "_");
		uniquePath = uniquePath.replaceAll("/", "_");
		uniquePath = uniquePath.replaceAll("\\\\", "_");
		return uniquePath;
	}

	static String[] fetchConverterCommand(File inFile, File outFile, int compressionLevel) {
		// compressionLevel [1-31]
		String[] command = new String[] { //
				"" + ffmpeg.replaceAll("\\\\", "/") + "", //
				"-i", //
				"\"" + (inFile.getAbsolutePath().replaceAll("\\\\", "/")) + "\"", //
				"-q:v", //
				"" + compressionLevel, "\"" + outFile.getAbsolutePath().replaceAll("\\\\", "/") + "\"" };
		return command;
	}

	static void processImageFile(File inFile, File outFile) {
		String[] conversionCommand = fetchConverterCommand(inFile, outFile, compressionLevel);
		ProcessStatusTO commandOutput = ProcessHelperUtil.executeCommandWithOutput(String.join(" ", conversionCommand), processTimeout);
		logger.debug("Command stdOut : " + commandOutput.getStdOut());
		logger.debug("Command errOut : " + commandOutput.getErrOut());
	}

	public static void processImageFilesRecursively(File file, File outFolder) {
		if (file != null) {
			if (file.isDirectory()) {
				for (File child : file.listFiles()) {
					processImageFilesRecursively(child, outFolder);
				}
			} else if (file.canRead()) {
				String uniquePath = fetchUniqueStringForFile(file);
				String includeFolder = null;
				boolean tempStatus = false;
				{
					// Check file extension
					for (String extension : imgExtensions) {
						if (file.getName().toLowerCase().endsWith(extension)) {
							tempStatus = true;
							break;
						}
					}

					// Check presence of inclusive folders
					if (tempStatus && !includeFoldersWithGivenNamesOnly.isEmpty()) {
						tempStatus = false;
						for (String iFolder : includeFoldersWithGivenNamesOnly) {
							if (uniquePath.indexOf("_" + iFolder + "_") > -1) {
								includeFolder = iFolder;
								tempStatus = true;
								break;
							}
						}
					}

					// Check presence of exclusive folders
					if (tempStatus) {
						for (String excludeFolder : excludeFoldersWithGivenNames) {
							if (uniquePath.indexOf("_" + excludeFolder + "_") > -1) {
								tempStatus = false;
								break;
							}
						}
					}
				}

				if (tempStatus) {
					String absPath = file.getAbsolutePath();
					File tempFile = new File(tempOutPath + File.separator + uniquePath);
					// tempFile.delete();
					if (tempFile.exists()) {
						logger.info("File already processed [" + absPath + "] as temp file present on system [" + tempFile.getAbsolutePath() + "].");
					} else {
						File outFile = null;
						if (includeFolder == null) {
							outFile = outFolder;
						} else {
							outFile = new File(outFolder.getAbsolutePath() + File.separator + includeFolder);
						}
						outFile.mkdirs();
						outFile = new File(outFile.getAbsolutePath() + File.separator + file.getName());
						logger.info("Processing file : " + absPath + ", outFile : " + outFile.getAbsolutePath());

						if (outFile.exists()) {
							logger.info("OutFile : " + outFile.getAbsolutePath() + " already present, remove it for new processing ...");
							outFile.delete();
						}

						// Process file and verify the same
						processImageFile(file, outFile);
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
							logger.info("Image File : " + absPath + " processed successfully and stored at : " + outFile.getAbsolutePath());
						} else {
							logger.info("Image File : " + absPath + " processing failed.");
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
					logger.info("Populating config from file : media-converter-config.json");
					
					if (mediaConverterConfig.getFfmpegPath() != null && !mediaConverterConfig.getFfmpegPath().isEmpty()) {
						ffmpeg = mediaConverterConfig.getFfmpegPath();
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
					if (mediaConverterConfig.getImgExtensions() != null && !mediaConverterConfig.getImgExtensions().isEmpty()) {
						imgExtensions = mediaConverterConfig.getImgExtensions();
					}
					if (mediaConverterConfig.getIncludeFoldersWithGivenNamesOnly() != null && !mediaConverterConfig.getIncludeFoldersWithGivenNamesOnly().isEmpty()) {
						includeFoldersWithGivenNamesOnly = mediaConverterConfig.getIncludeFoldersWithGivenNamesOnly();
					}
					if (mediaConverterConfig.getExcludeFoldersWithGivenNames() != null && !mediaConverterConfig.getExcludeFoldersWithGivenNames().isEmpty()) {
						excludeFoldersWithGivenNames = mediaConverterConfig.getExcludeFoldersWithGivenNames();
					}
					if (mediaConverterConfig.getCompressionLevel() > 0 && mediaConverterConfig.getCompressionLevel() < 34) {
						compressionLevel = mediaConverterConfig.getCompressionLevel();
					}
					if (mediaConverterConfig.getProcessTimeout() > 0) {
						processTimeout = mediaConverterConfig.getProcessTimeout();
					}
				}
			} else {
				logger.info("Unable to read data from file : media-converter-config.json");
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
				processImageFilesRecursively(child, outFolder);
			}
		}
	}

}
