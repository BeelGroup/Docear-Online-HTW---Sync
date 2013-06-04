package org.docear.syncdaemon.hashing;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.security.DigestInputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import org.apache.commons.io.IOUtils;

public class SHA2 implements HashAlgorithm {

	@Override
	public String generate(File file) throws FileNotFoundException {
		return getFileCheckSum(new DigestInputStream(new FileInputStream(file), createMessageDigest()));
	}

	/**
	 * taken from http://www.mkyong.com/java/java-sha-hashing-example/
	 * 
	 * @return
	 */
	private static String getFileCheckSum(DigestInputStream inputStream) {
		final MessageDigest md = inputStream.getMessageDigest();
		final byte[] mdbytes = md.digest();

		// convert the byte to hex format
		final StringBuffer sb = new StringBuffer();
		for (int i = 0; i < mdbytes.length; i++) {
			sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
		}

		IOUtils.closeQuietly(inputStream);
		return sb.toString();
	}
	
	private static MessageDigest createMessageDigest() {
		try {
			return MessageDigest.getInstance("SHA-512");
		} catch (NoSuchAlgorithmException e) {
			throw new RuntimeException("Invalid Crypto algorithm! ", e);
		}
	}
}
