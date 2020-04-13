package org.icepdf.core.pobjects;

import org.icepdf.core.pobjects.security.SecurityManager;
import org.icepdf.core.util.Library;

import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

/**
 * If a PDF file contains file specifications that refer to an external file and the PDF file is archived or
 * transmitted, some provision should be made to ensure that the external references will remain valid. One way to
 * do this is to arrange for copies of the external files to accompany the PDF file. Embedded file streams (PDF 1.3)
 * address this problem by allowing the contents of referenced files to be embedded directly within the body of the
 * PDF file. This makes the PDF file a self-contained unit that can be stored or transmitted as a single entity.
 *
 * @since 6.2
 */
public class EmbeddedFileStream extends Dictionary {

    /**
     * An embedded file parameter dictionary that shall contain additional file-specific information.
     */
    public static final Name PARAMS_KEY = new Name("Params");

    /**
     * The size of the uncompressed embedded file, in bytes.
     */
    public static final Name PARAMS_SIZE_KEY = new Name("Size");

    /**
     * The date and time when the embedded file was created.
     */
    public static final Name PARAMS_CREATION_DATE_KEY = new Name("CreationDate");

    /**
     * The date and time when the embedded file was last modified.
     */
    public static final Name PARAMS_MOD_DATE_KEY = new Name("ModDate");

    /**
     * A sub dictionary containing additional information specific to Mac OS files (see Table 47).
     */
    public static final Name PARAMS_MAC_KEY = new Name("Mac");
    /**
     * The embedded file’s file type. It shall be encoded as an integer according to Mac OS conventions: a 4-character
     * ASCII text literal, that shall be a 32-bit integer, with the high-order byte first.
     */
    public static final Name PARAMS_MAC_SUBTYPE_KEY = new Name("Subtype");
    /**
     * The embedded file’s creator signature shall be encoded in the same way as Subtype.
     */
    public static final Name PARAMS_MAC_CREATOR_KEY = new Name("Creator");
    /**
     * the binary contents of the embedded file’s resource fork.
     */
    public static final Name PARAMS_MAC_RES_FORK_KEY = new Name("ResFork");

    /**
     * A 16-byte string that is the checksum of the bytes of the uncompressed embedded file. The checksum shall be
     * calculated by applying the standard MD5 message-digest algorithm (described in Internet RFC 1321, The MD5
     * Message-Digest Algorithm; see the Bibliography) to the bytes of the embedded file stream.
     */
    public static final Name PARAMS_CHECK_SUM_KEY = new Name("CheckSum");

    protected Stream fileStream;
    private SecurityManager securityManager;

    public EmbeddedFileStream(Library library, Stream fileStream) {
        super(library, fileStream.getEntries());
        this.securityManager = library.getSecurityManager();
        this.fileStream = fileStream;
    }

    /**
     * (Optional) The type of PDF object that this dictionary describes; if present, shall be EmbeddedFile for an
     * embedded file stream.
     *
     * @return type value if present otherwise null.
     */
    public Name getType() {
        return library.getName(entries, TYPE_KEY);
    }

    /**
     * (Optional) The subtype of the embedded file. The value of this entry shall be a first-class name, as defined in
     * Annex E. Names without a registered prefix shall conform to the MIME media type names defined in
     * Internet RFC 2046, Multipurpose Internet Mail Extensions (MIME), Part Two: Media Types (see the Bibliography),
     * with the provision that characters not allowed in names shall use the 2-character hexadecimal code format
     * described in 7.3.5, "Name Objects."
     *
     * @return mime media type of object
     */
    public Name getSubType() {
        return library.getName(entries, SUBTYPE_KEY);
    }

    /**
     * An embedded file parameter dictionary that shall contain additional file-specific information.
     *
     * @return the raw dictionary.
     */
    public HashMap getParams() {
        return library.getDictionary(entries, PARAMS_KEY);
    }

    /**
     * (Optional) The size of the uncompressed embedded file, in bytes.
     *
     * @return uncompressed size in bytes,  null if not specified.
     */
    public int getParamUncompressedSize() {
        int size = library.getInt(getParams(), PARAMS_SIZE_KEY);
        if (size == 0){
            size = fileStream.getDecodedStreamBytes().length;
        }
        return size;
    }

    /**
     * Get compressed size in bytes.
     *
     * @return
     */
    public int getCompressedSize() {
        return fileStream.getRawBytes().length;
    }

    /**
     * Gets the file streams decoded data stream which can be used to open or save the file given the appropriate
     * file handler.
     *
     * @return decoded byte array input stream.
     * @throws IOException io exception during stream decoding.
     */
    public InputStream getDecodedStreamData() throws IOException {
        return fileStream.getDecodedByteArrayInputStream();
    }

    /**
     * (Optional) The date and time when the embedded file was created.
     *
     * @return creation date if set,  null otherwise.
     */
    public PDate getParamCreationData() {
        Object value = library.getObject(getParams(), PARAMS_CREATION_DATE_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return new PDate(securityManager, text.getDecryptedLiteralString(securityManager));
        }
        return null;
    }

    /**
     * (Optional) The date and time when the embedded file was created.
     *
     * @return creation date if set,  null otherwise.
     */
    public PDate getParamLastModifiedData() {
        Object value = library.getObject(getParams(), PARAMS_MOD_DATE_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return new PDate(securityManager, text.getDecryptedLiteralString(securityManager));
        }
        return null;
    }

    /**
     * (Optional) A sub dictionary containing additional information specific to Mac OS files
     *
     * @return mac sub dictionary, or null if not set.
     */
    public HashMap getMacDictionary() {
        return library.getDictionary(getParams(), PARAMS_MAC_KEY);
    }

    /**
     * (Optional) A 16-byte string that is the checksum of the bytes of the uncompressed embedded file. The checksum
     * shall be calculated by applying the standard MD5 message-digest algorithm (described in Internet RFC 1321,
     * The MD5 Message-Digest Algorithm; see the Bibliography) to the bytes of the embedded file stream.
     *
     * @return checksum or null;
     */
    public String getCheckSum() {
        Object value = library.getObject(getParams(), PARAMS_CHECK_SUM_KEY);
        if (value != null && value instanceof StringObject) {
            StringObject text = (StringObject) value;
            return text.getDecryptedLiteralString(securityManager);
        }
        return null;
    }
}
