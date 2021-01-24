package com.android.app2faces;

import android.util.Base64;

import java.nio.charset.StandardCharsets;
import java.security.InvalidAlgorithmParameterException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

import javax.crypto.BadPaddingException;
import javax.crypto.Cipher;
import javax.crypto.IllegalBlockSizeException;
import javax.crypto.NoSuchPaddingException;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;

public class Crypto {

    /**
     * Compute sha256 digest of a string
     *
     * @param   message to be hashed
     * @return  Base64 representation of hashed string
     */
    public static String sha256(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("SHA-256");
            byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * Compute md5 digest of a string
     *
     * @param   message to be hashed
     * @return  Base64 representation of hashed string
     */
    public static String md5(String message) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] hash = md.digest(message.getBytes(StandardCharsets.UTF_8));
            return Base64.encodeToString(hash, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        }

        return null;
    }

    /**
     * @param key     Base64 representation of 256bit key
     * @param iv      Base64 representation of 128bit iv
     * @param message to encrypt
     * @return Base64 representation of encrypted string
     */
    public static String encryptString(String key, String iv, String message) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            SecretKeySpec keySpec = new SecretKeySpec(Base64.decode(key, Base64.DEFAULT), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(Base64.decode(iv, Base64.DEFAULT), 0, 16);

            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);
            byte[] encrypted = cipher.doFinal(message.getBytes());

            return Base64.encodeToString(encrypted, Base64.DEFAULT);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | IllegalBlockSizeException | NoSuchPaddingException | InvalidKeyException | BadPaddingException e) {
            e.printStackTrace();
        }
        return "";
    }

    /**
     * @param key       Base64 representation of 256bit key
     * @param iv        Base64 representation of 128bit iv
     * @param encrypted Base64 representation of encrypted string to be decrypt
     * @return decrypted string
     */
    public static String decryptString(String key, String iv, String encrypted) {
        try {
            Cipher cipher = Cipher.getInstance("AES/CBC/PKCS5Padding");

            SecretKeySpec keySpec = new SecretKeySpec(Base64.decode(key, Base64.DEFAULT), "AES");
            IvParameterSpec ivSpec = new IvParameterSpec(Base64.decode(iv, Base64.DEFAULT), 0, 16);

            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);
            byte[] decstr = cipher.doFinal(Base64.decode(encrypted, Base64.DEFAULT));

            return new String(decstr);
        } catch (NoSuchAlgorithmException | InvalidAlgorithmParameterException | NoSuchPaddingException | BadPaddingException | IllegalBlockSizeException | InvalidKeyException e) {
            e.printStackTrace();
        }
        return "";
    }
}
