package com.github.gabrielortiz00.byp_costing_jdeploy.utils;

import java.security.MessageDigest;

/** hashing utility class for login system **/

public class HashUtil {

    public static String hashSHA256(String plaintext) throws Exception {
        //get instance of sha-256 md algorithm
        MessageDigest md = MessageDigest.getInstance("SHA-256");
        //convert string to bytes
        byte[] hashedBytes = md.digest(plaintext.getBytes("UTF-8"));

        //convert bytes to hexadecimal
        StringBuilder sb = new StringBuilder();
        for (byte b : hashedBytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

}