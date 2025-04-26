package com.mucommander.commons.file.util;

import org.junit.Assert;
import org.testng.annotations.Test;

public class WindowsFileNameSanitizerTest {

    @Test
    public void sanitizeFilename_whenCalledWithIllegalChars_thenSanitizeCorrectly() {
        String input = "report<final>*version?.txt";
        String expected = "report_final__version_.txt";

        Assert.assertEquals(expected, WindowsFilenameSanitizer.sanitizeFileName(input));
    }

}
