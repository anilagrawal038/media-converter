package com.san.to;

import java.util.ArrayList;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonInclude.Include;

@JsonInclude(Include.NON_NULL)
public class FileTO {

	private String name;
	private String absolutePath;
	private String message;
	private Boolean isDirectory;
	private List<FileTO> children;

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Boolean isDirectory() {
		return isDirectory;
	}

	public void setDirectory(Boolean isDirectory) {
		this.isDirectory = isDirectory;
		if (Boolean.TRUE.equals(isDirectory) && children == null) {
			children = new ArrayList<>();
		}
	}

	public List<FileTO> getChildren() {
		return children;
	}

	public void setChildren(List<FileTO> children) {
		this.children = children;
	}

	public void addChild(FileTO child) {
		if (children == null) {
			children = new ArrayList<>();
		}
		children.add(child);
	}

	public String getAbsolutePath() {
		return absolutePath;
	}

	public void setAbsolutePath(String absolutePath) {
		this.absolutePath = absolutePath;
	}

	public String getMessage() {
		return message;
	}

	public void setMessage(String message) {
		this.message = message;
	}

}
