/*
 * Copyright 2006-2017 ICEsoft Technologies Canada Corp.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the
 * License. You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an "AS
 * IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either
 * express or implied. See the License for the specific language
 * governing permissions and limitations under the License.
 */
package org.icepdf.core.pobjects.security;

/**
 * <p>Standard encryption has permissions associated with it which is defined
 * by a key in the encryption dictionary.  It is up to the viewer application
 * to respect these permissions.</p>
 * <p/>
 * <p>The value of the P key is an unsigned 32-bit integer containing a set of
 * flags specifying which access permissions should be granted when the document
 * is opened with user access. The below list shows the meanings of these flags.
 * Bit positions within the flag word are numbered from 1 (low-order) to 32
 * (high-order); a 1 bit in any position enables the corresponding access
 * permission. Which bits are meaningful, and in some cases how they are
 * interpreted, depends on the security handler's revision number (specified in
 * the encryption dictionary's R entry).</p>
 * <p/>
 * <table border="1" cellpadding="1" cellspacing="1" >
 * <tr>
 * <td><b> Bits</b></td>
 * <td><b> Meaning</b></td>
 * </tr>
 * <tr>
 * <td valign="top" >1-2</td>
 * <td>Reserved; must be 0.</td>
 * </tr>
 * <tr>
 * <td valign="top" > 3</td>
 * <td> (Revision 2) Print the document.<br />
 * (Revision 3) Print the document (possibly not at the highest
 * quality (possibly not at the highest quality level, depending on
 * whether bit 12 is also set).
 * </td>
 * </tr>
 * <tr>
 * <td valign="top" >4</td>
 * <td>Modify the contents of the document by operations other than those
 * controlled by bits 6, 9, and 11.</td>
 * </tr>
 * <tr>
 * <td valign="top" >5</td>
 * <td>(Revision 2) Copy or otherwise extract text and graphics from the
 * document, including extracting text and graphics (in support of
 * accessibility to disabled users or for other purposes). (Revision 3)
 * Copy or otherwise extract text and graphics from the document by
 * operations other than that controlled by bit 10.</td>
 * </tr>
 * <tr>
 * <td valign="top" >6</td>
 * <td>Add or modify text annotations, fill in interactive form fields,
 * and, if bit 4 is also set, create or modify interactive form fields
 * (including signature fields).</td>
 * </tr>
 * <tr>
 * <td valign="top" >7-8</td>
 * <td>Reserved; must be 1.</td>
 * </tr>
 * <tr>
 * <td valign="top" >9</td>
 * <td>(Revision 3 only) Fill in existing interactive form fields
 * (including signature fields), even if bit 6 is clear.</td>
 * </tr>
 * <tr>
 * <td valign="top" >10</td>
 * <td>(Revision 3 only) Extract text and graphics (in support of
 * accessibility to disabled users or for other purposes).</td>
 * </tr>
 * <tr>
 * <td valign="top" >11</td>
 * <td>(Revision 3 only) Assemble the document (insert, rotate, or delete
 * pages and create bookmarks or thumbnail images), even if bit 4 is
 * clear.</td>
 * </tr>
 * <tr>
 * <td valign="top" >12</td>
 * <td>(Revision 3 only) Print the document to a representation from which
 * a faithful digital copy of the PDF content could be generated. When
 * this bit is clear (and bit 3 is set), printing is limited to a
 * lowlevel representation of the appearance, possibly of degraded
 * quality.</td>
 * </tr>
 * <tr>
 * <td valign="top" >13-32</td>
 * <td>(Revision 3 only) Reserved; must be 1.</td>
 * </tr>
 * </table>
 * <br />
 * <b>Note:</b><br/>
 * PDF integer objects in fact are represented internally in signed
 * twos complement form. Since all the reserved high-order flag bits in the
 * encryption dictionary's P value are required to be 1, the value must be
 * specified as a negative integer. For example, assuming revision 2 of the
 * security handler, the value -44 allows printing and copying but disallows
 * modifying the contents and annotations.
 *
 * @since 1.1
 */
public class Permissions {

    // constants for parsing bits from P value
    // bit 3, 11111111111111111111000011000100
    private static final int PRINT_DOCUMENT_BIT_3 = 0xFFFFF0C4;
    // bit 4, 11111111111111111111000011001000
    private static final int MODIFY_DOCUMENT_BIT_4 = 0xFFFFF0C8;
    // bit 5, 11111111111111111111000011010000
    private static final int DATA_EXTRACTION_BIT_5 = 0xFFFFF0D0;
    // bit 6, 11111111111111111111000011100000
    private static final int MODIFY_TEXT_BIT_6 = 0xFFFFF0E0;
    // bit 9, 11111111111111111111000111000000
    private static final int MODIFY_FORMS_BIT_9 = 0xFFFFF1C0;
    // bit 10, 11111111111111111111001011000000
    private static final int ACCESSIBILITY_BIT_10 = 0xFFFFF2C0;
    // bit 11, 11111111111111111111010011000000
    private static final int ASSEMBLE_DOCUMENT_BIT_11 = 0xFFFFF4C0;
    // bit 12 11111111111111111111100011000000
    private static final int PRINT_QUALITY_BIT_12 = 0xFFFFF8C0;

    // Constants for retrieving permission values

