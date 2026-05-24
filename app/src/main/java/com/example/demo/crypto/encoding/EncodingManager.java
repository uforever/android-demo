package com.example.demo.crypto.encoding;

import android.util.Base64;

import org.lsposed.lsparanoid.Obfuscate;

import java.net.URLDecoder;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

@Obfuscate
public class EncodingManager {

    public static String base64Encode(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        return Base64.encodeToString(bytes, Base64.NO_WRAP);
    }

    public static String base64Decode(String input) {
        byte[] bytes = Base64.decode(input, Base64.NO_WRAP);
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public static String hexEncode(String input) {
        byte[] bytes = input.getBytes(StandardCharsets.UTF_8);
        StringBuilder sb = new StringBuilder();
        for (byte b : bytes) {
            sb.append(String.format("%02x", b));
        }
        return sb.toString();
    }

    public static String hexDecode(String hex) {
        int len = hex.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(hex.charAt(i), 16) << 4)
                    + Character.digit(hex.charAt(i + 1), 16));
        }
        return new String(data, StandardCharsets.UTF_8);
    }

    public static String urlEncode(String input) {
        try {
            return URLEncoder.encode(input, StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }

    public static String urlDecode(String input) {
        try {
            return URLDecoder.decode(input, StandardCharsets.UTF_8.name());
        } catch (java.io.UnsupportedEncodingException e) {
            throw new RuntimeException(e);
        }
    }
}
