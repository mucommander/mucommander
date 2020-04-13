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

import org.bouncycastle.asn1.ASN1ObjectIdentifier;
import org.bouncycastle.asn1.x500.X500Name;
import org.bouncycastle.asn1.x500.style.BCStyle;
import org.icepdf.core.util.HexDumper;
import org.icepdf.ri.common.EscapeJDialog;
import org.icepdf.ri.common.utility.signatures.SignatureUtilities;
import org.icepdf.ri.images.Images;

import javax.swing.*;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.table.DefaultTableModel;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeSelectionModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.security.MessageDigest;
import java.security.cert.Certificate;
import java.security.cert.X509Certificate;
import java.text.MessageFormat;
import java.util.Collection;
import java.util.ResourceBundle;

/**
 * CertificatePropertiesDialog takes a certificate chain and displays each certificate in a summery view. Certificates
 * can be easily viewed and selected via a jTree component hierarchy.
 */
public class CertificatePropertiesDialog extends EscapeJDialog {

    protected static ResourceBundle messageBundle;
    private Collection<Certificate> certs;

    public CertificatePropertiesDialog(Frame parent, ResourceBundle messageBundle, Collection<Certificate> certs) {
        super(parent, true);
        CertificatePropertiesDialog.messageBundle = messageBundle;
        this.certs = certs;
        buildUI();
    }

    public CertificatePropertiesDialog(JDialog parent, ResourceBundle messageBundle, Collection<Certificate> certs) {
        super(parent, true);
        CertificatePropertiesDialog.messageBundle = messageBundle;
        this.certs = certs;
        buildUI();
    }

