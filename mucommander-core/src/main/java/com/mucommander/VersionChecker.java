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

import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.commons.io.StreamUtils;
import net.minidev.json.JSONValue;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

/**
 * Retrieves information about the latest release of muCommander from GitHub.
 * <p>
 * This class uses the GitHub REST API to check for the latest release from the mucommander/mucommander repository.
 * For nightly builds, it checks for the latest pre-release tagged as "nightly".
 * For stable builds, it checks for the latest stable release.
 * </p>
 * <p>
 * Checking for new releases is a fairly straightforward process:
 * <pre>
 * VersionChecker version;
 *
 * try {
 *     version = VersionChecker.getInstance();
 *     if(version.isNewVersionAvailable())
 *         System.out.println("A new version of muCommander is available");
 *     else
 *         System.out.println("You've got the latest muCommander version");
 *    }
 * catch(Exception e) {System.err.println("An error occurred.");}
 * </pre>
 * </p>
 * <p>
 * Version comparison works as follows:
 * <ol>
 *   <li>First, the commit SHA of the latest release is compared with the current build's commit ID
 *       ({@link com.mucommander.RuntimeConstants#BUILD_NUMBER})</li>
 *   <li>If they match, the build is up to date</li>
 *   <li>If they differ, the build date of the latest release is compared with the current build's date
 *       ({@link com.mucommander.RuntimeConstants#BUILD_DATE})</li>
 *   <li>If the remote build date is newer, a new version is available</li>
 * </ol>
 * This approach works for both nightly and stable builds.
 * </p>
 *
 * @author Maxence Bernard, Nicolas Rinaudo, Arik Hadas
 */
public class VersionChecker {
    private static final Logger LOGGER = LoggerFactory.getLogger(VersionChecker.class);

    // - Constants --------------------------------------------------------------
    // --------------------------------------------------------------------------
    /** GitHub API URL for latest stable release. */
    private static final String GITHUB_LATEST_RELEASE_URL = "https://api.github.com/repos/mucommander/mucommander/releases/latest";
    /** GitHub API URL for nightly release by tag. */
    private static final String GITHUB_NIGHTLY_RELEASE_URL = "https://api.github.com/repos/mucommander/mucommander/releases/tags/nightly";
    /** GitHub API URL for getting tag reference. */
    private static final String GITHUB_TAG_REF_URL =
            "https://api.github.com/repos/mucommander/mucommander/git/ref/tags/nightly";

    // - Instance fields --------------------------------------------------------
    // --------------------------------------------------------------------------
    /** Tag name of the latest release. */
    private String tagName;
    /** Commit SHA of the latest release. */
    private String commitSha;
    /** Download URL for the release page. */
    private String htmlUrl;
    /** Date of the release in YYYYMMDD format. */
    private String releaseDate;

    @FunctionalInterface
    interface ReadFromURL {

        /**
         * Reads content from a URL.
         *
         * @param url
         *         the URL to read from
         * @return the response body as a string
         * @throws IOException
         *         if there was an error fetching the URL
         */
        String apply(String url) throws IOException;
    }

    // - Initialisation ---------------------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Creates a new version checker instance.
     */
    private VersionChecker() {
    }

    /**
     * Checks if the current build is a nightly build based on the {@link com.mucommander.RuntimeConstants#BUILD_NUMBER).
     *
     * @return true if this is a nightly build (snapshot), false if it's a stable build
     */
    private static boolean isNightlyBuild() {
        return "snapshot".equals(RuntimeConstants.BUILD_NUMBER);
    }

    /**
     * Retrieves information about the latest release from GitHub using the REST API.
     * For nightly builds, checks for the latest nightly pre-release.
     * For stable builds, checks for the latest stable release.
     *
     * @return a description of the latest release of muCommander.
     * @throws Exception
     *         thrown if any error happens while retrieving the remote version.
     */
    public static VersionChecker getInstance() throws Exception {
        return getInstance(isNightlyBuild(), url -> {
                FileURL fileURL = FileURL.getFileURL(url);
                AbstractFile file = FileFactory.getFile(fileURL);

                if (file == null || !file.exists()) {
                    throw new IOException("Could not access URL: " + url);
                }

                return StreamUtils.readAsString(file.getInputStream());
            });
    }

