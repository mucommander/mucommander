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

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.RDN;
import org.bouncycastle.asn1.x500.X500Name;

/**
 * Utility of commonly used signature related algorithms.
 */
public class SignatureUtilities {

    /**
     * Parse out a known data element from an X500Name.
     *
     * @param rdName     name to parse value from.
     * @param commonCode BCStyle name .
     * @return BCStyle name value,  null if the BCStyle name was not found.
     */
    public static String parseRelativeDistinguishedName(X500Name rdName, ASN1ObjectIdentifier commonCode) {
        RDN[] rdns = rdName.getRDNs(commonCode);
        if (rdns != null && rdns.length > 0 && rdns[0].getFirst() != null) {
            return rdns[0].getFirst().getValue().toString();
        }
        return null;
    }
}
