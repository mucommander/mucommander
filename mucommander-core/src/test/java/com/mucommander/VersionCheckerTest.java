/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander;

import org.testng.annotations.Test;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import static org.testng.Assert.*;

/**
 * Unit tests for {@link VersionChecker}.
 * Uses a mock GitHubApiClient to avoid making actual HTTP calls.
 *
 * @author Arik Hadas
 */
public class VersionCheckerTest {

    /**
     * Mock implementation of {@link VersionChecker.ReadFromURL} that returns predefined responses.
     */
    private static class MockReadFromURL implements VersionChecker.ReadFromURL {
        private final Map<String, String> responses = new HashMap<>();

        void addResponse(String url, String response) {
            responses.put(url, response);
        }

        @Override
        public String apply(String url) throws IOException {
            String response = responses.get(url);
            if (response == null) {
                throw new IOException("No mock response configured for URL: " + url);
            }
            return response;
        }
    }

    @Test
    public void testParseStableRelease() throws Exception {
        MockReadFromURL mockReader = new MockReadFromURL();

        String releaseJson = "{" +
                "\"tag_name\": \"1.5.0\"," +
                "\"target_commitish\": \"abc1234\"," +
                "\"html_url\": \"https://github.com/mucommander/mucommander/releases/tag/1.5.0\"," +
                "\"published_at\": \"2024-04-15T09:21:43Z\"" +
                "}";

        mockReader.addResponse("https://api.github.com/repos/mucommander/mucommander/releases/latest", releaseJson);

        VersionChecker checker = VersionChecker.getInstance(false, mockReader);
        assertNotNull(checker);

        String downloadURL = checker.getDownloadURL();
        assertNotNull(downloadURL);
        assertEquals(downloadURL, "https://github.com/mucommander/mucommander/releases/tag/1.5.0");
    }

    @Test
    public void testParseNightlyRelease() throws Exception {
        MockReadFromURL mockReader = new MockReadFromURL();

        String tagRefJson = "{" +
                "\"ref\": \"refs/tags/nightly\"," +
                "\"object\": {" +
                "\"sha\": \"def5678901234567890123456789012345678901\"," +
                "\"type\": \"commit\"" +
                "}" +
                "}";

        String releaseJson = "{" +
                "\"tag_name\": \"nightly\"," +
                "\"target_commitish\": \"master\"," +
                "\"html_url\": \"https://github.com/mucommander/mucommander/releases/tag/nightly\"," +
                "\"published_at\": \"2024-03-28T12:34:56Z\"" +
                "}";

        mockReader.addResponse("https://api.github.com/repos/mucommander/mucommander/git/ref/tags/nightly", tagRefJson);
        mockReader.addResponse("https://api.github.com/repos/mucommander/mucommander/releases/tags/nightly", releaseJson);

        VersionChecker checker = VersionChecker.getInstance(true, mockReader);
        assertNotNull(checker);

        String downloadURL = checker.getDownloadURL();
        assertNotNull(downloadURL);
        assertEquals(downloadURL, "https://github.com/mucommander/mucommander/releases/tag/nightly");
    }

    @Test
    public void testNoNewVersionByHash() throws Exception {
        MockReadFromURL mockReader = new MockReadFromURL();

        String releaseJson = "{" +
                "\"tag_name\": \"1.5.0\"," +
                "\"target_commitish\": \"abc1234\"," +
                "\"html_url\": \"https://github.com/mucommander/mucommander/releases/tag/1.5.0\"," +
                "\"published_at\": \"2024-04-15T09:21:43Z\"" +
                "}";

        mockReader.addResponse("https://api.github.com/repos/mucommander/mucommander/releases/latest", releaseJson);

        // Note: This test assumes BUILD_NUMBER is not "snapshot" (but a stable version)
        VersionChecker checker = VersionChecker.getInstance(false, mockReader);
        assertFalse(checker.isNewVersionAvailable("abc1234", "20240401"));
    }

    @Test
    public void testNewVersion() throws Exception {
        MockReadFromURL mockReader = new MockReadFromURL();

        String releaseJson = "{" +
                "\"tag_name\": \"1.5.0\"," +
                "\"target_commitish\": \"abc1234\"," +
                "\"html_url\": \"https://github.com/mucommander/mucommander/releases/tag/1.5.0\"," +
                "\"published_at\": \"2024-04-15T09:21:43Z\"" +
                "}";

        mockReader.addResponse("https://api.github.com/repos/mucommander/mucommander/releases/latest", releaseJson);

        // Note: This test assumes BUILD_NUMBER is not "snapshot" (but a stable version)
        VersionChecker checker = VersionChecker.getInstance(false, mockReader);
        assertTrue(checker.isNewVersionAvailable("abc1235", "20240401"));
    }

    @Test
    public void testNoNewVersionByDate() throws Exception {
        MockReadFromURL mockReader = new MockReadFromURL();

        String releaseJson = "{" +
                "\"tag_name\": \"1.5.0\"," +
                "\"target_commitish\": \"abc1234\"," +
                "\"html_url\": \"https://github.com/mucommander/mucommander/releases/tag/1.5.0\"," +
                "\"published_at\": \"2024-04-15T09:21:43Z\"" +
                "}";

        mockReader.addResponse("https://api.github.com/repos/mucommander/mucommander/releases/latest", releaseJson);

        // Note: This test assumes BUILD_NUMBER is not "snapshot" (but a stable version)
        VersionChecker checker = VersionChecker.getInstance(false, mockReader);
        assertFalse(checker.isNewVersionAvailable("abc1235", "20240416"));
    }

    @Test(expectedExceptions = Exception.class)
    public void testHandleIOException() throws Exception {
        MockReadFromURL mockReader = new MockReadFromURL();
        // Don't add any responses - this will cause an IOException

        // This should throw an Exception wrapping the IOException
        VersionChecker.getInstance(false, mockReader);
    }

    @Test
    public void testHandleNullJsonResponse() throws Exception {
        MockReadFromURL mockReader = new MockReadFromURL();

        // Return null/invalid JSON
        mockReader.addResponse("https://api.github.com/repos/mucommander/mucommander/releases/latest", "null");

        VersionChecker checker = VersionChecker.getInstance(false, mockReader);

        // Should handle gracefully - getDownloadURL should return null
        assertNull(checker.getDownloadURL());
    }
}
