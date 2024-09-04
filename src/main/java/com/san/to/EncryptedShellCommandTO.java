package com.san.to;

import java.util.Date;

public class EncryptedShellCommandTO {

	public EncryptedShellCommandTO(String data) {
		this.data = data;
		this.time = new Date().getTime();
	}

	private long time;
	private String data;

	public long getTime() {
		return time;
	}

	public void setTime(long time) {
		this.time = time;
	}

	public String getData() {
		return data;
	}

	public void setData(String data) {
		this.data = data;
	}

}
