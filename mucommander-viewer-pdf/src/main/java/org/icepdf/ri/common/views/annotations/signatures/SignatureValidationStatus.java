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
package org.icepdf.ri.common.views.annotations.signatures;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.icepdf.core.pobjects.acroform.SignatureDictionary;
import org.icepdf.core.pobjects.acroform.SignatureFieldDictionary;
import org.icepdf.core.pobjects.acroform.signature.SignatureValidator;
import org.icepdf.core.pobjects.acroform.signature.exceptions.SignatureIntegrityException;
import org.icepdf.core.pobjects.annotations.SignatureWidgetAnnotation;
import org.icepdf.ri.common.utility.signatures.SignatureUtilities;
import org.icepdf.ri.images.Images;

import javax.security.auth.x500.X500Principal;
import java.net.URL;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ResourceBundle;

/**
 * Common panel construct for show validation status of a given signature and validator.
 */
public class SignatureValidationStatus {

    private String validity;
    private String singedBy;
    private String documentModified;
    private String certificateTrusted;
    private String signatureTime;
    private String emailAddress;
    private String organization;
    private String commonName;
    private URL validityIconPath;

    private String dictionaryName;
    private String dictionaryLocation;
    private String dictionaryReason;
    private String dictionaryContact;
    private String dictionaryDate;

    public SignatureValidationStatus(ResourceBundle messageBundle,
                                     SignatureWidgetAnnotation signatureWidgetAnnotation, SignatureValidator signatureValidator) {

        // build out the string that we need to display
        validity = "viewer.annotation.signature.validation.common.invalid.label";
        if (!signatureValidator.isSignedDataModified() && signatureValidator.isCertificateChainTrusted()) {
            validity = "viewer.annotation.signature.validation.common.unknown.label";
        } else if (!signatureValidator.isSignedDataModified() && !signatureValidator.isCertificateChainTrusted()) {
            validity = "viewer.annotation.signature.validation.common.valid.label";
        }
        validity = messageBundle.getString(validity);

        // signed by
        singedBy = messageBundle.getString("viewer.annotation.signature.validation.common.notAvailable.label");
        try {
            validateSignatureNode(signatureWidgetAnnotation, signatureValidator);
            MessageFormat formatter = new MessageFormat(messageBundle.getString(
                    "viewer.annotation.signature.validation.common.signedBy.label"));
            singedBy = formatter.format(new Object[]{(commonName != null ? commonName + " " : " "),
                    (emailAddress != null ? "<" + emailAddress + ">" : "")});
        } catch (SignatureIntegrityException e) {
            e.printStackTrace();
        }

        // document modification
        documentModified = "viewer.annotation.signature.validation.common.doc.modified.label";
        if (!signatureValidator.isSignedDataModified() && !signatureValidator.isDocumentDataModified()) {
            documentModified = "viewer.annotation.signature.validation.common.doc.unmodified.label";
        } else if (!signatureValidator.isSignedDataModified() && signatureValidator.isDocumentDataModified() && signatureValidator.isSignaturesCoverDocumentLength()) {
            documentModified = "viewer.annotation.signature.validation.common.doc.modified.label";
        } else if (!signatureValidator.isSignaturesCoverDocumentLength()) {
            documentModified = "viewer.annotation.signature.validation.common.doc.major.label";
        }
        documentModified = messageBundle.getString(documentModified);

        // trusted certification
        certificateTrusted = "viewer.annotation.signature.validation.common.identity.unknown.label";
        if (signatureValidator.isCertificateChainTrusted()) {
            if (signatureValidator.isRevocation()) {
                certificateTrusted = "viewer.annotation.signature.validation.common.identity.unchecked.label";
            } else {
                certificateTrusted = "viewer.annotation.signature.validation.common.identity.valid.label";
            }
        }
        certificateTrusted = messageBundle.getString(certificateTrusted);

        // signature time.
        signatureTime = "viewer.annotation.signature.validation.common.time.local.label";
        if (signatureValidator.isSignerTimeValid()) {
            signatureTime = "viewer.annotation.signature.validation.common.time.embedded.label";
        }
        signatureTime = messageBundle.getString(signatureTime);

        validityIconPath = getLargeValidityIcon(signatureValidator);

        // signature dictionary common names.
        SignatureDictionary signatureDictionary = signatureWidgetAnnotation.getSignatureDictionary();
        // grab some signer properties right from the annotations dictionary.
        dictionaryName = signatureDictionary.getName();
        dictionaryLocation = signatureDictionary.getLocation();
        dictionaryReason = signatureDictionary.getReason();
        dictionaryContact = signatureDictionary.getContactInfo();
        dictionaryDate = signatureDictionary.getDate();
    }

    private void validateSignatureNode(SignatureWidgetAnnotation signatureWidgetAnnotation, SignatureValidator signatureValidator)
            throws SignatureIntegrityException {
        SignatureFieldDictionary fieldDictionary = signatureWidgetAnnotation.getFieldDictionary();

        if (fieldDictionary != null) {
            // try and parse out the signer info.
            X509Certificate certificate = signatureValidator.getSignerCertificate();
            X500Principal principal = certificate.getIssuerX500Principal();
            X500Name x500name = new X500Name(principal.getName());
            if (x500name.getRDNs() != null) {
                commonName = SignatureUtilities.parseRelativeDistinguishedName(x500name, BCStyle.CN);
                organization = SignatureUtilities.parseRelativeDistinguishedName(x500name, BCStyle.O);
                emailAddress = SignatureUtilities.parseRelativeDistinguishedName(x500name, BCStyle.EmailAddress);
            }
        }
    }

    // set one of the three icon's to represent the validity status of the signature node.
    protected URL getLargeValidityIcon(SignatureValidator signatureValidator) {
        if (!signatureValidator.isSignedDataModified() && signatureValidator.isCertificateChainTrusted()
                && signatureValidator.isSignaturesCoverDocumentLength()) {
            return Images.get("signature_valid_lg.png");
        } else if (!signatureValidator.isSignedDataModified() && signatureValidator.isSignaturesCoverDocumentLength()) {
            return Images.get("signature_caution_lg.png");
        } else {
            return Images.get("signature_invalid_lg.png");
        }
    }

    public URL getValidityIconPath() {
        return validityIconPath;
    }

    public String getValidity() {
        return validity;
    }

    public String getSingedBy() {
        return singedBy;
    }

    public String getDocumentModified() {
        return documentModified;
    }

    public String getCertificateTrusted() {
        return certificateTrusted;
    }

    public String getSignatureTime() {
        return signatureTime;
    }

    public String getEmailAddress() {
        return emailAddress;
    }

    public String getCommonName() {
        return commonName;
    }

    public String getOrganization() {
        return organization;
    }

    public String getDictionaryName() {
        return dictionaryName;
    }

    public String getDictionaryLocation() {
        return dictionaryLocation;
    }

    public String getDictionaryReason() {
        return dictionaryReason;
    }

    public String getDictionaryContact() {
        return dictionaryContact;
    }

    public String getDictionaryDate() {
        return dictionaryDate;
    }
}
