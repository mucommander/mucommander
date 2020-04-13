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
package org.icepdf.core.pobjects;

import org.icepdf.core.util.Library;
import org.icepdf.core.util.Utils;

import java.util.HashMap;

/**
 * <p>The File Specification diction provides more flexibility then the string
 * form.  allowing different files to be specified for different file systems or
 * platforms, or for file system other than the standard ones (DOS/Windows, Mac
 * OS, and Unix).</p>
 *
 * @author ICEsoft Technologies, Inc.
 * @since 2.6
 */
public class FileSpecification extends Dictionary {

    /**
     * The name of the file system that shall be used to interpret this file
     * specification. If this entry is present, all other entries in the dictionary
     * shall be interpreted by the designated file system. PDF shall define only
     * one standard file system name, URL; an application can register other
     * names. This entry shall be independent of the F, UF, DOS, Mac, and
     * Unix entries.
     */
    public static final Name FS_KEY = new Name("FS");

    /**
     * (Required if the DOS, Mac, and Unix entries are all absent; amended with
     * the UF entry for PDF 1.7) A file specification string of the form
     * described in 7.11.2, "File Specification Strings," or (if the file system is URL)
     * a uniform resource locator, as described in 7.11.5, "URL Specifications."
     * <p/>
     * The UF entry should be used in addition to the F entry. The UF entry provides
     * cross-platform and cross-language compatibility and the F entry provides
     * backwards compatibility.
     */
    public static final Name F_KEY = new Name("F");

    /**
     * A Unicode text string that provides file specification of the form
     * described in 7.11.2, "File Specification Strings." This is a text string
     * encoded using PDFDocEncoding or UTF-16BE with a leading byte-order marker
     * (as defined in 7.9.2.2, "Text String Type"). The F entry should be included
     * along with this entry for backwards compatibility reasons.
     */
    public static final Name UF_KEY = new Name("UF");

    /**
     * A file specification string (see 7.11.2, "File Specification Strings")
     * representing a DOS file name.
     * <p/>
     * This entry is obsolescent and should not be used by conforming writers.
     */
    public static final Name DOS_KEY = new Name("DOS");

    /**
     * A file specification string (see 7.11.2, "File Specification
     * Strings") representing a Mac OS file name.
     * <p/>
     * This entry is obsolescent and should not be used by conforming writers.
     */
    public static final Name MAC_KEY = new Name("Mac");

    /**
     * A file specification string (see 7.11.2, "File Specification Strings")
     * representing a UNIX file name.
     * <p/>
     * This entry is obsolescent and should not be used by conforming writers.
     */
    public static final Name UNIX_KEY = new Name("Unix");

    /**
     * An array of two byte strings constituting a file identifier that should
     * be included in the referenced file.
     * <p/>
     * <b>NOTE</b>
     * The use of this entry improves an application's chances of finding the
     * intended file and allows it to warn the user if the file has changed
     * since the link was made.
     */
    public static final Name ID_KEY = new Name("ID");

    /**
     * flag indicating whether the file referenced by the file specification is
     * volatile (changes frequently with time). If the value is true, applications
     * shall not cache a copy of the file. For example, a movie annotation
     * referencing a URL to a live video camera could set this flag to true to
     * notify the conforming reader that it should re-acquire the movie each time
     * it is played. Default value: false.
     */
    public static final Name V_KEY = new Name("V");

    /**
     * (Required if RF is present; PDF 1.3; amended to include the UF key in PDF
     * 1.7) A dictionary containing a subset of the keys F, UF, DOS, Mac, and Unix,
     * corresponding to the entries by those names in the file specification dictionary.
     * The value of each such key shall be an embedded file stream (see 7.11.4,
     * "Embedded File Streams") containing the corresponding file. If this entry
     * is present, the Type entry is required and the file specification dictionary
     * shall be indirectly referenced.
     * <p/>
     * The F and UF entries should be used in place of the DOS, Mac, or Unix entries.
     */
    public static final Name EF_KEY = new Name("EF");

    /**
     * A dictionary with the same structure as the EF dictionary, which shall be
     * present. Each key in the RF dictionary shall also be present in the EF
     * dictionary. Each value shall be a related files array (see 7.11.4.2,
     * "Related Files Arrays") identifying files that are related to the
     * corresponding file in the EF dictionary. If this entry is present, the
     * Type entry is required and the file specification dictionary shall be
     * indirectly referenced.
     */
    public static final Name RF_KEY = new Name("RF");

    /**
     * Descriptive text associated with the file specification. It shall be used
     * for files in the EmbeddedFiles name tree
     */
    public static final Name DESC_KEY = new Name("Desc");

    /**
     * A collection item dictionary, which shall be used to create the user
     * interface for portable collections (see 7.11.6, "Collection Items").
     */
    public static final Name CI_KEY = new Name("CI");

    /**
     * Constructs a new specification dictionary.
     *
     * @param l document library.
     * @param h dictionary entries.
     */
    public FileSpecification(Library l, HashMap h) {
        super(l, h);
    }

