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
package org.icepdf.ri.common.utility.signatures;

import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.icepdf.core.pobjects.PDate;
import org.icepdf.core.pobjects.acroform.SignatureDictionary;
import org.icepdf.core.pobjects.acroform.SignatureFieldDictionary;
import org.icepdf.core.pobjects.acroform.signature.SignatureValidator;
import org.icepdf.core.pobjects.acroform.signature.exceptions.SignatureIntegrityException;
import org.icepdf.core.pobjects.annotations.SignatureWidgetAnnotation;
import org.icepdf.ri.images.Images;

import javax.security.auth.x500.X500Principal;
import javax.swing.*;
import javax.swing.tree.DefaultMutableTreeNode;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.ResourceBundle;
import java.util.logging.Logger;

/**
 * Represents a signatures in the signature tree.  The node can be expanded to show more details about the
 * signer, validity and certificate details.
 */
@SuppressWarnings("serial")
public class SignatureTreeNode extends DefaultMutableTreeNode {

    private static final Logger logger =
            Logger.getLogger(SignatureTreeNode.class.toString());

    private ResourceBundle messageBundle;
    private SignatureWidgetAnnotation signatureWidgetAnnotation;
    private SignatureValidator signatureValidator;
    // flag that validation process is taking place.
    private boolean verifyingSignature;


    private String location = null;
    private String reason = null;
    private String contact = null;
    private String name = null;
    private String commonName = null;
    private String organization = null;
    private String emailAddress = null;
    private String date = null;

    /**
     * Creates a new instance of an OutlineItemTreeNode
     *
     * @param signatureWidgetAnnotation Contains PDF Outline signatureWidgetAnnotation data
     */
    public SignatureTreeNode(SignatureWidgetAnnotation signatureWidgetAnnotation, ResourceBundle messageBundle) {
        super();
        this.signatureWidgetAnnotation = signatureWidgetAnnotation;
        this.messageBundle = messageBundle;

        try {
            validateSignatureNode();
        } catch (SignatureIntegrityException e) {
            logger.warning("There was an issue creating a node for the signature: " +
                    signatureWidgetAnnotation.toString());
            // build a user node object to report the error.
            MessageFormat formatter = new MessageFormat(messageBundle.getString(
                    "viewer.utilityPane.signatures.tab.certTree.error.label"));
            setUserObject(formatter.format(new Object[]{(commonName != null ? commonName + " " : " "),
                    (emailAddress != null ? "<" + emailAddress + ">" : "")}));
        }
    }

    /**
     * Validates the signatures represented by this tree node.  This method is called by a worker thread
     * and once validation is complete the notes states is updated with a call to {@link #refreshSignerNode()}
     *
     * @throws SignatureIntegrityException
     */
    public void validateSignatureNode() throws SignatureIntegrityException {

        SignatureFieldDictionary fieldDictionary = signatureWidgetAnnotation.getFieldDictionary();
        SignatureDictionary signatureDictionary = signatureWidgetAnnotation.getSignatureDictionary();
        if (fieldDictionary != null) {
            // grab some signer properties right from the annotations dictionary.
            name = signatureDictionary.getName();
            location = signatureDictionary.getLocation();
            reason = signatureDictionary.getReason();
            contact = signatureDictionary.getContactInfo();
            date = signatureDictionary.getDate();

            // getting a signatureValidator should give us a pointer the to the signer cert if all goes well.
            signatureValidator = signatureWidgetAnnotation.getSignatureValidator();
            // try and parse out the signer info.
            X509Certificate certificate = signatureValidator.getSignerCertificate();
            X500Principal principal = certificate.getIssuerX500Principal();
            X500Name x500name = new X500Name(principal.getName());
            if (x500name.getRDNs() != null) {
                commonName = SignatureUtilities.parseRelativeDistinguishedName(x500name, BCStyle.CN);
                organization = SignatureUtilities.parseRelativeDistinguishedName(x500name, BCStyle.O);
                emailAddress = SignatureUtilities.parseRelativeDistinguishedName(x500name, BCStyle.EmailAddress);
            }
            // Start validation process.
            setVerifyingSignature(true);
            signatureValidator.validate();
            setVerifyingSignature(true);
        }

    }

