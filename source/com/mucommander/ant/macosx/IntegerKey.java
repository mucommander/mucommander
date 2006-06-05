package com.mucommander.ant.macosx;

/**
 * Ant representation of an <code>integer</code> key.
 * <h3>Description</h3>
 * Declares an integer key in the <code>Info.plist</code> file.
 * <p>
 * </p>
 * <h3>Parameters</h3>
 * <p>
 * <table border="1" cellpadding="2" cellspacing="0">
 *   <tr>
 *     <td valign="top"><b>Attribute</b></td>
 *     <td valign="top"><b>Description</b></td>
 *     <td valign="top"><b>Required</b></td>
 *   </tr>
 *   <tr>
 *     <td valign="top">{@link com.mucommander.ant.macosx.NamedInfoElement#setName(String) name}</td>
 *     <td valign="top">
 *       The integer key's name. This can be any string, although good practice has it that it should
 *       not contain any whitespace character, and that each 'word' in the name should start with an
 *       upper case letter.
 *     </td>
 *     <td valign="top">Yes</td>
 *   </tr>
 *   <tr>
 *     <td valign="top">{@link #setValue(int) value}</td>
 *     <td valign="top">
 *       The integer property's value. This can be any signed or unsigned
 *       base 10 integer.
 *     </td>
 *     <td valign="top">Yes</td>
 *   </tr>
 * </table>
 * </p>
 * <h3>Examples</h3>
 * <blockquote>
 *   <pre>
 * &lt;integer name=&quot;MuCommanderExample&quot; value=&quot;10&quot;/&gt;
 *   </pre>
 * </blockquote>
 * <p>
 * creates an integer key named MuCommanderExample with a value of 10.
 * This will generate the following entry in the <code>Info.plist</code> file:
 * </p>
 * <blockquote>
 *   <pre>
 * &lt;key&gt;MuCommanderExample&lt;/key&gt;
 * &lt;integer&gt;10&lt;/integer&gt;
 *   </pre>
 * </blockquote>
 * 
 * @author Nicolas Rinaudo
 */
public class IntegerKey extends NamedInfoElement {
    /**
     * Creates an integer key.
     */
    public IntegerKey() {}

    /**
     * Sets the value of the integer key.
     * @param i value of the integer key.
     */
    public void setValue(int i) {setValue(new IntegerValue(i));}
}