    /**
     * The type of the PDF object that this dictionary describes which is always
     * "Filespec".
     *
     * @return type of PDF object, "Filespec".
     */
    public Name getType() {
        return library.getName(entries, TYPE_KEY);
    }

    /**
     * Gets the name of the file system to be used to interpret this file
     * specification. This entry is independent of the F, DOS, Mac and Unix
     * entries.
     *
     * @return the name of the file system to be used to interpret this file.
     */
    public Name getFileSystemName() {
        return library.getName(entries, FS_KEY);
    }

    /**
     * Gets the file specification string.
     *
     * @return file specification string.
     */
    public String getFileSpecification() {
        Object tmp = library.getObject(entries, F_KEY);
        if (tmp instanceof StringObject) {
            return Utils.convertStringObject(library, (StringObject) tmp);
        } else {
            return null;
        }
    }

    /**
     * Gets the unicode file specification string.
     *
     * @return file specification string.
     */
    public String getUnicodeFileSpecification() {
        Object tmp = library.getObject(entries, UF_KEY);
        if (tmp instanceof StringObject) {
            return Utils.convertStringObject(library, (StringObject) tmp);
        } else {
            return null;
        }
    }

    /**
     * Gets the file specification string representing a DOS file name.
     *
     * @return DOS file name.
     */
    public String getDos() {
        Object tmp = library.getObject(entries, DOS_KEY);
        if (tmp instanceof StringObject) {
            return ((StringObject) tmp)
                    .getDecryptedLiteralString(
                            library.getSecurityManager());
        } else {
            return null;
        }
    }

    /**
     * Gets the file specification string representing a Mac file name.
     *
     * @return Mac file name.
     */
    public String getMac() {
        Object tmp = library.getObject(entries, MAC_KEY);
        if (tmp instanceof StringObject) {
            return ((StringObject) tmp)
                    .getDecryptedLiteralString(
                            library.getSecurityManager());
        } else {
            return null;
        }
    }

    /**
     * Gets the file specification string representing a Unix file name.
     *
     * @return Unix file name.
     */
    public String getUnix() {
        Object tmp = library.getObject(entries, UNIX_KEY);
        if (tmp instanceof StringObject) {
            return ((StringObject) tmp)
                    .getDecryptedLiteralString(
                            library.getSecurityManager());
        } else {
            return null;
        }
    }

    /**
     * Gets an array of two strings constituting a file identifier that is also
     * included in the referenced file.
     *
     * @return file identifier.
     */
    public String getId() {
        Object tmp = library.getObject(entries, ID_KEY);
        if (tmp != null) {
            return tmp.toString();
        } else {
            return null;
        }
    }

    /**
     * Returns a flag indicating whether the file referenced by the file
     * specification is volatile (changes frequently with time).
     *
     * @return true indicates the file is volitile and should not be cached,
     * otherwise true.
     */
    public Boolean isVolitile() {
        return library.getBoolean(entries, V_KEY);
    }

    /**
     * Gets a dictionary containing a subset of the keys F, DOS, Mac, and
     * Unix.  The value of each key is an embedded file stream.
     *
     * @return embbed file stream properties.
     */
    public HashMap getEmbeddedFileDictionary() {
        return library.getDictionary(entries, EF_KEY);
    }

    /**
     * Gets the full file specification if present. If null is returned then the file specification is simple
     * and a call should be made to dos, mac or unix, however these are obsolescent and likely won't be
     * encountered.
     *
     * @return associated EmbeddedFileStream object if present, null otherwise.
     */
    public EmbeddedFileStream getEmbeddedFileStream(){
        HashMap fileDictionary = getEmbeddedFileDictionary();
        Reference fileRef = (Reference) fileDictionary.get(FileSpecification.F_KEY);
        if (fileRef != null) {
            Stream fileStream = (Stream) library.getObject(fileRef);
            if (fileStream != null) {
                return new EmbeddedFileStream(library, fileStream);
            }
        }
        return null;
    }

    /**
     * Gets a dictionary with the same structure as the EF dectionary, which
     * must also b present.  EAch key in the RF dictionary must also be present
     * in the EF diciontary.  Each value is a related file array identifying
     * files that a re related to the corresponding file in the EF dictionary.
     *
     * @return related files dictionary.
     */
    public HashMap getRelatedFilesDictionary() {
        return library.getDictionary(entries, RF_KEY);
    }

    /**
     * Gets the descriptive text associated with the file specification.
     *
     * @return file identifier.
     */
    public String getDescription() {
        Object description = library.getObject(entries, DESC_KEY);
        if (description instanceof StringObject) {
            return Utils.convertStringObject(library, (StringObject) description);
        } else if (description instanceof String) {
            return description.toString();
        } else {
            return null;
        }
    }

    /**
     * GA collection item dictionary, which shall be used to create the user
     * interface for portable collections (see 7.11.6, "Collection Items").
     *
     * @return related files dictionary.
     */
    public HashMap getCollectionItemDictionary() {
        return library.getDictionary(entries, CI_KEY);
    }
}