    /**
     * Retrieves information about the latest release from GitHub using the REST API.
     * Package-private method that allows injection of a custom {@link ReadFromURL} for testing.
     *
     * @param urlReader the reader to use for fetching release information
     * @return a description of the latest release of muCommander.
     * @throws Exception
     *         thrown if any error happens while retrieving the remote version.
     */
    static VersionChecker getInstance(boolean nightly, ReadFromURL urlReader) throws Exception {
        LOGGER.debug("Connecting to GitHub API to check for latest {} release", nightly ? "nightly" : "stable");

        try {
            VersionChecker instance = new VersionChecker();

            if (nightly) {
                // For nightly builds, get the commit SHA that the nightly tag points to
                String tagResponseBody = urlReader.apply(GITHUB_TAG_REF_URL);

                // Extract the commit SHA from the tag reference
                Map<String, Object> tagJson = (Map<String, Object>) JSONValue.parse(tagResponseBody);
                Map<String, Object> objectData = (Map<String, Object>) tagJson.get("object");
                String tagCommitSha = (String) objectData.get("sha");

                if (tagCommitSha == null) {
                    throw new Exception("Could not extract commit SHA from nightly tag");
                }

                LOGGER.debug("Nightly tag points to commit: {}", tagCommitSha);

                // Get the nightly release by tag using GitHub's /releases/tags/nightly endpoint
                String releaseResponseBody = urlReader.apply(GITHUB_NIGHTLY_RELEASE_URL);

                // Parse JSON response for the nightly release
                instance.parseRelease(releaseResponseBody);

                // Override the commitSha with the one from the tag reference
                instance.commitSha = tagCommitSha.substring(0, 7);

                LOGGER.debug("Found nightly release at commit: {}", instance.commitSha);
            } else {
                // For stable builds, get the latest stable release using GitHub's /releases/latest endpoint
                String releaseResponseBody = urlReader.apply(GITHUB_LATEST_RELEASE_URL);

                // Parse JSON response for the latest stable release
                instance.parseRelease(releaseResponseBody);

                LOGGER.debug("Found stable release: {}", instance.tagName);
            }

            return instance;

        } catch (IOException e) {
            LOGGER.debug("Failed to retrieve version information from GitHub", e);
            throw new Exception("Failed to check for updates", e);
        }
    }

    /**
     * Parses the JSON response from GitHub API to extract release information. Uses json-smart library for JSON
     * parsing. This method expects a single release object from either the /releases/latest or /releases/tags/{tag} endpoint.
     */
    private void parseRelease(String json) {
        Map<String, Object> release = (Map<String, Object>) JSONValue.parse(json);

        if (release == null) {
            LOGGER.warn("Could not parse release JSON");
            return;
        }

        // Extract release information
        this.tagName = (String) release.get("tag_name");
        this.commitSha = (String) release.get("target_commitish");
        this.htmlUrl = (String) release.get("html_url");

        LOGGER.debug("Found release: {}", this.tagName);
        LOGGER.debug("Commit SHA: {}", this.commitSha);
        LOGGER.debug("htmlUrl: {}", this.htmlUrl);

        // Extract published_at and format as YYYYMMDD
        String publishedAt = (String) release.get("published_at");
        if (publishedAt != null && publishedAt.length() >= 10) {
            // Format: 2024-03-28T12:34:56Z -> 20240328
            this.releaseDate =
                    publishedAt.substring(0, 4) + publishedAt.substring(5, 7) + publishedAt.substring(8, 10);
        }
    }

    // - Remote version information ---------------------------------------------
    // --------------------------------------------------------------------------

    /**
     * Checks whether a new version is available.
     * First compares commit hashes. If different, compares build dates to determine if remote is newer.
     *
     * @return <code>true</code> if a newer version is available, <code>false</code> otherwise.
     */
    public boolean isNewVersionAvailable() {
        return isNewVersionAvailable(RuntimeConstants.GIT_HASH, RuntimeConstants.BUILD_DATE);
    }

    boolean isNewVersionAvailable(String currentGitHash, String currentBuildDate) {
        if (currentGitHash == null || currentGitHash.equals("?")) {
            LOGGER.debug("Current build number is unknown, cannot determine if new version is available");
            return false;
        }

        if (commitSha == null) {
            LOGGER.debug("Latest commit SHA is null, cannot determine if new version is available");
            return false;
        }

        // Normalize commit SHAs to compare (use first 7 characters if available)
        String normalizedLatest = commitSha.length() >= 7 ? commitSha.substring(0, 7) : commitSha;
        String normalizedCurrent = currentGitHash.length() >= 7 ? currentGitHash.substring(0, 7) : currentGitHash;

        boolean sameCommit = normalizedLatest.equalsIgnoreCase(normalizedCurrent);

        LOGGER.debug("Current commit: {}, Latest commit: {}, Same commit: {}", normalizedCurrent, normalizedLatest, sameCommit);

        // If commits are the same, we're up to date
        if (sameCommit) {
            return false;
        }

        // Commits are different - compare build dates to determine which is newer
        if (releaseDate == null || releaseDate.isEmpty()) {
            LOGGER.debug("Latest release date is unknown, cannot determine if new version is available");
            return false;
        }

        if (currentBuildDate == null || currentBuildDate.equals("?")) {
            LOGGER.debug("Current build date is unknown, cannot determine if new version is available");
            return false;
        }

        // Compare dates (format: YYYYMMDD) as strings - lexicographic comparison works for this format
        int dateComparison = releaseDate.compareTo(currentBuildDate);

        LOGGER.debug("Current build date: {}, Latest release date: {}, Comparison: {}", currentBuildDate,  releaseDate, dateComparison);

        // Return true if the latest release date is newer than current build date
        return dateComparison > 0;
    }

    /**
     * Returns the URL at which the latest release can be viewed on GitHub.
     *
     * @return the URL to the latest release page.
     */
    public String getDownloadURL() {
        return htmlUrl;
    }
}
