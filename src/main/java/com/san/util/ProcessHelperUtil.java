package com.san.util;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.util.Date;
import java.util.List;
import java.util.StringTokenizer;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.san.to.ProcessStatusTO;
import com.san.util.CommonUtil;

public class ProcessHelperUtil {

	final static Logger logger = LoggerFactory.getLogger(ProcessHelperUtil.class);
	private static ObjectMapper objectMapper = new ObjectMapper();

	public static Integer PROCESS_PORT = null;

	public static boolean isProcessRunning(Process process, String processLabel) {
		boolean alreadyRunning = false;
		try {
			if (process == null || process.exitValue() == 0) {
				logger.debug("Last " + processLabel + " process was terminated normally");
			} else {
				logger.debug("Last " + processLabel + " process was not terminated normally");
			}
		} catch (Exception e) {
			logger.trace("Process " + processLabel + " is in running mode");
			alreadyRunning = true;
		}
		return alreadyRunning;
	}

	public static Process startProcess(int port, String jarPath) {
		String[] command = new String[] { "java", //
				// "-Xdebug", // For Remote debugging
				// "-Xrunjdwp:transport=dt_socket,address=" + (port + 1) + ",server=y", // For Remote debugging
				"-jar", //
				jarPath, //
				"--server.port=" + port, //
				"--process.home=" + "", //
				"--process.healthCheckAPI=" + "" //
		};

		ProcessBuilder startServiceProcessBuilder = new ProcessBuilder(command);
		Process p = null;
		try {
			p = startServiceProcessBuilder.start();
			InputStream inputStream = p.getInputStream();
			InputStream errStream = p.getErrorStream();
			try {
				// Note: There are below options to make subprocess work normally
				// 1: Read continuously from input streams
				// 2: Close input streams
				// 3: Redirect input streams
				// Reason: Its required to clear the in-memory input stream buffers of sub-process
				if (logger.isTraceEnabled()) {
					// logger.info("Logger level is TRACE, starting Service console logger ...");
					// startStreamLogger("native-library-service-console-logger", inputStream, logger);

					logger.debug("Logger level is TRACE, closing Service Console Stream ...");
					inputStream.close();

					logger.debug("Logger level is TRACE, starting Service error logger ...");
					startStreamLogger("native-library-service-error-logger", errStream, logger);
				} else {
					logger.debug("Logger level is not TRACE, closing Service Console, Error Streams ...");
					inputStream.close();
					errStream.close();
				}
				logger.debug("Process (" + jarPath + ") started successfully");
			} catch (IOException e1) {
				logger.error("Exception occurred in ProcessHelperUtil.startProcess()", e1);
			}
		} catch (IOException e) {
			logger.error("Exception occurred in ProcessHelperUtil.startProcess()", e);
		}
		return p;
	}

