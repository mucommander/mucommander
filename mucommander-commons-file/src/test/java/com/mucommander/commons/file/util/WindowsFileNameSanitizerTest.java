package com.mucommander.commons.file.util;

import org.junit.Assert;
import org.testng.annotations.Test;

public class WindowsFileNameSanitizerTest {

    @Test
    public void sanitizeFileName_whenCalledWithIllegalChars_thenSanitizeCorrectly() {
        String input = "report<final>*version?.txt";
        String expected = "report%3Cfinal%3E%2Aversion%3F.txt";

        Assert.assertEquals(expected, WindowsFileNameSanitizer.sanitizeFileName(input));
    }

}
