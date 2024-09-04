package com.san.to;

public class VideoConverterConfig {

	private String sourcePath;
	private String outPath;
	private String tempOutPath;
	private String ffprobePath;
	private String vlcPath;

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

}