	public static int executeCommand(String shellCommand, long timeoutInSeconds, String outputLinePrefix) {
		String arg1, arg2 = null;
		StringBuilder res = new StringBuilder();
		int status = 1;

		if (CommonUtil.isWindowsOS()) {
			arg1 = "cmd.exe";
			arg2 = "/C";
		} else {
			arg1 = "bash";
			arg2 = "-c";
		}

		ProcessBuilder startServiceProcessBuilder = new ProcessBuilder(arg1, arg2, shellCommand);
		Process p;
		try {
			p = startServiceProcessBuilder.start();
		} catch (IOException e) {
			logger.debug("Exception occurred in ProcessHelperUtil.executeCommand() while executing command : " + shellCommand + "", e);
			return status;
		}

		InputStreamReader tempReader = new InputStreamReader(new BufferedInputStream(p.getInputStream()));
		final BufferedReader reader = new BufferedReader(tempReader);
		InputStreamReader tempErrReader = new InputStreamReader(new BufferedInputStream(p.getErrorStream()));
		final BufferedReader errReader = new BufferedReader(tempErrReader);
		AtomicBoolean interruptLoggingThread = new AtomicBoolean(false);

		new Thread("executeCommand()::stdOut-Thread") {
			public void run() {
				String line = null;
				try {
					logger.debug("Executed command : " + shellCommand + " :: Console Output : \n");
					boolean usePrefix = ((outputLinePrefix != null) && !outputLinePrefix.isEmpty());
					while (!interruptLoggingThread.get()) {
						if (reader.ready()) {
							line = reader.readLine();
							if (line == null || line.isEmpty()) {
								continue;
							}
							line = line.trim();
							if (!line.isEmpty()) {
								if (usePrefix) {
									if (line.indexOf(outputLinePrefix) > -1) {
										res.setLength(0);
										res.append(line);
									}
								} else {
									res.setLength(0);
									res.append(line);
								}
								logger.debug(line);
							}
						} else {
							Thread.sleep(1000l);
						}
					}
				} catch (Exception e) {
					logger.debug("Executed command : " + shellCommand + " :: Console Output Line : " + line);
					logger.debug("Exception occurred in ProcessHelperUtil.executeCommand() while executing command : " + shellCommand + " ", e);
				}
			}
		}.start();

		new Thread("executeCommand()::stdErr-Thread") {
			public void run() {
				String line = null;
				try {
					logger.debug("Executed command : " + shellCommand + " :: Error Output : \n");
					while (!interruptLoggingThread.get()) {
						if (errReader.ready()) {
							line = errReader.readLine();
							if (line == null || line.isEmpty()) {
								continue;
							}
							line = line.trim();
							if (!line.isEmpty()) {
								logger.debug(line);
							}
						} else {
							Thread.sleep(1000l);
						}
					}
				} catch (Exception e) {
					logger.debug("Exception occurred in ProcessHelperUtil.executeCommand() while executing command : " + shellCommand + "", e);
				}
			}
		}.start();

		try {
			p.waitFor(timeoutInSeconds, TimeUnit.SECONDS);
			interruptLoggingThread.set(true);
			try {
				p.destroyForcibly();
			} catch (Exception e) {
			}
			status = p.exitValue();
		} catch (Exception e) {
			logger.debug("[Waiting for process for maximum of " + timeoutInSeconds + " seconds] : Exception occurred in ProcessHelperUtil.executeCommand() while executing command : " + shellCommand + "", e);
		}

		try {
			reader.close();
		} catch (Exception e) {
		}
		try {
			tempReader.close();
		} catch (Exception e) {
		}
		try {
			errReader.close();
		} catch (Exception e) {
		}
		try {
			tempErrReader.close();
		} catch (Exception e) {
		}
		return status;
	}

	public static ProcessStatusTO executeCommandWithOutput(String shellCommand, long timeout) {
		String arg1, arg2;
		final ProcessStatusTO processStatus = new ProcessStatusTO();
		if (timeout <= 0) {
			timeout = 30; // Default timeout is 30 seconds
		}
		processStatus.setStartTime((new Date()).getTime());
		if (CommonUtil.isWindowsOS()) {
			arg1 = "cmd.exe";
			arg2 = "/C";
		} else {
			arg1 = "bash";
			arg2 = "-c";
		}
		logger.debug("Executing command \n\n" + shellCommand + "\n\n");

		Process p;
		ProcessBuilder startServiceProcessBuilder = null;
		if (shellCommand.indexOf(System.lineSeparator()) > -1) {
			startServiceProcessBuilder = new ProcessBuilder(arg1);
		} else {
			startServiceProcessBuilder = new ProcessBuilder(arg1, arg2, shellCommand);
		}

		try {
			p = startServiceProcessBuilder.start();
			InputStream inputStream = p.getInputStream();
			InputStream errStream = p.getErrorStream();
			OutputStream outStream = null;
			try {
				// Note: There are below options to make subprocess work normally
				// 1: Read continuously from input streams
				// 2: Close input streams
				// 3: Redirect input streams
				// Reason: Its required to clear the in-memory input stream buffers of sub-process

				startStreamLogger("os-command-std-out-thread-" + processStatus.getStartTime(), inputStream, processStatus, false);
				startStreamLogger("os-command-err-out-thread-" + processStatus.getStartTime(), errStream, processStatus, true);
				if (shellCommand.indexOf(System.lineSeparator()) > -1) {
					outStream = p.getOutputStream();
					String[] commandLines = shellCommand.split(System.lineSeparator());
					if (commandLines.length > 1) {
						for (int index = 0; index < commandLines.length; index++) {
							outStream.write((commandLines[index] + System.lineSeparator()).getBytes());
							try {
								outStream.flush();
							} catch (Exception e) {
							}
						}
					}
				}
				if (outStream != null) {
					try {
						outStream.close();
					} catch (Exception e) {
					}
				}
			} catch (Exception e1) {
				logger.error("IO Exception occurred in ProcessHelperUtil.executeCommand()", e1);
			}
			p.waitFor(timeout, TimeUnit.SECONDS);
			processStatus.setEndTime((new Date()).getTime());
			if (p.isAlive()) {
				try {
					logger.debug("Process still alive, killing it forcibly.");
					processStatus.setExitCode(-9);
					processStatus.setTimeoutOccurred(true);
					p.destroyForcibly();
				} catch (Exception e) {
				}
			} else {
				processStatus.setExitCode(p.exitValue());
			}
		} catch (IOException | InterruptedException e) {
			logger.error("Exception occurred in ProcessHelperUtil.executeCommand()", e);
		}

		logger.debug("processStatus : " + processStatus);
		return processStatus;
	}

