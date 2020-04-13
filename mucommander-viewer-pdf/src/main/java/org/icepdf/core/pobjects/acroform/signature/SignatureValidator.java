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
package org.icepdf.core.pobjects.acroform.signature;

import org.icepdf.core.pobjects.acroform.signature.exceptions.SignatureIntegrityException;

import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.util.Collection;
import java.util.Date;

/**
 * Interface for Digital Signature validation.  Singer certificate validity can be determined from this class.
 */
public interface SignatureValidator {

    void init() throws SignatureIntegrityException;

    /**
     * Checks integrity of the signature and will set the boolean property defining isSignedDataModified.
     *
     * @throws SignatureIntegrityException occurs if there is an issue validating the public key against the cert.
     */
    void validate() throws SignatureIntegrityException;

    /**
     * Indicates if the singed data section specified by a signature has been modified.  This indicates the document
     * has been tampered with.
     *
     * @return true if singed data has been altered, false otherwise.
     */
    boolean isSignedDataModified();

    /**
     * Indicates that data after the signature definition has been been modified.  This is most likely do to another
     * signature being added to the document or some form or page manipulation.  However it is possible that
     * an major update has been appended to the document.
     *
     * @return true if the document has been modified outside the byte range of the signature.
     */
    boolean isDocumentDataModified();

    /**
     * Indicates that there are no unaccounted for bytes in the file that haven't been singed.  This generally indicates
     * if true that the document is unmodified as the signatures cover all teh bytes in the file.
     *
     * @return true if signatures cover length of file.
     */
    boolean isSignaturesCoverDocumentLength();

    /**
     * Sets the signaturesCoverDocumentLength param to indicate that all signatures have been check and cover
     * all the bytes in the document.
     *
     * @param signaturesCoverDocumentLength
     */
    void setSignaturesCoverDocumentLength(boolean signaturesCoverDocumentLength);

    /**
     * The certificate has been verified as trusted.
     *
     * @return true if the certificate is trusted, otherwise false.
     */
    boolean isCertificateChainTrusted();

    /**
     * Indicates if the signing certificate or a certificate in the chain is on a revocation list.
     *
     * @return true if the certy have been revoked, false otherwise.
     */
    boolean isRevocation();

    /**
     * Indicates the signature was self singed and the certificate can not be trusted.
     *
     * @return true if self signed, false otherwise.
     */
    boolean isSelfSigned();

    /**
     * Indicates if a certificate data has been marked as invalid.  This generally means that a certificate
     * has expired.
     *
     * @return true if the certificate data is valid, otherwise false.
     */
    boolean isCertificateDateValid();

    /**
     * The singer time stamp is valid.
     *
     * @return true if the signer time is valid.
     */
    boolean isSignerTimeValid();

    /**
     * Validation time is valid.
     *
     * @return true if the validation time is valid.
     */
    boolean isEmbeddedTimeStamp();

    /**
     * Gets the signers certificate.
     *
     * @return signers certificate.
     */
    X509Certificate getSignerCertificate();

    boolean checkByteRange() throws SignatureIntegrityException;

    /**
     * CertificateChain.
     *
     * @return certificate chain.
     */
    Collection<Certificate> getCertificateChain();

    /**
     * Gets the last time the signature was validation cycle was completed.
     *
     * @return date that validation last completed.
     */
    Date getLastValidated();

}
