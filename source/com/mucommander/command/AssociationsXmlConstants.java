package com.mucommander.command;

/**
 * Defines the structure of a custom associations XML file.
 * <p>
 * This interface is only meant as a convenient way of sharing the XML
 * file format between the {@link AssociationWriter} and {@link AssociationReader}. It will be removed
 * at bytecode optimisation time.
 * </p>
 * <p>
 * Associations XML files must match the following DTD:
 * <pre>
 * &lt;!ELEMENT associations (association*)&gt;
 * 
 * &lt;!ELEMENT association EMPTY&gt;
 * &lt;!ATTLIST association command CDATA    #REQUIRED&gt;
 * &lt;!ATTLIST association mask    CDATA    #REQUIRED&gt;
 * &lt;!ATTLIST association read    (yes|no) #IMPLIED&gt;
 * &lt;!ATTLIST association write   (yes|no) #IMPLIED&gt;
 * &lt;!ATTLIST association execute (yes|no) #IMPLIED&gt;
 * </pre>
 * Where:
 * <ul>
 *  <li>
 *    <i>command</i> must be the alias of a command defined in the {@link CommandsXmlConstants commands file}.
 *    This command will be executed when a file matches the association.
 *  </li>
 *  <li>
 *    <i>mask</i> is a regular expression that will be applied to file names. If a file name matches it, and the file's permissions
 *    match the filter's permissions mask, then the associated command will be executed.
 *  </li>
 *  <li>
 *    <i>read</i> indicates whether a file's read flag should be set to be matched. This attribute can be ignored, in which case
 *    no matching will be performed on files' read flags.
 *  </li>
 *  <li>
 *    <i>write</i> indicates whether a file's write flag should be set to be matched. This attribute can be ignored, in which case
 *    no matching will be performed on files' write flags.
 *  </li>
 *  <li>
 *    <i>execute</i> indicates whether a file's execute flag should be set to be matched. This attribute can be ignored, in which case
 *    no matching will be performed on files' execute flags.
 *  </li>
 * </ul>
 * </p>
 * @see AssociationReader
 * @see AssociationWriter
 * @author Nicolas Rinaudo
 */
interface AssociationsXmlConstants {
    // - XML elements ----------------------------------------------------------
    // -------------------------------------------------------------------------
    /** Root element. */
    public static final String ELEMENT_ROOT        = "associations";
    /** Custom association definition element. */
    public static final String ELEMENT_ASSOCIATION = "association";



    // - Custom association structure ------------------------------------------
    // -------------------------------------------------------------------------
    /** Name of the attribute containing the alias of the command to execute in this association. */
    public static final String ARGUMENT_COMMAND    = "command";
    /** Name of the attribute containing the mask against which filenames are matched. */
    public static final String ARGUMENT_MASK       = "mask";
    /** Name of the attribute containing the association's <i>read</i> permission filter. */
    public static final String ARGUMENT_READABLE   = "read";
    /** Name of the attribute containing the association's <i>write</i> permission filter. */
    public static final String ARGUMENT_WRITABLE   = "write";
    /** Name of the attribute containing the association's <i>execute</i> permission filter. */
    public static final String ARGUMENT_EXECUTABLE = "execute";
    /** <i>Yes</i> value for permission filters. */
    public static final String VALUE_YES           = "yes";
    /** <i>No</i> value for permission filters. */
    public static final String VALUE_NO            = "no";
}