	public static boolean processExecutableExists(String jarPath) {
		if (!new File(jarPath).exists()) {
			logger.info("Executable (" + jarPath + ") not exists.");
			return false;
		}
		return true;
	}

	// Return pair of PID & PORT
	public static Integer findAnyRunningProcess(String executable, String args) {
		String command = "ps -e -o pid=ProcessId -o cmd=CommandLine | grep -E \"ProcessId|" + executable + "\" | awk '{print $1\",\"$2\" \"$3\" \"$4\" \"$5}'";
		// ps -e -o pid=ProcessId -o cmd=CommandLine | grep -E \"ProcessId|java\" | awk '{print $1\",\"$2\" \"$3\" \"$4\" \"$5}'
		if (CommonUtil.isWindowsOS()) {
			// wmic process where Name=\"java.exe\" get ProcessId,CommandLine /format:\"%WINDIR%\\System32\\wbem\\en-us\\csv\"
			command = "wmic process where Name=\"" + executable + ".exe" + "\" get ProcessId,CommandLine /format:\"%WINDIR%\\System32\\wbem\\en-us\\csv\"";
		}
		ProcessStatusTO procStatus = executeCommandWithOutput(command, 5);
		List<String> outputLines = procStatus.getStdOut();
		Integer processID = null;
		int processIDIndex = 0, commandLineIndex = 0, headerColumnCount = 0;
		if (outputLines != null && !outputLines.isEmpty()) {
			for (String line : outputLines) {
				if (line.contains("ProcessId")) {
					String[] lineParts = line.split(",");
					headerColumnCount = lineParts.length;
					for (int counter = 0; counter < lineParts.length; counter++) {
						String linePart = lineParts[counter];
						if (linePart.equals("ProcessId")) {
							processIDIndex = counter;
						} else if (linePart.equals("CommandLine")) {
							commandLineIndex = counter;
						}
					}
				} else if (line.contains(args)) {
					String[] lineParts = line.split(",");
					logger.debug("A running process found. Command line is as below \n" + lineParts[commandLineIndex]);
					try {
						if (processIDIndex == (headerColumnCount - 1)) {
							processID = Integer.parseInt(lineParts[lineParts.length - 1].trim());
						} else {
							processID = Integer.parseInt(lineParts[processIDIndex].trim());
						}
					} catch (Exception e) {
						logger.error("Unable to fetch ProcessId for running process : " + line);
					}
					break;
				}
			}
		}
		if (processID == null || processID < 1) {
			return null;
		}
		return processID;
	}

