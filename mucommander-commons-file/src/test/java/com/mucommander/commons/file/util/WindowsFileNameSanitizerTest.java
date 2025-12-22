package com.mucommander.commons.file.util;

import static org.junit.jupiter.api.Assertions.*;
import org.testng.annotations.Test;

public class WindowsFileNameSanitizerTest {

    @Test
    public void sanitizeFilename_whenCalledWithIllegalChars_thenSanitizeCorrectly() {
        String input = "report<final>*version?.txt";
        String expected = "report_final__version_.txt";

        assertEquals(expected, WindowsFilenameSanitizer.sanitizeFileName(input));
    }

}
