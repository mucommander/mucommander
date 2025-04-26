package com.mucommander.commons.file.util;

import java.util.Set;

public class WindowsFileNameSanitizer {

    private static final Set<Character> ILLEGAL_CHARS = Set.of(
            '<', '>', ':', '"', '/', '\\', '|', '?', '*', '`'
    );

    public static String sanitizeFileName(String input) {
        StringBuilder sanitized = new StringBuilder();

        for (char c : input.toCharArray()) {
            if (ILLEGAL_CHARS.contains(c)) {
                sanitized.append(String.format("%%%02X", (int) c));
            } else {
                sanitized.append(c);
            }
        }

        return sanitized.toString();
    }

}