    /**
     * Builds a rather complicated tree node and child nodes to show various properties of a a signer and the
     * corresponding certificate.  The main purpose is to display to the end user if the certificate is valid and
     * can be trusted as well as showing document permissions and if the document has been modified since it was
     * singed.
     * <p/>
     * - Singed by "signer name"
     * |
     * - Signature is <valid|invalid>
     * |
     * - This version of the document has <not> been altered
     * - SignatureSigner's identity is <valid|invalid>
     * - Signature includes an embedded timestamp | Signing is from the clock of the signer's computer.
     * - Permissions
     * |
     * - No changes allowed
     * - Field values can be changed
     * - needs more research
     * - Signature Details
     * |
     * - Reason:
     * - Location:
     * - Certificate Details (clickable, loads certificate dialog)
     * - Last Checked: <verification last run time>
     * - Field Name: <field name> on page X (clickable, takes to page and applies focus).
     *
     */
    public synchronized void refreshSignerNode() {
        if (isVerifyingSignature()) {
            // should have enough data to build a out a full signature node.
            MessageFormat formatter = new MessageFormat(messageBundle.getString(
                    "viewer.utilityPane.signatures.tab.certTree.rootSigned.label"));
            setUserObject(formatter.format(new Object[]{(commonName != null ? commonName + " " : " "),
                    (emailAddress != null ? "<" + emailAddress + ">" : "")}));
            removeAllChildren();
            // signature validity
            buildSignatureValidity(this);
            // add signature details
            buildSignatureDetails(this);
            // tack on last verified date and link to annotation if present
            buildVerifiedDateAndFieldLink(this);
        } else {
            // build out a simple validating message
            MessageFormat formatter = new MessageFormat(messageBundle.getString(
                    "viewer.utilityPane.signatures.tab.certTree.rootValidating.label"));
            setUserObject(formatter.format(new Object[]{(commonName != null ? commonName + " " : " "),
                    (emailAddress != null ? "<" + emailAddress + ">" : "")}));
        }
    }

    // set one of the three icon's to represent the validity status of the signature node.
    protected ImageIcon getRootNodeValidityIcon() {
        if (!signatureValidator.isSignedDataModified() && signatureValidator.isCertificateChainTrusted()
                && signatureValidator.isSignaturesCoverDocumentLength()) {
            return new ImageIcon(Images.get("signature_valid.png"));
        } else if (!signatureValidator.isSignedDataModified() && signatureValidator.isSignaturesCoverDocumentLength()) {
            return new ImageIcon(Images.get("signature_caution.png"));
        } else {
            return new ImageIcon(Images.get("signature_invalid.png"));
        }
    }

