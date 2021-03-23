package com.mucommander.ui.main.tabs;

import java.io.File;

import com.mucommander.auth.CredentialsManager;
import com.mucommander.auth.CredentialsMapping;
import com.mucommander.commons.file.AbstractFile;
import com.mucommander.commons.file.AuthException;
import com.mucommander.commons.file.FileFactory;
import com.mucommander.commons.file.FileURL;
import com.mucommander.core.LocalLocationHistory;
import com.mucommander.ui.dialog.auth.AuthDialog;
import com.mucommander.ui.main.WindowManager;
import com.mucommander.ui.main.frame.MainFrameBuilder;

public class ConfFileTableTab extends FileTableTab {

	private boolean lock;
	private FileURL location;
	private String title;
	private AbstractFile fileToSelect;

	public ConfFileTableTab(String location) {
	    this.location = getInitialAbstractPaths(location);
	}

	public ConfFileTableTab(FileURL location) {
	    this.location = location;
	}

	public ConfFileTableTab(boolean lock, FileURL location, String title) {
		this.lock = lock;
		this.location = location;
		this.title = title;
	}

	@Override
	public boolean isLocked() {
		return lock;
	}

	@Override
	public FileURL getLocation() {
		return location;
	}

	@Override
	public String getTitle() {
		return title;
	}

	@Override
	public void setLocation(FileURL location) {
		throw new UnsupportedOperationException("cannot change location of configuration tab");
	}

	@Override
	public void setLocked(boolean locked) {
		throw new UnsupportedOperationException("cannot lock configuration tab");
	}

	@Override
	public void setTitle(String title) {
		throw new UnsupportedOperationException("cannot change title of configuration tab");
	}

	@Override
	public LocalLocationHistory getLocationHistory() {
		return null;
	}

	/**
	 * Returns a valid initial abstract path for the specified frame.
	 * <p>
	 * This method does its best to interpret <code>path</code> properly, or to fail
	 * politely if it can't. This means that:<br/>
	 * - we first try to see whether <code>path</code> is a legal, existing URI.<br/>
	 * - if it's not, we check whether it might be a legal local, existing file path.<br/>
	 * - if it's not, we'll just use the default initial path for the frame.<br/>
	 * - if <code>path</code> is browsable (eg directory, archive, ...), use it as is.<br/>
	 * - if it's not, use its parent.<br/>
	 * - if it does not have a parent, use the default initial path for the frame.<br/>
	 * </p>
	 *
	 * @param path            path to the folder we want to open in <code>frame</code>.
	 * @param folderPanelType identifier of the panel we want to compute the path for (either {@link com.mucommander.ui.main.FolderPanel.FolderPanelType.LEFT} or
	 *                        {@link #@link com.mucommander.ui.main.FolderPanel.FolderPanelType.RIGHT}).
	 * @return our best shot at what was actually requested.
	 */
	private FileURL getInitialAbstractPaths(String path) {
	    // This is one of those cases where a null value actually has a proper meaning.
	    if (path == null)
	        return MainFrameBuilder.getHomeFolder().getURL();

	    // Tries the specified path as-is.
	    AbstractFile file;
	    CredentialsMapping newCredentialsMapping;

	    while (true) {
	        try {
	            file = FileFactory.getFile(path, true);
	            if (!file.exists())
	                file = null;
	            break;
	        }
	        // If an AuthException occurred, gets login credential from the user.
	        catch (Exception e) {
	            if (e instanceof AuthException) {
	                // Prompts the user for a login and password.
	                AuthException authException = (AuthException) e;
	                FileURL url = authException.getURL();
	                AuthDialog authDialog = new AuthDialog(WindowManager.getCurrentMainFrame(), url, true, authException.getMessage());
	                authDialog.showDialog();
	                newCredentialsMapping = authDialog.getCredentialsMapping();
	                if (newCredentialsMapping != null) {
	                    // Use the provided credentials
	                    CredentialsManager.authenticate(url, newCredentialsMapping);
	                    path = url.toString(true);
	                }
	                // If the user cancels, we fall back to the default path.
	                else {
	                    return MainFrameBuilder.getHomeFolder().getURL();
	                }
	            } else {
	                file = null;
	                break;
	            }
	        }
	    }

	    // If the specified path does not work out,
	    if (file == null)
	        // Tries the specified path as a relative path.
	        if ((file = FileFactory.getFile(new File(path).getAbsolutePath())) == null || !file.exists())
	            // Defaults to home.
	            return MainFrameBuilder.getHomeFolder().getURL();

	    // If the specified path is a non-browsable, uses its parent.
	    if (!file.isBrowsable()) {
	        fileToSelect = file;
	        // This is just playing things safe, as I doubt there might ever be a case of
	        // a file without a parent directory.
	        if ((file = file.getParent()) == null)
	            return MainFrameBuilder.getHomeFolder().getURL();
	    }

	    return file.getURL();
	}

	public AbstractFile getFileToSelect() {
	    return fileToSelect;
	}
}
