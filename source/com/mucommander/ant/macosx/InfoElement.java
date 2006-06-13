package com.mucommander.ant.macosx;

import com.mucommander.xml.writer.XmlWriter;
import org.apache.tools.ant.BuildException;

/**
 * Contract for all Info.plist elements.
 * <p>
 * This interface's sole purpose is to unify the property list writing process.
 * </p>
 * <p>
 * Since property list elements vary widly in structure, it is all but impossible
 * for us to offer generic Ant hooks. The {@link com.mucommander.ant.macosx.NamedInfoElement}
 * class generalises the notion of element name, but values need to be set on a case by case basis.<br/>
 * While it is not enforced programatically, good practice requires such Ant hooks as are used to set
 * a value to be named, rather logically, <code>value</code>. Implementations of this interface
 * are thus expected to have a public <code>setValue</code> method used for Ant to, well, set
 * element's value.
 * </p>
 * @author Nicolas Rinaudo
 */
interface InfoElement {
    /**
     * Writes the content of this element to the specified XmlWriter.
     * @param     out            where to write the content of this element.
     * @exception BuildException thrown if anything wrong occurs.
     */
    void write(XmlWriter out) throws BuildException;
}