    // builds otu the validity tree node.
    private void buildSignatureValidity(DefaultMutableTreeNode root) {
        // figure out the opening messages.
        String validity = "viewer.utilityPane.signatures.tab.certTree.cert.invalid.label";
        if (!signatureValidator.isSignedDataModified() && signatureValidator.isCertificateChainTrusted()) {
            validity = "viewer.utilityPane.signatures.tab.certTree.cert.unknown.label";
        } else if (!signatureValidator.isSignedDataModified() && !signatureValidator.isCertificateChainTrusted()) {
            validity = "viewer.utilityPane.signatures.tab.certTree.cert.valid.label";
        }
        SigPropertyTreeNode rootValidityDetails = new SigPropertyTreeNode(
                messageBundle.getString(validity));

        // document modification
        String documentModified = "viewer.utilityPane.signatures.tab.certTree.doc.modified.label";
        if (!signatureValidator.isSignedDataModified() && !signatureValidator.isDocumentDataModified()) {
            documentModified = "viewer.utilityPane.signatures.tab.certTree.doc.unmodified.label";
        } else if (!signatureValidator.isSignedDataModified() && signatureValidator.isDocumentDataModified() && signatureValidator.isSignaturesCoverDocumentLength()) {
            documentModified = "viewer.utilityPane.signatures.tab.certTree.doc.modified.label";
        } else if (!signatureValidator.isSignaturesCoverDocumentLength()) {
            documentModified = "viewer.utilityPane.signatures.tab.certTree.doc.major.label";
        }
        rootValidityDetails.add(new SigPropertyTreeNode(messageBundle.getString(documentModified)));
        // trusted certification
        String certificateTrusted = "viewer.utilityPane.signatures.tab.certTree.signature.identity.unknown.label";
        if (signatureValidator.isCertificateChainTrusted()) {
            if (signatureValidator.isRevocation()) {
                certificateTrusted = "viewer.utilityPane.signatures.tab.certTree.signature.identity.unchecked.label";
            } else {
                certificateTrusted = "viewer.utilityPane.signatures.tab.certTree.signature.identity.valid.label";
            }
        }
        rootValidityDetails.add(new SigPropertyTreeNode(messageBundle.getString(certificateTrusted)));
        // signature time.
        String signatureTime = "viewer.utilityPane.signatures.tab.certTree.signature.time.local.label";
        if (signatureValidator.isEmbeddedTimeStamp()) {
            signatureTime = "viewer.utilityPane.signatures.tab.certTree.signature.time.embedded.label";
        }
        rootValidityDetails.add(new SigPropertyTreeNode(messageBundle.getString(signatureTime)));
        root.add(rootValidityDetails);
    }

    // builds out the signature details
    private void buildSignatureDetails(DefaultMutableTreeNode root) {
        SigPropertyTreeNode rootSignatureDetails = new SigPropertyTreeNode(
                messageBundle.getString("viewer.utilityPane.signatures.tab.certTree.signature.details.label"));
        // try and add the reason
        if (reason != null && reason.length() > 0) {
            MessageFormat messageFormat = new MessageFormat(messageBundle.getString(
                    "viewer.utilityPane.signatures.tab.certTree.signature.details.reason.label"));
            rootSignatureDetails.add(new SigPropertyTreeNode(messageFormat.format(new Object[]{reason})));
        }
        // add the location
        if (location != null && location.length() > 0) {
            MessageFormat messageFormat = new MessageFormat(messageBundle.getString(
                    "viewer.utilityPane.signatures.tab.certTree.signature.details.location.label"));
            rootSignatureDetails.add(new SigPropertyTreeNode(messageFormat.format(new Object[]{location})));
        }
        // add link for bringing up the certificate details.
        rootSignatureDetails.add(new SignatureCertTreeNode(
                messageBundle.getString("viewer.utilityPane.signatures.tab.certTree.signature.details.full.label"),
                signatureValidator.getCertificateChain(),
                getRootNodeValidityIcon().getImage()));
        root.add(rootSignatureDetails);
    }

    private void buildVerifiedDateAndFieldLink(DefaultMutableTreeNode root) {
        if (signatureValidator != null && signatureValidator.getLastValidated() != null) {
            MessageFormat messageFormat = new MessageFormat(messageBundle.getString(
                    "viewer.utilityPane.signatures.tab.certTree.signature.lastChecked.label"));
            SigPropertyTreeNode lastChecked =
                    new SigPropertyTreeNode(messageFormat.format(new Object[]{
                            new PDate(signatureWidgetAnnotation.getLibrary().getSecurityManager(),
                                    PDate.formatDateTime(signatureValidator.getLastValidated())).toString()}));
            lastChecked.setAllowsChildren(false);
            root.add(lastChecked);
        }
    }

    public synchronized boolean isVerifyingSignature() {
        return verifyingSignature;
    }

    /**
     * Flat to indicated that the validation process has completed and the state variables are in a completed
     * state.  This doesn't mean that the signature is valid just the validation process is complete.
     *
     * @param verifyingSignature true to indicate the validation process is complete, otherwise falls.
     */
    public void setVerifyingSignature(boolean verifyingSignature) {
        this.verifyingSignature = verifyingSignature;
    }

    public SignatureWidgetAnnotation getOutlineItem() {
        return signatureWidgetAnnotation;
    }

}
