
package com.mucommander.bookmark;

import com.mucommander.file.FileURL;


/**
 * This class represents a bookmark, which is a simple name/url pair.
 *
 * @author Maxence Bernard
 */
public class Bookmark {

	private String name;
	private FileURL fileURL;
	

	/**
	 * Creates a new Bookmark.
	 *
	 * @param name Name given to this bookmark
	 * @param fileURL URL of the file this bookmark points to.
	 */
	public Bookmark(String name, FileURL fileURL) {
		this.name = name;
		this.fileURL = fileURL;
	}


	/**
	 * Returns this bookmark's name.
	 */
	private String getName() {
		return name;
	}


	/**
	 * Returns this bookmark's file URL.
	 */
	private FileURL getURL() {
		return fileURL;
	}
}