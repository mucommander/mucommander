
package com.mucommander.file;


/**
 * Abstract class denoting a remote file. This class should be extended instead of AbstractFile whenever the file
 * is located remotely, that is somewhere with a substantial latency.
 *
 * @author Maxence Bernard
 */
public abstract class RemoteFile extends AbstractFile {
}
