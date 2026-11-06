package com.group.exam.common;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * 密码加密工具：MD5(明文 + 固定盐值)。
 * 与 exam_db.sql 初始数据使用的加密方式完全一致。
 */
public final class PasswordUtil {

    /** 盐值，必须与 exam_db.sql 中一致 */
    private static final String SALT = "group_exam_salt";

    private PasswordUtil() {
    }

    /**
     * 加密明文密码。
     *
     * @param rawPassword 明文密码
     * @return 32 位 MD5 十六进制摘要
     */
    public static String encrypt(String rawPassword) {
        if (rawPassword == null) {
            rawPassword = "";
        }
        return md5Hex(rawPassword + SALT);
    }

    /**
     * 校验明文密码与密文是否匹配。
     */
    public static boolean verify(String rawPassword, String encrypted) {
        return encrypt(rawPassword).equalsIgnoreCase(encrypted);
    }

    private static String md5Hex(String input) {
        try {
            MessageDigest md = MessageDigest.getInstance("MD5");
            byte[] digest = md.digest(input.getBytes(StandardCharsets.UTF_8));
            StringBuilder sb = new StringBuilder();
            for (byte b : digest) {
                String hex = Integer.toHexString(b & 0xff);
                if (hex.length() == 1) {
                    sb.append('0');
                }
                sb.append(hex);
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("当前 JDK 不支持 MD5 算法", e);
        }
    }
}
