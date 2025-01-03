/*
 * This file is part of Neon-Eleanor project
 *
 * This is proprietary software. See the EULA file distributed with
 * this project for additional information regarding copyright ownership.
 *
 * Copyright (c) 2011-2013, Neon-Eleanor Team. All rights reserved.
 */
package com.ne.gs.services;

import com.ne.gs.configs.main.NameConfig;

public final class NameRestrictionService {

    private static final String ENCODED_BAD_WORD = "----";
    private static String[] forbiddenSequences;
    private static String[] forbiddenByClient;

    public static boolean isValidName(String name) {
        return NameConfig.CHAR_NAME_PATTERN.matcher(name).matches();
    }

    public static boolean isForbiddenWord(String name) {
        return (isForbiddenByClient(name)) || (isForbiddenBySequence(name));
    }

    private static boolean isForbiddenByClient(String name) {
        if ((!NameConfig.NAME_FORBIDDEN_ENABLE) || (NameConfig.NAME_FORBIDDEN_CLIENT.equals(""))) {
            return false;
        }
        if ((forbiddenByClient == null) || (forbiddenByClient.length == 0)) {
            forbiddenByClient = NameConfig.NAME_FORBIDDEN_CLIENT.split(",");
        }
        for (String s : forbiddenByClient) {
            if (name.equalsIgnoreCase(s)) {
                return true;
            }
        }
        return false;
    }

    private static boolean isForbiddenBySequence(String name) {
        if (NameConfig.NAME_SEQUENCE_FORBIDDEN.equals("")) {
            return false;
        }
        if ((forbiddenSequences == null) || (forbiddenSequences.length == 0)) {
            forbiddenSequences = NameConfig.NAME_SEQUENCE_FORBIDDEN.toLowerCase().split(",");
        }
        for (String s : forbiddenSequences) {
            if (name.toLowerCase().contains(s)) {
                return true;
            }
        }
        return false;
    }

    public static String filterMessage(String message) {
        for (String word : message.split(" ")) {
            if (isForbiddenWord(word)) {
                message.replace(word, ENCODED_BAD_WORD);
            }
        }
        return message;
    }
}
