package org.icepdf.core.pobjects.annotations;

import org.icepdf.core.pobjects.acroform.FieldDictionary;
import org.icepdf.core.pobjects.acroform.SignatureDictionary;
import org.icepdf.core.pobjects.acroform.SignatureFieldDictionary;
import org.icepdf.core.pobjects.acroform.SignatureHandler;
import org.icepdf.core.pobjects.acroform.signature.SignatureValidator;
import org.icepdf.core.util.Library;

import java.awt.geom.AffineTransform;
import java.util.HashMap;
import java.util.logging.Logger;

/**
 * A digital signature (PDF 1.3) may be used to authenticate the identity of a user and the document's contents. It
 * stores information about the signer and the state of the document when it was signed. The signature may be purely
 * mathematical, such as a public/private-key encrypted document digest, or it may be a biometric form of identification,
 * such as a handwritten signature, fingerprint, or retinal scan. The specific form of authentication used shall be
 * implemented by a special software module called a signature handler. Signature handlers shall be identified in
 * accordance with the rules defined in Annex E.
 * <br />
 * NOTE 2<br />
 * The entries in the signature dictionary can be conceptualized as being in different dictionaries; they are in one
 * dictionary for historical and cryptographic reasons. The categories are signature properties (R, M, Name, Reason,
 * Location, Prop_Build, Prop_AuthTime, and Prop_AuthType); key information (Cert and portions of Contents when the
 * signature value is a PKCS#7 object); reference (Reference and ByteRange); and signature value (Contents when the
 * signature value is a PKCS#1 object).
 */
public class SignatureWidgetAnnotation extends AbstractWidgetAnnotation<SignatureFieldDictionary> {

    private static final Logger logger =
            Logger.getLogger(SignatureWidgetAnnotation.class.toString());

    // signature field dictionary,
    private SignatureFieldDictionary fieldDictionary;

    // signatures value holds all the signature info for signing.
    private SignatureDictionary signatureDictionary;

    private SignatureValidator signatureValidator;

    public SignatureWidgetAnnotation(Library l, HashMap h) {
        super(l, h);
        fieldDictionary = new SignatureFieldDictionary(library, entries);

        HashMap valueDict = library.getDictionary(entries, FieldDictionary.V_KEY);
        signatureDictionary = new SignatureDictionary(library, valueDict);

    }

    public SignatureValidator getSignatureValidator() {
        if (signatureValidator == null) {
            SignatureHandler signatureHandler = fieldDictionary.getLibrary().getSignatureHandler();
            signatureValidator = signatureHandler.validateSignature(fieldDictionary);
        }
        return signatureValidator;
    }

    public SignatureWidgetAnnotation(Annotation widgetAnnotation) {
        super(widgetAnnotation.getLibrary(), widgetAnnotation.getEntries());
        fieldDictionary = new SignatureFieldDictionary(library, entries);
        // copy over the reference number.
        setPObjectReference(widgetAnnotation.getPObjectReference());
    }

    public SignatureDictionary getSignatureDictionary() {
        return signatureDictionary;
    }

    @Override
    public void reset() {

    }

    @Override
    public void resetAppearanceStream(double dx, double dy, AffineTransform pageSpace) {

    }

    @Override
    public SignatureFieldDictionary getFieldDictionary() {
        return fieldDictionary;
    }
}
