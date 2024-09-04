package com.san.to;

import java.util.List;

public class MediaConverterConfig {

	private String sourcePath;
	private String outPath;
	private String tempOutPath;
	private String ffprobePath;
	private String vlcPath;
	private int processTimeout; // In Seconds

	private String ffmpegPath;
	private int compressionLevel = 1; // 1 [Low Compression] - 33 [High Compression]
	private List<String> imgExtensions;
	private List<String> includeFoldersWithGivenNamesOnly;
	private List<String> excludeFoldersWithGivenNames;

	public String getSourcePath() {
		return sourcePath;
	}

	public void setSourcePath(String sourcePath) {
		this.sourcePath = sourcePath;
	}

	public String getOutPath() {
		return outPath;
	}

	public void setOutPath(String outPath) {
		this.outPath = outPath;
	}

	public String getTempOutPath() {
		return tempOutPath;
	}

	public void setTempOutPath(String tempOutPath) {
		this.tempOutPath = tempOutPath;
	}

	public String getFfprobePath() {
		return ffprobePath;
	}

	public void setFfprobePath(String ffprobePath) {
		this.ffprobePath = ffprobePath;
	}

	public String getVlcPath() {
		return vlcPath;
	}

	public void setVlcPath(String vlcPath) {
		this.vlcPath = vlcPath;
	}

	public int getProcessTimeout() {
		return processTimeout;
	}

	public void setProcessTimeout(int processTimeout) {
		this.processTimeout = processTimeout;
	}

	public String getFfmpegPath() {
		return ffmpegPath;
	}

	public void setFfmpegPath(String ffmpegPath) {
		this.ffmpegPath = ffmpegPath;
	}

	public int getCompressionLevel() {
		return compressionLevel;
	}

	public void setCompressionLevel(int compressionLevel) {
		this.compressionLevel = compressionLevel;
	}

	public List<String> getImgExtensions() {
		return imgExtensions;
	}

	public void setImgExtensions(List<String> imgExtensions) {
		this.imgExtensions = imgExtensions;
	}

	public List<String> getIncludeFoldersWithGivenNamesOnly() {
		return includeFoldersWithGivenNamesOnly;
	}

	public void setIncludeFoldersWithGivenNamesOnly(List<String> includeFoldersWithGivenNamesOnly) {
		this.includeFoldersWithGivenNamesOnly = includeFoldersWithGivenNamesOnly;
	}

	public List<String> getExcludeFoldersWithGivenNames() {
		return excludeFoldersWithGivenNames;
	}

	public void setExcludeFoldersWithGivenNames(List<String> excludeFoldersWithGivenNames) {
		this.excludeFoldersWithGivenNames = excludeFoldersWithGivenNames;
	}

}
