package org.docear.syncdaemon.hashing;

import org.apache.commons.io.IOUtils;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class SHA2 implements HashAlgorithm {

    /**
     * taken from http://www.mkyong.com/java/java-sha-hashing-example/
     *
     * @return
     * @throws IOException
     */
    private static String getFileCheckSum(File file, MessageDigest md) throws IOException {
        InputStream in = null;
        StringBuffer sb = new StringBuffer();
        try {
            in = new FileInputStream(file);

            byte[] dataBytes = new byte[1024];

            int nread = 0;
            while ((nread = in.read(dataBytes)) != -1) {
                md.update(dataBytes, 0, nread);
            }
            ;
            byte[] mdbytes = md.digest();

            //convert the byte to hex format method 1
            for (int i = 0; i < mdbytes.length; i++) {
                sb.append(Integer.toString((mdbytes[i] & 0xff) + 0x100, 16).substring(1));
            }

        } finally {
            IOUtils.closeQuietly(in);
        }
        return sb.toString();
    }

    private static MessageDigest createMessageDigest() {
        try {
            return MessageDigest.getInstance("SHA-512");
        } catch (NoSuchAlgorithmException e) {
            throw new RuntimeException("Invalid Crypto algorithm! ", e);
        }
    }

    @Override
    public String generate(File file) throws IOException {
        return getFileCheckSum(file, createMessageDigest());
    }

    @Override
    public boolean isValidHash(String hash) {
        return hash.length() == 64;
    }
}