    /**
     * Determine if printing of document is allowed.
     */
    public static final int PRINT_DOCUMENT = 0;
    /**
     * Determine the quality of printed allowed.
     */
    public static final int PRINT_DOCUMENT_QUALITY = 1;
    /**
     * Determine if changing the document is allowed.
     */
    public static final int MODIFY_DOCUMENT = 2;
    /**
     * Determine if content copying or extraction is allowed.
     */
    public static final int CONTENT_EXTRACTION = 3;
    /**
     * Determine if authoring comments and form fields is allowed.
     */
    public static final int AUTHORING_FORM_FIELDS = 4;
    /**
     * Determine if form field fill-in or signing is allowed.
     */
    public static final int FORM_FIELD_FILL_SIGNING = 5;
    /**
     * Determine if content accessibility is allowed.
     */
    public static final int CONTENT_ACCESSABILITY = 6;
    /**
     * Determine if document assembly is allowed.
     */
    public static final int DOCUMENT_ASSEMBLY = 7;


    // Permission values, indexes are mapped to constant values
    private boolean[] permissions = new boolean[10];
    // original permission integer from encrypt dictionary
    // not permission bits and revered bits. 11111111111111111111000011000000
    private int permissionFlags = 0xFFFFF0C0;
    // Revision of standard encryption algorithms
    private int revision = 2;

    // Initiated flag
    boolean isInit = false;

    /**
     * Creates a new object which can determine a document's user permissions.
     *
     * @param dictionary Encryption dictionary which contains a P key.
     */
    public Permissions(EncryptionDictionary dictionary) {
        this.permissionFlags = dictionary.getPermissions();
        revision = dictionary.getRevisionNumber();
    }

    /**
     * Initiate the permission object.  Extracts Permission bit values from
     * P key.
     */
    public void init() {

        for (int i = 0; i < permissions.length; i++) {
            permissions[i] = false;
        }

        // Create permissions based on Revision 2 rules
        if (revision == 2) {
            // print document rules
            if ((permissionFlags & PRINT_DOCUMENT_BIT_3)
                    == PRINT_DOCUMENT_BIT_3) {
                permissions[PRINT_DOCUMENT] = true;
            }
            // modify document
            if ((permissionFlags & MODIFY_DOCUMENT_BIT_4)
                    == MODIFY_DOCUMENT_BIT_4) {
                permissions[MODIFY_DOCUMENT] = true;
            }
            // copy or extract text and graphics
            if ((permissionFlags & DATA_EXTRACTION_BIT_5)
                    == DATA_EXTRACTION_BIT_5) {
                permissions[CONTENT_EXTRACTION] = true;
            }
            // authoring forms, not in 2, but use CONTENT_EXTRACTION permission
            if (permissions[CONTENT_EXTRACTION]) {
                permissions[AUTHORING_FORM_FIELDS] = true;
            }
            // Fill in existing interactive form fields, not in 2, but use
            // CONTENT_EXTRACTION permission
            if (permissions[CONTENT_EXTRACTION]) {
                permissions[FORM_FIELD_FILL_SIGNING] = true;
            }
            // document accessibility, not in 2, but use CONTENT_EXTRACTION
            // permission
            if (permissions[CONTENT_EXTRACTION]) {
                permissions[CONTENT_ACCESSABILITY] = true;
            }
            // allow assembly of document, not in 2, but use MODIFY_DOCUMENT
            // permission
            if (permissions[MODIFY_DOCUMENT]) {
                permissions[DOCUMENT_ASSEMBLY] = true;
            }
            // Print document quality, if true, print low quality version
            if ((permissionFlags & PRINT_QUALITY_BIT_12)
                    == PRINT_QUALITY_BIT_12) {
                permissions[PRINT_DOCUMENT_QUALITY] = true;
            }

            isInit = true;

        }
        // Revision 3 rules
        else if (revision >= 3) {
            // print document rules
            if ((permissionFlags & PRINT_DOCUMENT_BIT_3)
                    == PRINT_DOCUMENT_BIT_3) {
                permissions[PRINT_DOCUMENT] = true;
            }
            // modify document
            if ((permissionFlags & MODIFY_DOCUMENT_BIT_4)
                    == MODIFY_DOCUMENT_BIT_4) {
                permissions[MODIFY_DOCUMENT] = true;
            }
            // copy or extract text and graphics
            if ((permissionFlags & DATA_EXTRACTION_BIT_5)
                    == DATA_EXTRACTION_BIT_5) {
                permissions[CONTENT_EXTRACTION] = true;
            }
            // authoring forms
            if ((permissionFlags & MODIFY_TEXT_BIT_6)
                    == MODIFY_TEXT_BIT_6) {
                permissions[AUTHORING_FORM_FIELDS] = true;
            }
            // Fill in existing interactive form fields
            if ((permissionFlags & MODIFY_FORMS_BIT_9)
                    == MODIFY_FORMS_BIT_9) {
                permissions[FORM_FIELD_FILL_SIGNING] = true;
            }
            // document accessibility
            if ((permissionFlags & ACCESSIBILITY_BIT_10)
                    == ACCESSIBILITY_BIT_10) {
                permissions[CONTENT_ACCESSABILITY] = true;
            }
            // allow assembly of document
            if ((permissionFlags & ASSEMBLE_DOCUMENT_BIT_11)
                    == ASSEMBLE_DOCUMENT_BIT_11) {
                permissions[DOCUMENT_ASSEMBLY] = true;
            }
            // Print document quality, if true, print low quality version
            if ((permissionFlags & PRINT_QUALITY_BIT_12)
                    == PRINT_QUALITY_BIT_12) {
                permissions[PRINT_DOCUMENT_QUALITY] = true;
            }
            isInit = true;
        }

    }

    /**
     * Gets the permission value of the specified Permission constant.
     *
     * @param permissionIndex one of the classes constants for determining a
     *                        specific user permission.
     * @return boolean value of the permission being called.
     */
    public boolean getPermissions(final int permissionIndex) {
        if (!isInit) {
            init();
        }
        // return false if the permission index is out of bounds. 
        return !(permissionIndex < 0 || permissionIndex > permissions.length)
                && permissions[permissionIndex];
    }
}