    private void buildUI() {
        setTitle(messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.title"));

        getContentPane().setLayout(new BorderLayout());
        Certificate[] certArray = new Certificate[certs.size()];
        int i = 0;
        for (Certificate certificate : certs) {
            certArray[i] = certificate;
            i++;
        }
        getContentPane().add(getComponents(certArray),
                BorderLayout.CENTER);

        JPanel buttonPanel = new JPanel();
        buttonPanel.setLayout(new FlowLayout(FlowLayout.TRAILING));
        JButton closeButton = new JButton(messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.closeButton.label"));
        closeButton.setMnemonic("viewer.utilityPane.signatures.cert.dialog.closeButton.mnemonic".charAt(0));
        closeButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent e) {
                setVisible(false);
            }
        });
        buttonPanel.add(closeButton);
        getContentPane().add(buttonPanel, BorderLayout.SOUTH);
        setSize(new Dimension(760, 450));
        setLocationRelativeTo(getParent());
        setResizable(true);
    }

    /**
     * builds out the dialog components, mainly the tree and info panel.
     */
    private JComponent getComponents(Certificate[] certificateChain) {

        if (certificateChain.length > 0) {
            final JTable certificateInfoTable = new JTable();
            final JTextArea propteryValueTextAea = new JTextArea();
            // Build certificate chain into a tree hierarchy.
            final JTree certChainTree = buildCertChainTree(certificateChain);
            certChainTree.addTreeSelectionListener(new TreeSelectionListener() {
                public void valueChanged(TreeSelectionEvent e) {
                    DefaultMutableTreeNode node = (DefaultMutableTreeNode) certChainTree.getLastSelectedPathComponent();
                    if (node != null) {
                        CertificateInfo certInfo = (CertificateInfo) node.getUserObject();
                        // Show certificate in the cert info panel
                        showCertificateInfo(certInfo.getCertificate(), certificateInfoTable, propteryValueTextAea);
                    }
                }
            });
            // Build certificate info table
            showCertificateInfo((X509Certificate) certificateChain[0], certificateInfoTable, propteryValueTextAea);
            certificateInfoTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
            ListSelectionModel selectionModel = certificateInfoTable.getSelectionModel();
            selectionModel.addListSelectionListener(new ListSelectionListener() {
                public void valueChanged(ListSelectionEvent e) {
                    int row = certificateInfoTable.getSelectedRow();
                    if (row >= 0) {
                        String value = (String) certificateInfoTable.getValueAt(row, 1);
                        // Update text area when selection changes
                        propteryValueTextAea.setText(value);
                        propteryValueTextAea.repaint();
                    }
                }
            });

            // main properties view.
            propteryValueTextAea.setLineWrap(false);
            propteryValueTextAea.setEditable(false);
            propteryValueTextAea.setRows(10);
            propteryValueTextAea.setColumns(40);
            // Get font from ResourceManager, and create new font
            Font fixedWidthFont = new java.awt.Font("Monospaced", java.awt.Font.PLAIN, 12);
            propteryValueTextAea.setFont(fixedWidthFont);

            // Select last row by default
            certificateInfoTable.setRowSelectionInterval(8, 8);

            // Create cert info panel
            JScrollPane scrollPane = new JScrollPane(certificateInfoTable);
            JSplitPane panelInfo = new JSplitPane(JSplitPane.VERTICAL_SPLIT);
            panelInfo.setDividerLocation(175);
            panelInfo.setTopComponent(scrollPane);
            panelInfo.setBottomComponent(new JScrollPane(propteryValueTextAea));

            JSplitPane panel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);
            panel.setBorder(BorderFactory.createCompoundBorder(
                    BorderFactory.createEmptyBorder(5, 5, 5, 5),
                    scrollPane.getBorder()));
            panel.setDividerLocation(200);
            scrollPane = new JScrollPane(certChainTree);

            panel.setLeftComponent(scrollPane);
            panel.setRightComponent(panelInfo);

            return panel;
        }
        return new JPanel();
    }

    /**
     * Break down DN string into an array used for message format.
     * Organization: {0}\n Organization Unit :{1}\n Common Name: {2}\n Local: {3}\n State: {4}\n Country:{5}\n Email: {6}
     */
    private Object[] formatDNString(X500Name rdName) {
        Object[] output = new Object[7];
        output[0] = parseRelativeDistinguishedName(rdName, BCStyle.O);
        output[1] = parseRelativeDistinguishedName(rdName, BCStyle.OU);
        output[2] = parseRelativeDistinguishedName(rdName, BCStyle.CN);
        output[3] = parseRelativeDistinguishedName(rdName, BCStyle.L);
        output[4] = parseRelativeDistinguishedName(rdName, BCStyle.ST);
        output[5] = parseRelativeDistinguishedName(rdName, BCStyle.C);
        output[6] = parseRelativeDistinguishedName(rdName, BCStyle.EmailAddress);
        return output;
    }

    protected static String parseRelativeDistinguishedName(X500Name rdName, ASN1ObjectIdentifier commonCode) {
        String rdn = SignatureUtilities.parseRelativeDistinguishedName(rdName, commonCode);
        if (rdn != null) {
            return rdn;
        }
        return messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.notAvailable.label");
    }

    /**
     * Method to reflect certificate chain in the tree view
     */
    private JTree buildCertChainTree(Certificate cert[]) {
        DefaultMutableTreeNode root = null;
        DefaultMutableTreeNode currentNode = null;
        for (Certificate aCert : cert) {
            DefaultMutableTreeNode childNode = new DefaultMutableTreeNode(
                    new CertificateInfo((X509Certificate) aCert, messageBundle));
            if (root == null) {
                root = childNode;
                currentNode = childNode;
            } else {
                currentNode.add(childNode);
                currentNode = childNode;
            }
        }
        JTree tree = new JTree(root);
        // Disable HTML to disable anchor click out.
        DefaultTreeCellRenderer customCellRenderer = new DefaultTreeCellRenderer();
        customCellRenderer.putClientProperty("html.disable", Boolean.TRUE);
        customCellRenderer.setOpenIcon(new ImageIcon(Images.get("page.gif")));
        customCellRenderer.setClosedIcon(new ImageIcon(Images.get("page.gif")));
        customCellRenderer.setLeafIcon(new ImageIcon(Images.get("page.gif")));
        tree.setCellRenderer(customCellRenderer);

        // Allow single node selection only
        tree.getSelectionModel().setSelectionMode(
                TreeSelectionModel.SINGLE_TREE_SELECTION);
        tree.setRootVisible(true);
        tree.setShowsRootHandles(true);
        tree.setScrollsOnExpand(true);

        return tree;
    }

    /**
     * Converts a byte to hex digit and writes to the supplied buffer
     */
    private void byte2hex(byte b, StringBuffer buf) {
        char[] hexChars = {'0', '1', '2', '3', '4', '5', '6', '7', '8',
                '9', 'A', 'B', 'C', 'D', 'E', 'F'};
        int high = ((b & 0xf0) >> 4);
        int low = (b & 0x0f);
        buf.append(hexChars[high]);
        buf.append(hexChars[low]);
    }

    /**
     * Converts a byte array to hex string
     */
    private String toHexString(byte[] block) {
        StringBuffer buf = new StringBuffer();
        int len = block.length;
        for (int i = 0; i < len; i++) {
            byte2hex(block[i], buf);
            if (i < len - 1) {
                buf.append(":");
            }
        }
        return buf.toString();
    }

    /**
     * Gets the requested finger print of the certificate.
     */
    private String getCertFingerPrint(String mdAlg, X509Certificate cert)
            throws Exception {
        byte[] encCertInfo = cert.getEncoded();
        MessageDigest md = MessageDigest.getInstance(mdAlg);
        byte[] digest = md.digest(encCertInfo);
        return toHexString(digest);
    }

    /**
     * Method to reflect table data based on the certificate
     */
    private void showCertificateInfo(X509Certificate cert,
                                     JTable certInfoTable, JTextArea textArea) {
        MessageFormat formatter = new MessageFormat(messageBundle.getString(
                "viewer.utilityPane.signatures.cert.dialog.info.version.value"));
        String certVersion = formatter.format(new Object[]{String.valueOf(cert.getVersion())});

        formatter.applyPattern(messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.serialNumber.value"));
        String serialNumber = formatter.format(new Object[]{String.valueOf(cert.getSerialNumber())});

        formatter.applyPattern(messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.signatureAlgorithm.value"));
        String signatureAlgorithm = formatter.format(new Object[]{cert.getSigAlgName()});

        formatter.applyPattern(messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.issuer.value"));
        String issuer = formatter.format(formatDNString(new X500Name(cert.getIssuerDN().toString())));

        formatter.applyPattern(messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.validity.value"));
        String validity = formatter.format(new Object[]{cert.getNotBefore(), cert.getNotAfter()});

        formatter.applyPattern(messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.subject.value"));
        String subject = formatter.format(formatDNString(new X500Name(cert.getSubjectDN().toString())));

        String signature = new HexDumper().dump(cert.getSignature());
        String md5 = null;
        String sha1 = null;
        try {
            formatter.applyPattern(messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.md5.value"));
            md5 = formatter.format(new Object[]{getCertFingerPrint("MD5", cert)});
            formatter.applyPattern(messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.sha1.value"));
            sha1 = formatter.format(new Object[]{getCertFingerPrint("SHA1", cert)});
        } catch (Throwable e) {
            // eat any errors.
        }
        Object[][] data = {
                {messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.version.label"), certVersion},
                {messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.serialNumber.label"), serialNumber},
                {messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.signatureAlgorithm.label"), signatureAlgorithm},
                {messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.issuer.label"), issuer},
                {messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.validity.label"), validity},
                {messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.subject.label"), subject},
                {messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.signature.label"), signature},
                {messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.md5.label"), md5},
                {messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.sha1.label"), sha1}};

        String[] columnNames = {
                messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.column1.label"),
                messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.column2.label")};

        certInfoTable.setModel(new DefaultTableModel(data, columnNames) {
            public boolean isCellEditable(int row, int col) {
                return false;
            }
        });

        // Select last row by default
        certInfoTable.setRowSelectionInterval(8, 8);
        certInfoTable.repaint();
        textArea.repaint();
    }
}

class CertificateInfo {
    private X509Certificate cert;
    private ResourceBundle messageBundle;

    CertificateInfo(X509Certificate cert, ResourceBundle messageBundle) {
        this.cert = cert;
        this.messageBundle = messageBundle;
    }

    public X509Certificate getCertificate() {
        return cert;
    }

    /**
     * Extrace CN from DN in the certificate.
     *
     * @param cert X509 certificate
     * @return CN
     */
    private String extractAliasName(X509Certificate cert) {
        String subjectName = messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.unknownSubject.label");
        String issuerName = messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.unknownIssuer.label");
        // Extract CN from the DN for each certificate
        try {
            X500Name principal = new X500Name(cert.getSubjectDN().toString());
            X500Name principalIssuer = new X500Name(cert.getIssuerDN().toString());

            // Extract subject name
            subjectName = CertificatePropertiesDialog.parseRelativeDistinguishedName(principal, BCStyle.CN);
            if (subjectName == null) {
                subjectName = CertificatePropertiesDialog.parseRelativeDistinguishedName(principal, BCStyle.O);
            }
            if (subjectName == null) {
                subjectName = messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.unknownSubject.label");
            }
            // Extract issuer name
            issuerName = CertificatePropertiesDialog.parseRelativeDistinguishedName(principalIssuer, BCStyle.CN);
            if (issuerName == null) {
                issuerName = CertificatePropertiesDialog.parseRelativeDistinguishedName(principalIssuer, BCStyle.O);
            }
            if (issuerName == null) {
                issuerName = messageBundle.getString("viewer.utilityPane.signatures.cert.dialog.info.unknownIssuer.label");
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        // Add Subject name and Issuer name in the return string
        MessageFormat messageFormat = new MessageFormat(messageBundle.getString(
                "viewer.utilityPane.signatures.cert.dialog.info.certificateInfo.label"));
        Object[] args = {subjectName, issuerName};
        return messageFormat.format(args);
    }


    public String toString() {
        return extractAliasName(cert);
    }
}