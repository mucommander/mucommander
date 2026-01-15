package com.mucommander.commons.file.util;

public enum FileComparatorModeEnum {

    LEXICOGRAPHIC,
    NATURAL;

    public static FileComparatorModeEnum getMode(boolean useLexicographicSort) {
        return useLexicographicSort ? LEXICOGRAPHIC : NATURAL;
    }

}
