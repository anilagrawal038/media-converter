package com.san.util;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.ObjectInput;
import java.io.ObjectInputStream;
import java.io.ObjectOutput;
import java.io.ObjectOutputStream;
import java.io.Reader;
import java.io.Serializable;
import java.io.Writer;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Base64;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Queue;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.core.JsonGenerationException;
import com.fasterxml.jackson.core.JsonParseException;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.type.MapType;
import com.fasterxml.jackson.databind.type.TypeFactory;
import com.san.to.FileTO;

public class CommonUtil {

	static Logger logger = LoggerFactory.getLogger(CommonUtil.class);

	private static SimpleDateFormat folderNameTimeFormat = new SimpleDateFormat("yyyy_MM_dd_HH_mm_ss_SSS", Locale.ENGLISH);
	private static ObjectMapper objectMapper = new ObjectMapper();
	private static Boolean isWindowsOS = null;

	public static boolean isWindowsOS() {
		if (isWindowsOS == null) {
			String osName = System.getProperty("os.name");
			logger.debug("OS name -> " + osName);
			logger.debug("OS version -> " + System.getProperty("os.version"));
			logger.debug("OS Architecture -> " + System.getProperty("os.arch"));
			if (osName != null && !osName.isEmpty() && osName.toLowerCase().contains("windows")) {
				isWindowsOS = true;
			} else {
				isWindowsOS = false;
			}
		}
		return isWindowsOS;
	}

	public static void mapProperties(Object sourceObject, Object destObject) {
		mapProperties(sourceObject, destObject, false);
	}

	@SuppressWarnings("unchecked")
	public static <T> T cloneObject(T sourceObject) {
		T clone = null;
		try {
			clone = (T) sourceObject.getClass().newInstance();
			mapProperties(sourceObject, clone, true);
		} catch (InstantiationException | IllegalAccessException e) {
		}
		return clone;
	}

	public static <T> String joinCollection(String delimiter, Collection<T> data) {
		if (data == null || data.size() < 1) {
			return "";
		}
		StringBuilder sb = new StringBuilder();
		data.stream().forEach(obj -> {
			if (sb.length() > 0) {
				sb.append(delimiter);
			}
			sb.append(obj);
		});
		return sb.toString();
	}

	public static String fetchFileContentAsString(String filePath, boolean removeLineSeparator) throws IOException {
		String content = null;
		File file = new File(filePath);
		if (file.exists() && file.isFile() && file.canRead()) {
			FileInputStream fis = new FileInputStream(file);
			content = fetchStringFromInputStream(fis, removeLineSeparator);
		} else {
			InputStream inputStream = CommonUtil.class.getClassLoader().getResourceAsStream(filePath);
			content = fetchStringFromInputStream(inputStream, removeLineSeparator);
		}
		return content;
	}

	public static String fetchStringFromInputStream(InputStream inputStream, boolean removeLineSeparator) throws IOException {
		BufferedReader inStreamReader = new BufferedReader(new InputStreamReader(inputStream));
		String inputLine;
		StringBuffer response = new StringBuffer();
		while ((inputLine = inStreamReader.readLine()) != null) {
			response.append(inputLine);
			if (!removeLineSeparator) {
				response.append(System.lineSeparator());
			}
		}
		inputStream.close();
		return response.toString();
	}

