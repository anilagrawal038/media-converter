package com.san.to;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;

@JsonSerialize
public class ShellCommandRequestTO {

	private String command;
	private String privateKey;
	private String publicKey;
	private boolean isEncrypted;
	private long timeout; // In seconds

	public String getCommand() {
		return command;
	}

	public void setCommand(String command) {
		this.command = command;
	}

	public String getPrivateKey() {
		return privateKey;
	}

	public void setPrivateKey(String privateKey) {
		this.privateKey = privateKey;
	}

	public String getPublicKey() {
		return publicKey;
	}

	public void setPublicKey(String publicKey) {
		this.publicKey = publicKey;
	}

	public boolean getIsEncrypted() {
		return isEncrypted;
	}

	public void setIsEncrypted(boolean isEncrypted) {
		this.isEncrypted = isEncrypted;
	}

	public long getTimeout() {
		return timeout;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

}
