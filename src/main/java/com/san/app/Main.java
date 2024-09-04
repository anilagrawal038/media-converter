package com.san.app;

public class Main {

	public static void main(String[] args) {
		String converter = "VideoConverter";
		if (args != null && args.length > 0 && !args[0].isEmpty()) {
			converter = args[0].trim();
		}
		switch (converter) {
		case "VideoConverter": {
			VideoConverter.main(args);
			break;
		}
		case "ImageConverter": {
			ImageConverter.main(args);
			break;
		}
		default: {
			VideoConverter.main(args);
		}
		}
	}

}
