package com.langa.backend.common.utils;

import java.math.BigInteger;
import java.util.UUID;

public class KeyGenerator {

    private static final String BASE62 = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
    private static final String APP_PREFIX = "APP-";
    private static final String USER_PREFIX = "U-";
    private static final String TEAM_PREFIX = "T-";

    private KeyGenerator() {}

    public static String generateAppKey() {
        UUID uuid = UUID.randomUUID();
        StringBuilder sb = toBase62(uuid);
        return APP_PREFIX.concat(sb.reverse().toString());
    }

    public static String generateAccountKey(String email) {
        UUID uuid = UUID.nameUUIDFromBytes(email.getBytes());
        StringBuilder sb = toBase62(uuid);
        return USER_PREFIX.concat(sb.reverse().toString());
    }

    public static String generateTeamKey(String teamName, String owner) {
        UUID uuid = UUID.nameUUIDFromBytes(teamName.concat(owner).getBytes());
        StringBuilder sb = toBase62(uuid);
        return TEAM_PREFIX.concat(sb.reverse().toString());
    }

    private static StringBuilder toBase62(UUID uuid) {
        BigInteger number = new BigInteger(uuid.toString().replace("-", ""), 16);
        StringBuilder sb = new StringBuilder();
        while (number.compareTo(BigInteger.ZERO) > 0) {
            sb.append(BASE62.charAt(number.mod(BigInteger.valueOf(62)).intValue()));
            number = number.divide(BigInteger.valueOf(62));
        }
        return sb;
    }


}