	public static InputStream fetchInputStreamFromString(String data) {
		return new ByteArrayInputStream(data.getBytes());
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

	public static byte[] convertToJsonBytes(Object object) throws JsonGenerationException, JsonMappingException, IOException {
		byte[] out = null;
		ByteArrayOutputStream byteOutputStream = null;
		try {
			byteOutputStream = new ByteArrayOutputStream();
			objectMapper.writeValue(byteOutputStream, object);
			out = byteOutputStream.toByteArray();
		} finally {
			byteOutputStream.close();
		}
		return out;
	}

	public static void writeJson(Writer writer, Object object) throws JsonGenerationException, JsonMappingException, IOException {
		try {
			objectMapper.writeValue(writer, object);
		} finally {
			writer.close();
		}
	}

	public static String readStringFromReader(Reader reader) throws JsonParseException, JsonMappingException, IOException {
		StringBuilder stringBuilder = null;
		try {
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			stringBuilder = new StringBuilder();
			while ((str = br.readLine()) != null) {
				System.out.println(str);
				stringBuilder.append(str);
			}
		} finally {
			reader.close();
		}
		return stringBuilder.toString();
	}

	public static Map<String, Object> readJsonMapFromReader(Reader reader) throws JsonParseException, JsonMappingException, IOException {
		Map<String, Object> map = new HashMap<String, Object>();
		try {
			BufferedReader br = new BufferedReader(reader);
			String str = null;
			StringBuilder stringBuilder = new StringBuilder();
			while ((str = br.readLine()) != null) {
				System.out.println(str);
				stringBuilder.append(str);
			}
			map = objectMapper.readValue(stringBuilder.toString(), new TypeReference<HashMap<String, Object>>() {
			});
		} finally {
			reader.close();
		}
		return map;
	}

	public static JsonNode readJsonNodeFromReader(Reader reader) throws JsonProcessingException, IOException {
		JsonNode jsonNode = null;
		try {
			jsonNode = objectMapper.readTree(reader);
		} finally {
			reader.close();
		}
		return jsonNode;
	}

	public static JsonNode fetchJSONResource(String resource) {
		JsonNode jsonNode = null;
		InputStream is = null;
		try {
			is = CommonUtil.class.getClassLoader().getResourceAsStream(resource);
			jsonNode = objectMapper.readTree(is);
			is.close();
		} catch (Exception e) {
			// TODO: handle exception
		} finally {

		}
		return jsonNode;
	}

	public static String fetchResourceLocation() {
		String path = "";
		boolean resourceLocationFound = false;
		try {
			URL url = CommonUtil.class.getClassLoader().getResource("log4j.properties");
			if (url != null) {
				path = url.getPath();
				File file = new File(path);
				if (file.isFile()) {
					path = file.getParent();
					resourceLocationFound = true;
				}
			}
			if (!resourceLocationFound) {
				System.out.println("log4j.properties not found inside WEB-INF/classes");
				System.out.println("Other resources also may not found due to relative location configuration");
			}
		} catch (Exception e) {
			// TODO: handle exception
		}
		return path;
	}

	public static <T> T bindJSONToObject(String jsonString, Class<T> clazz) throws JsonParseException, JsonMappingException, IOException {
		T obj = objectMapper.readValue(jsonString, clazz);
		return obj;
	}

	public static Map<String, String> bindJSONToMap(String jsonString) throws JsonParseException, JsonMappingException, IOException {
		TypeFactory factory = TypeFactory.defaultInstance();
		MapType type = factory.constructMapType(HashMap.class, String.class, String.class);
		Map<String, String> obj = objectMapper.readValue(jsonString, type);
		return obj;
	}

	public static byte[] fetchByteArrayOfSerializable(Serializable object) throws IOException {
		ByteArrayOutputStream bos = new ByteArrayOutputStream();
		ObjectOutput out = null;
		byte[] bytes = null;
		try {
			out = new ObjectOutputStream(bos);
			out.writeObject(object);
			out.flush();
			bytes = bos.toByteArray();
		} finally {
			try {
				bos.close();
			} catch (IOException ex) {
				logger.error("Exception in fetchByteArrayOfSerializable(), exp : ", ex);
			}
		}
		return bytes;

	}

	public static Serializable fetchSerializableFromByteArray(byte[] bytes) throws IOException, ClassNotFoundException {
		ByteArrayInputStream bis = new ByteArrayInputStream(bytes);
		ObjectInput in = null;
		Serializable object = null;
		try {
			in = new ObjectInputStream(bis);
			object = (Serializable) in.readObject();
		} finally {
			try {
				if (in != null) {
					in.close();
				}
			} catch (IOException ex) {
				logger.error("Exception in fetchSerializableFromByteArray(), exp : ", ex);
			}
		}
		return object;
	}

	public static String encodeToBase64String(byte[] bytes) {
		return Base64.getEncoder().encodeToString(bytes);
	}

	public static byte[] decodeFromBase64String(String data) {
		return Base64.getDecoder().decode(data);
	}

	public static int compareVersions(String version1, String version2) {
		if (version1 == null || version2 == null || version1.length() < 1 || version2.length() < 1) {
			return -1;
		}
		int major1 = 0, minor1 = 0, patch1 = 0;
		int major2 = 0, minor2 = 0, patch2 = 0;
		String[] version1Parts = version1.split("\\.");
		String[] version2Parts = version2.split("\\.");
		if (version1Parts.length > 0 && version1Parts[0].length() > 0) {
			try {
				major1 = Integer.parseInt(version1Parts[0]);
			} catch (IllegalArgumentException exp) {
			}
		}
		if (version1Parts.length > 1 && version1Parts[1].length() > 0) {
			try {
				minor1 = Integer.parseInt(version1Parts[1]);
			} catch (IllegalArgumentException exp) {
			}
		}
		if (version1Parts.length > 2 && version1Parts[2].length() > 0) {
			try {
				patch1 = Integer.parseInt(version1Parts[2]);
			} catch (IllegalArgumentException exp) {
			}
		}
		if (version2Parts.length > 0 && version2Parts[0].length() > 0) {
			try {
				major2 = Integer.parseInt(version2Parts[0]);
			} catch (IllegalArgumentException exp) {
			}
		}
		if (version2Parts.length > 1 && version2Parts[1].length() > 0) {
			try {
				minor2 = Integer.parseInt(version2Parts[1]);
			} catch (IllegalArgumentException exp) {
			}
		}
		if (version2Parts.length > 2 && version2Parts[2].length() > 0) {
			try {
				patch2 = Integer.parseInt(version2Parts[2]);
			} catch (IllegalArgumentException exp) {
			}
		}
		int status = 0;
		if (major1 < major2) {
			return -1;
		} else if (status == 0 && major1 > major2) {
			status = 1;
		}
		if (minor1 < minor2) {
			return -1;
		} else if (status == 0 && minor1 > minor2) {
			status = 1;
		}
		if (patch1 < patch2) {
			return -1;
		} else if (status == 0 && patch1 > patch2) {
			status = 1;
		}
		return status;
	}

	@SuppressWarnings("unchecked")
	public static Object trimProperties(Object sourceObject) {
		Object destObject = null;
		try {
			destObject = sourceObject.getClass().getConstructor().newInstance();
		} catch (Exception exp) {
			return sourceObject;
		}
		Class<?> clazz = sourceObject.getClass();
		Method[] methods = clazz.getMethods();
		for (Method sourceMethod : methods) {
			String sourceMethodName = sourceMethod.getName();
			String destMethodName = "set" + sourceMethodName.substring(3);
			Method destMethod = null;
			try {

				if (sourceMethodName.startsWith("get")) {
					destMethod = clazz.getMethod(destMethodName, sourceMethod.getReturnType());
				} else if (sourceMethodName.startsWith("is")) {
					destMethodName = "set" + sourceMethodName.substring(2);
					destMethod = clazz.getMethod(destMethodName, sourceMethod.getReturnType());
				} else {
					continue;
				}
				Class<?> returnType = sourceMethod.getReturnType();
				Class<?>[] paramTypes = destMethod.getParameterTypes();
				Object valueObject = sourceMethod.invoke(sourceObject);
				String fieldName = destMethodName.substring(3, 4).toLowerCase() + destMethodName.substring(4);
				Field targetField = clazz.getDeclaredField(fieldName);
				if (valueObject == null || targetField == null || paramTypes.length != 1 || !paramTypes[0].equals(returnType)) {
					continue;
				}
				if (returnType.equals(String.class)) {
					String value = (String) valueObject;
					if (value != null) {
						destMethod.invoke(destObject, value.trim());
					}
				} else if (Iterable.class.isAssignableFrom(returnType) && ((ParameterizedType) targetField.getGenericType()).getActualTypeArguments()[0].equals(String.class)) {
					if (Set.class.isAssignableFrom(returnType)) {
						Set<String> values = (Set<String>) valueObject;
						Set<String> newIterable = values.getClass().getConstructor().newInstance();
						for (String value : values) {
							if (value != null) {
								newIterable.add(value.trim());
							}
						}
						destMethod.invoke(destObject, newIterable);
					} else if (List.class.isAssignableFrom(returnType)) {
						List<String> values = (List<String>) valueObject;
						List<String> newIterable = values.getClass().getConstructor().newInstance();
						for (String value : values) {
							if (value != null) {
								newIterable.add(value.trim());
							}
						}
						destMethod.invoke(destObject, newIterable);
					} else if (Queue.class.isAssignableFrom(returnType)) {
						Queue<String> values = (Queue<String>) valueObject;
						Queue<String> newIterable = values.getClass().getConstructor().newInstance();
						for (String value : values) {
							if (value != null) {
								newIterable.add(value.trim());
							}
						}
						destMethod.invoke(destObject, newIterable);
					}
				} else if (Map.class.isAssignableFrom(returnType) && ((ParameterizedType) targetField.getGenericType()).getActualTypeArguments()[0].equals(String.class) && ((ParameterizedType) targetField.getGenericType()).getActualTypeArguments()[1].equals(String.class)) {
					Map<String, String> map = (Map<String, String>) valueObject;
					Map<String, String> newMap = map.getClass().getConstructor().newInstance();
					for (String key : map.keySet()) {
						String value = map.get(key);
						if (value != null) {
							newMap.put(key.trim(), value.trim());
						}
					}
					destMethod.invoke(destObject, newMap);
				} else {
					destMethod.invoke(destObject, valueObject);
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException | InstantiationException | NoSuchFieldException e) {
			}
		}
		return destObject;
	}

	public static byte[] objectToBytes(Object obj) {
		byte[] data = new byte[] {};
		if (obj == null) {
			return data;
		}
		ByteArrayOutputStream bos = null;
		ObjectOutputStream oos = null;
		try {
			bos = new ByteArrayOutputStream();
			oos = new ObjectOutputStream(bos);
			oos.writeObject(obj);
			oos.flush();
			data = bos.toByteArray();
		} catch (Exception e) {
			logger.info("Exception occurred in CommonsUtil.objectToBytes(), obj : " + obj);
		} finally {
			try {
				oos.close();
			} catch (Exception e) {
			}
			try {
				bos.close();
			} catch (Exception e) {
			}
		}
		return data;
	}

	public static Object bytesToObject(byte[] bytes) {
		Object obj = null;
		if (bytes == null) {
			return obj;

		}
		ByteArrayInputStream bis = null;
		ObjectInputStream ois = null;
		try {
			bis = new ByteArrayInputStream(bytes);
			ois = new ObjectInputStream(bis);
			obj = ois.readObject();

		} catch (Exception e) {
			logger.info("Exception occurred in CommonUtil.bytesToObject(), bytes : " + bytes);
		} finally {
			try {
				ois.close();
			} catch (Exception e) {
			}
			try {
				bis.close();
			} catch (Exception e) {
			}
		}
		return obj;
	}

	public static long fetchVersionNumericValue(String version) {
		String[] versionParts = version.trim().split(" ")[0].split("\\.");
		int[] versionPartValues = new int[4];
		int versionPartCounter = 0;
		for (String versionPart : versionParts) {
			versionPartValues[versionPartCounter++] = Integer.parseInt(versionPart);
		}
		String versionIntStr = String.format("%03d%03d%03d%03d", versionPartValues[0], versionPartValues[1], versionPartValues[2], versionPartValues[3]);
		return Long.parseLong(versionIntStr);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	public static void mapProperties(Object sourceObject, Object destObject, boolean cloneObject) {
		if (sourceObject == null || destObject == null) {
			return;
		}
		Class<?> sourceClass = sourceObject.getClass();
		Class<?> destClass = destObject.getClass();
		Method[] methods = sourceClass.getMethods();
		for (Method sourceMethod : methods) {
			String sourceMethodName = sourceMethod.getName();
			String destMethodName = "set" + sourceMethodName.substring(3);
			Method destMethod = null;
			try {
				if (sourceMethodName.startsWith("get")) {
					destMethod = destClass.getMethod(destMethodName, sourceMethod.getReturnType());
				} else if (sourceMethodName.startsWith("is")) {
					destMethodName = "set" + sourceMethodName.substring(2);
					destMethod = destClass.getMethod(destMethodName, sourceMethod.getReturnType());
				}
				if (destMethod != null) {
					Object source = sourceMethod.invoke(sourceObject);
					if (source != null) {
						if (!cloneObject) {
							destMethod.invoke(destObject, source);
						} else {
							if (source instanceof Collection) {
								try {
									if (source instanceof List) {
										List<?> tmp = (List<?>) source.getClass().newInstance();
										tmp.addAll((List) source);
										source = tmp;
									} else if (source instanceof Set) {
										Set<?> tmp = (Set<?>) source.getClass().newInstance();
										tmp.addAll((Set) source);
										source = tmp;
									} else if (source instanceof Map) {
										Map<?, ?> tmp = (Map<?, ?>) source.getClass().newInstance();
										tmp.putAll((Map) source);
										source = tmp;
									}
								} catch (Exception e) {
								}
							}
							destMethod.invoke(destObject, source);
						}
					}
				}
			} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException | InvocationTargetException e) {
			}
		}
	}

	public static String dateTimeStringForFolderName(Long timestamp) {
		if (timestamp == null) {
			timestamp = new Date().getTime();
		}
		return folderNameTimeFormat.format(timestamp);
	}

	public static Type getGenericParameterTypeOfClass(Class<?> clazz, int parameterIndex) {
		return ((ParameterizedType) clazz.getGenericSuperclass()).getActualTypeArguments()[parameterIndex];
	}

	public static Type getGenericParameterTypeOfInterface(Class<?> clazz, int parameterIndex) {
		return ((ParameterizedType) clazz.getGenericInterfaces()[0]).getActualTypeArguments()[parameterIndex];
	}

	// Execute system command
	public static void executeCommand(String command) {
		String[] executableCommand = { "/bin/sh", "-c", command };
		if (System.getProperty("os.name").toLowerCase().indexOf("windows") > -1) {
			executableCommand = new String[] { "cmd", "/C", command };
		}
		try {
			Process p = Runtime.getRuntime().exec(executableCommand);
			// boolean status = p.waitFor(timeOut, TimeUnit.SECONDS);
			int status = p.waitFor();
			logger.debug("Command : " + command + " exited with status code : " + status);
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			try {
			} catch (Exception e) {
			}
		}
	}

	public static String fetchFolderNameTime(long timestamp) {
		return folderNameTimeFormat.format(timestamp);
	}

	public static FileTO populateFileHierarchy(File file) {
		FileTO root = new FileTO();
		if (file != null) {
			root.setName(file.getName());
			try {
				// root.setAbsolutePath(file.getCanonicalPath());
				// Note : CanonicalPath resolves soft links (on Linux) as actual file which causing (Duplicate Key) issue on UI
				root.setAbsolutePath(file.getAbsolutePath());
			} catch (Exception e) {
			}
			if (file.isDirectory()) {
				root.setDirectory(true);
				for (File child : file.listFiles()) {
					root.addChild(populateFileHierarchy(child));
				}
			}
		}
		return root;
	}

	public static List<String> splitMacrosByParenthesis(String args) {
		List<String> arguments = new ArrayList<String>();
		int pOpenCounter = 0;
		boolean addArgument = false;
		int lastIndex = 0;
		for (int charCounter = 0; charCounter < args.length(); charCounter++) {
			if (args.charAt(charCounter) == '{') {
				addArgument = true;
				pOpenCounter++;
			} else if (args.charAt(charCounter) == '}') {
				pOpenCounter--;
			}
			if (addArgument && pOpenCounter == 0) {
				if (args.substring(charCounter).indexOf("{") > -1) {
					arguments.add(args.substring(lastIndex, charCounter + 1));
					lastIndex = charCounter + 1;
					addArgument = false;
				} else {
					arguments.add(args.substring(lastIndex, args.length()));
					break;
				}
			}
		}
		return arguments;
	}

	private static String formatRegexMacrosRecursively(Pattern pattern, Pattern argPattern, String searchKeys) {
		if (pattern == null) {
			pattern = Pattern.compile("\\$\\{\\{\\w*(\\{\\{.*\\}\\})*\\}\\}");
		}
		if (argPattern == null) {
			argPattern = Pattern.compile("\\{\\{.*\\}\\}");
		}
		String formattedSearchKey = searchKeys;
		if (searchKeys.indexOf("${{") > -1) {
			List<String> formattedParams = splitMacrosByParenthesis(searchKeys);
			for (String searchKey : formattedParams) {
				String formattedParam = searchKey;
				Matcher matcher = pattern.matcher(searchKey);
				while (matcher.find()) {
					String str = matcher.group();
					String _str = str;
					str = str.substring(3, str.length() - 2);
					if (str.indexOf("{{") > -1) {
						String key = str.substring(0, str.indexOf("{{"));
						String argsPart = str.substring(str.indexOf("{{") + 2, str.length() - 2);
						// str = LocalizationUtil.getLocaleFormattedGlobalDataForUI(key);
						str = key;
						List<String> argsStrings = splitMacrosByParenthesis(argsPart);
						for (String argsString : argsStrings) {
							Matcher argMatcher = argPattern.matcher(argsString);
							while (argMatcher.find()) {
								String argStr = argMatcher.group();
								argStr = argStr.substring(2, argStr.length() - 2);
								List<String> keyValuePair = splitMacrosByParenthesis(argStr);
								if (keyValuePair.size() == 2) {
									String argKey = keyValuePair.get(0).substring(2, keyValuePair.get(0).length() - 2);
									String argValue = keyValuePair.get(1).substring(2, keyValuePair.get(1).length() - 2);
									str = str.replace(argKey, formatRegexMacrosRecursively(pattern, argPattern, argValue));
								}
							}
						}
					} else {
						// str = LocalizationUtil.getLocaleFormattedGlobalDataForUI(str);
						str = str;
					}
					formattedParam = formattedParam.replace(_str, str);
				}
				formattedSearchKey = formattedSearchKey.replace(searchKey, formattedParam);
			}
		}
		return formattedSearchKey;
	}
}