	// Return pair of PID & PORT
	public static Integer findAnyRunningProcesses(String jarName, String className) {
		String command = "ps -e -o pid=ProcessId -o cmd=CommandLine | grep -E \"ProcessId|java\" | awk '{print $1\",\"$2\" \"$3\" \"$4\" \"$5}'";
		// ps -e -o pid=ProcessId -o cmd=CommandLine | grep -E \"ProcessId|java\" | awk '{print $1\",\"$2\" \"$3\" \"$4\" \"$5}'
		if (CommonUtil.isWindowsOS()) {
			// wmic process where Name=\"java.exe\" get ProcessId,CommandLine /format:\"%WINDIR%\\System32\\wbem\\en-us\\csv\"
			command = "wmic process where Name=\"java.exe\" get ProcessId,CommandLine /format:\"%WINDIR%\\System32\\wbem\\en-us\\csv\"";
		}
		ProcessStatusTO procStatus = executeCommandWithOutput(command, 5);
		List<String> outputLines = procStatus.getStdOut();
		Integer port = null;
		Integer processID = null;
		int processIDIndex = 0, commandLineIndex = 0;
		if (outputLines != null && !outputLines.isEmpty()) {
			for (String line : outputLines) {
				if (line.contains("ProcessId")) {
					String[] lineParts = line.split(",");
					for (int counter = 0; counter < lineParts.length; counter++) {
						String linePart = lineParts[counter];
						if (linePart.equals("ProcessId")) {
							processIDIndex = counter;
						} else if (linePart.equals("CommandLine")) {
							commandLineIndex = counter;
						}
					}
				} else if (line.contains(jarName) || line.contains(className)) {
					String[] lineParts = line.split(",");
					logger.info("A running process found. Command line is as below \n" + lineParts[commandLineIndex]);
					StringTokenizer tokenizer = new StringTokenizer(lineParts[commandLineIndex]);
					String processPort = null;
					while (tokenizer.hasMoreElements()) {
						String token = tokenizer.nextToken();
						if (token.startsWith("--server.port=")) {
							try {
								processPort = token.split("=")[1];
							} catch (Exception e) {
								logger.error("Exception occurred while fetching process port from token : " + token + ", exp : " + e);
							}
							break;
						}
					}
					if (processPort == null) {
						// If required we can use default port 8081
						// processPort = "8081";
					}
					try {
						port = Integer.parseInt(processPort);
					} catch (Exception e) {
						logger.error("Unable to fetch port for running process : " + line);
					}
					try {
						processID = Integer.parseInt(lineParts[processIDIndex].trim());
					} catch (Exception e) {
						logger.error("Unable to fetch ProcessId for running process : " + line);
					}
					break;
				}
			}
		}
		if (processID == null || processID < 1 || port == null || port < 1) {
			return null;
		}
		return processID;
	}

	public static int stopProcessByProcessID(int processID) {
		String command = null;
		if (CommonUtil.isWindowsOS()) {
			command = "wmic process where ProcessID=" + processID + " call terminate";
		} else {
			command = "kill " + processID;
		}
		executeCommand(command, 5, null);
		return 0;
	}

	public static String convertToJsonString(Object object) throws JsonGenerationException, JsonMappingException, IOException {
		String out = null;
		ByteArrayOutputStream byteOutputStream = null;
		try {
			byteOutputStream = new ByteArrayOutputStream();
			objectMapper.writeValue(byteOutputStream, object);
			out = byteOutputStream.toString();
		} finally {
			byteOutputStream.close();
		}
		return out;
	}

	private static void startStreamLogger(final String threadName, final InputStream inputStream, final Logger _logger) {
		new Thread(threadName) {
			@Override
			public void run() {
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
				boolean status = true;
				while (status) {
					try {
						String msg = br.readLine();
						if (msg != null) {
							_logger.info(msg);
						} else {
							status = false;
						}
					} catch (IOException e) {
						status = false;
					}
				}
				try {
					br.close();
				} catch (Exception e) {
				}
				try {
					inputStream.close();
				} catch (Exception e) {
				}
			}
		}.start();
	}

	private static void startStreamLogger(final String threadName, final InputStream inputStream, final ProcessStatusTO processStatus, boolean isErrorOutput) {
		new Thread(threadName) {
			@Override
			public void run() {
				BufferedReader br = new BufferedReader(new InputStreamReader(inputStream));
				boolean status = true;
				while (status) {
					try {
						String msg = br.readLine();
						if (msg != null) {
							if (isErrorOutput) {
								processStatus.addErrOut(msg);
							} else {
								processStatus.addStdOut(msg);
							}
						} else {
							status = false;
						}
					} catch (IOException e) {
						status = false;
					}
				}
				try {
					br.close();
				} catch (Exception e) {
				}
				try {
					inputStream.close();
				} catch (Exception e) {
				}
			}
		}.start();
	}

}
