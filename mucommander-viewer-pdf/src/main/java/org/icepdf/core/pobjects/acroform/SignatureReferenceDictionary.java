package org.icepdf.core.pobjects.acroform;

import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.pobjects.Name;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * Signature reference dictionary.
 */
public class SignatureReferenceDictionary extends Dictionary {

    /**
     * (Optional) The type of PDF object that this dictionary describes; if present, shall be SigRef for a signature
     * reference dictionary.
     */
    public static final Name SIG_REF_TYPE_VALUE = new Name("SigRef");

    /**
     * (Required) The name of the transform method (see Section 12.8.2, Transform Methods) that shall guide the
     * modification analysis that takes place when the signature is validated. Valid values shall be:
     * <ul>
     * <li>DocMDP Used to detect modifications to a document relative to a signature field that is signed by the
     * originator of a document; see 12.8.2.2, DocMDP.</li>
     * <li>UR Used to detect modifications to a document that would invalidate a signature in a rights-enabled
     * document; see 12.8.2.3, UR.</li>
     * <li>FieldMDPUsed to detect modifications to a list of form fields specified in TransformParams;
     * see 12.8.2.4, FieldMDP.</li>
     * </ul>
     */
    public static final Name TRANSFORM_METHOD_KEY = new Name("TransformMethod");

    private TransformParams transformParams;

    /**
     * Available Transfer Method currently available.
     */
    public enum TransformMethods {
        FieldMDP,
        DocMDP,
        UR3
    }

    /**
     * (Optional) A dictionary specifying transform parameters (variable data) for the transform method specified by
     * TransformMethod. Each method takes its own set of parameters. See each of the sub-clauses specified previously
     * for details on the individual transform parameter dictionaries
     */
    public static final Name TRANSFORM_PARAMS_KEY = new Name("TransformParams");

    /**
     * (Required when TransformMethod is FieldMDP) An indirect reference to the object in the document upon which the
     * object modification analysis should be performed. For transform methods other than FieldMDP, this object is
     * implicitly defined.
     */
    public static final Name DATA_KEY = new Name("Data");

    /**
     * (Optional; PDF 1.5 required) A name identifying the algorithm that shall be used when computing the digest.
     * Valid values are MD5 and SHA1. Default value: MD5. For security reasons, MD5 should not be used. It is mentioned
     * for backwards compatibility, since it remains the default value.
     */
    public static final Name DIGEST_METHOD_KEY = new Name("DigestMethod");

    /**
     * Creates a new instance of a Dictionary.
     *
     * @param library document library.
     * @param entries dictionary entries.
     */
    public SignatureReferenceDictionary(Library library, HashMap entries) {
        super(library, entries);
    }

    /**
     * Gets the transform method use by
     *
     * @return TransformMethods representing one of the preferred transform methods.
     */
    public TransformMethods getTransformMethod() {
        Name tmp = library.getName(entries, TRANSFORM_METHOD_KEY);
        if (tmp != null) {
            if (tmp.equals(TransformMethods.DocMDP.toString())) {
                return TransformMethods.DocMDP;
            } else if (tmp.equals(TransformMethods.FieldMDP.toString())) {
                return TransformMethods.FieldMDP;
            } else if (tmp.equals(TransformMethods.UR3.toString())) {
                return TransformMethods.UR3;
            }
        }
        return null;
    }

    /**
     * Gets the transforms Params implementations specified by the transform method.
     *
     * @return TransformParams of type; DocMDPTransferParam, FieldMDPTransferParam, or UR3TransferParam.
     */
    public TransformParams getTransformParams() {
        Name tmp = library.getName(entries, TRANSFORM_METHOD_KEY);
        if (tmp.equals(TransformMethods.DocMDP.toString())) {
            transformParams =
                    new DocMDPTransferParam(library, library.getDictionary(entries, TRANSFORM_PARAMS_KEY));
        } else if (tmp.equals(TransformMethods.FieldMDP.toString())) {
            transformParams =
                    new FieldMDPTransferParam(library, library.getDictionary(entries, TRANSFORM_PARAMS_KEY));
        } else if (tmp.equals(TransformMethods.UR3.toString())) {
            transformParams =
                    new UR3TransferParam(library, library.getDictionary(entries, TRANSFORM_PARAMS_KEY));
        }
        return transformParams;
    }

}
