package com.san.util;

import java.security.InvalidKeyException;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.interfaces.RSAPublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.PKCS8EncodedKeySpec;
import java.security.spec.X509EncodedKeySpec;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.san.to.CommonResponseTO;
import com.san.to.EncryptedShellCommandTO;

public class ShellExecutorUtil {

	static Logger logger = LoggerFactory.getLogger(ShellExecutorUtil.class);

	@SuppressWarnings("unused")
	private static final long ENCRYPTION_TIMEOUT = 1000l; // In MS
	private static final String PUBLIC_KEY;
	static {
		String publicKey = "-----BEGIN PUBLIC KEY-----";
		publicKey += "MIIBIjANBgkqhkiG9w0BAQEFAAOCAQ8AMIIBCgKCAQEAnvXQMwJYCixFmrjNL87Y";
		publicKey += "x8yOgAiXO4lAJ5iQFp2MFEm+jTlo8MXbvNBbleNSS4Nag476aD6Z23BiC1NLH18R";
		publicKey += "Od7uMTEQAHxJYHss65TyWlx2AMHpm4tXbk2Gh3vfuoqyWGwVCz2MqHFD0P9YhglL";
		publicKey += "uvOxiHXRJ2F7UIkoocVJrsJgZBVFEPjvwtU4frW2KYv+A4m8FlfqgjYr13fYwFuU";
		publicKey += "4p5bnOyKd5YjmRfIUEbePNX/ETlhWhO/qslxnD/1yQsSdUUjWlvlM8A6xyLd9RmC";
		publicKey += "fjFT/EJQENk7UjhpdNmActoy//6zhVQOxMFMwoYlMZgKhV0hlk8wIg2w3dmswZFp";
		publicKey += "7wIDAQAB";
		publicKey += "-----END PUBLIC KEY-----";
		PUBLIC_KEY = publicKey;
	}

	public static CommonResponseTO encryptCommand(String command) {
		return encryptCommand(command, PUBLIC_KEY);
	}

	private static CommonResponseTO encryptCommand(String command, String publicKey) {
		CommonResponseTO response = new CommonResponseTO();
		String rawData = command;
		// Wrap data in JSON along with time-stamp
		try {
			EncryptedShellCommandTO to = new EncryptedShellCommandTO(command);
			rawData = CommonUtil.convertToJsonString(to);
			response.setStatus(true);
		} catch (Exception exp) {
			response.setStatus(false);
			response.setMessage("Data encryption failed due to exception : " + exp.getClass());
			response.setData(exp.getMessage());
		}
		if (response.isStatus()) {
			try {
				String encryptedData = encryptString(rawData, publicKey);
				response.setStatus(true);
				response.setMessage("Data encrypted successfully");
				response.setData(encryptedData);
			} catch (Exception exp) {
				response.setStatus(false);
				response.setMessage("Data encryption failed due to exception : " + exp.getClass());
				response.setData(exp.getMessage());
			}
		}
		return response;
	}

	private static String encryptString(String data, String publicKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		// Turn the encoded key into a real RSA public key.
		// Public keys are encoded in X.509.
		PublicKey publicKey = null;
		KeyFactory kf = KeyFactory.getInstance("RSA");
		publicKeyString = publicKeyString.replaceAll("\\n", "").replace("-----BEGIN PUBLIC KEY-----", "").replace("-----END PUBLIC KEY-----", "");
		X509EncodedKeySpec keySpecX509 = new X509EncodedKeySpec(CommonUtil.decodeFromBase64String(publicKeyString));
		publicKey = (RSAPublicKey) kf.generatePublic(keySpecX509);

		// Encrypt data using public key
		byte[] encryptedData = null;
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.ENCRYPT_MODE, publicKey);
		encryptedData = cipher.doFinal(data.getBytes());

		return CommonUtil.encodeToBase64String(encryptedData);
	}

	@SuppressWarnings("unused")
	private static String decryptString(String encryptedData, String privateKeyString) throws NoSuchAlgorithmException, InvalidKeySpecException, NoSuchPaddingException, InvalidKeyException, IllegalBlockSizeException, BadPaddingException {
		// Turn the encoded key into a real RSA private key.
		PrivateKey privateKey = null;
		KeyFactory kf = KeyFactory.getInstance("RSA");
		privateKeyString = privateKeyString.replaceAll("\\n", "").replace("-----BEGIN PRIVATE KEY-----", "").replace("-----END PRIVATE KEY-----", "");
		PKCS8EncodedKeySpec keySpecPKCS8 = new PKCS8EncodedKeySpec(CommonUtil.decodeFromBase64String(privateKeyString));
		privateKey = kf.generatePrivate(keySpecPKCS8);

		// Decrypt data using private key
		byte[] encryptedBytes = CommonUtil.decodeFromBase64String(encryptedData);
		byte[] decryptedData = null;
		Cipher cipher = Cipher.getInstance("RSA/ECB/PKCS1Padding");
		cipher.init(Cipher.DECRYPT_MODE, privateKey);
		decryptedData = cipher.doFinal(encryptedBytes);
		return new String(decryptedData);
	}

}
