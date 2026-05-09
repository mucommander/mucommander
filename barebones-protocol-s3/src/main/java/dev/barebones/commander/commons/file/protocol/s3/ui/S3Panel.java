/*
 * Copyright (C) 2002-2026 muCommander contributors
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 */
package dev.barebones.commander.commons.file.protocol.s3.ui;

import dev.barebones.commander.commons.file.Credentials;
import dev.barebones.commander.commons.file.FileURL;
import dev.barebones.commander.commons.file.protocol.s3.S3ProtocolProvider;
import dev.barebones.commander.protocol.ui.ServerPanel;
import dev.barebones.commander.protocol.ui.ServerPanelListener;

import javax.swing.JCheckBox;
import javax.swing.JFrame;
import javax.swing.JPasswordField;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import java.net.MalformedURLException;
import java.text.ParseException;

/**
 * Connect-dialog tab for S3-compatible endpoints. Form maps directly
 * onto the AWS-SDK-v2 inputs that {@link S3ProtocolProvider} reads
 * back from the URL:
 *   - endpoint host  → URL host
 *   - port           → URL port (0 = default for the chosen scheme)
 *   - access key     → URL credentials.login
 *   - secret key     → URL credentials.password
 *   - region         → URL property "region"
 *   - path-style     → URL property "pathStyle" ("true"/"false")
 *   - HTTPS/HTTP     → URL property "useHttps" ("true"/"false")
 *
 * MinIO-style: endpoint = minio.local, port = 9000, path-style = on,
 * HTTPS off. AWS-style: endpoint = s3.amazonaws.com, port = 0,
 * path-style off, HTTPS on, region = us-east-1.
 */
public class S3Panel extends ServerPanel {

    private final JTextField endpointField;
    private final JTextField bucketField;
    private final JTextField accessKeyField;
    private final JPasswordField secretKeyField;
    private final JTextField regionField;
    private final JSpinner portSpinner;
    private final JCheckBox httpsCheck;
    private final JCheckBox pathStyleCheck;

    /** Cross-instance memory of the last entries — kept in a holder
     *  so writes from instance methods don't trip SpotBugs'
     *  ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD. */
    private static final class LastValues {
        String endpoint = "s3.amazonaws.com";
        String bucket = "";
        String accessKey = "";
        String region = "us-east-1";
        int port;
        boolean useHttps = true;
        boolean pathStyle;
    }
    private static final LastValues LAST = new LastValues();

    public S3Panel(ServerPanelListener listener, JFrame mainFrame) {
        super(listener, mainFrame);

        endpointField = new JTextField(LAST.endpoint);
        endpointField.selectAll();
        addTextFieldListeners(endpointField, true);
        addRow("Endpoint", endpointField, 5);

        bucketField = new JTextField(LAST.bucket);
        addTextFieldListeners(bucketField, true);
        addRow("Bucket (optional, blank = root)", bucketField, 15);

        accessKeyField = new JTextField(LAST.accessKey);
        addTextFieldListeners(accessKeyField, false);
        addRow("Access key (blank = use SDK default chain)", accessKeyField, 5);

        secretKeyField = new JPasswordField();
        addTextFieldListeners(secretKeyField, false);
        addRow("Secret key", secretKeyField, 15);

        regionField = new JTextField(LAST.region);
        addTextFieldListeners(regionField, false);
        addRow("Region", regionField, 5);

        portSpinner = createPortSpinner(LAST.port);
        addRow("Port (0 = default for scheme)", portSpinner, 5);

        httpsCheck = new JCheckBox("HTTPS", LAST.useHttps);
        addRow(" ", httpsCheck, 0);

        pathStyleCheck = new JCheckBox("Path-style addressing (MinIO / Ceph)", LAST.pathStyle);
        addRow(" ", pathStyleCheck, 15);
    }

    private void updateValues() {
        LAST.endpoint = endpointField.getText().trim();
        LAST.bucket = bucketField.getText().trim();
        LAST.accessKey = accessKeyField.getText().trim();
        LAST.region = regionField.getText().trim();
        LAST.port = (Integer) portSpinner.getValue();
        LAST.useHttps = httpsCheck.isSelected();
        LAST.pathStyle = pathStyleCheck.isSelected();
    }

    @Override
    public FileURL getServerURL() throws MalformedURLException {
        updateValues();
        if (LAST.endpoint.isEmpty()) {
            throw new MalformedURLException("S3 endpoint must not be blank");
        }
        String path = "/";
        if (!LAST.bucket.isEmpty()) {
            path = "/" + LAST.bucket + "/";
        }
        FileURL url = FileURL.getFileURL("s3://" + LAST.endpoint + path);
        if (LAST.port > 0) {
            url.setPort(LAST.port);
        }
        if (!LAST.accessKey.isEmpty()) {
            String secret = new String(secretKeyField.getPassword());
            url.setCredentials(new Credentials(LAST.accessKey, secret));
        }
        if (!LAST.region.isEmpty()) {
            url.setProperty(S3ProtocolProvider.PROPERTY_REGION, LAST.region);
        }
        url.setProperty(S3ProtocolProvider.PROPERTY_PATH_STYLE,
            Boolean.toString(LAST.pathStyle));
        url.setProperty(S3ProtocolProvider.PROPERTY_USE_HTTPS,
            Boolean.toString(LAST.useHttps));
        return url;
    }

    @Override
    public boolean usesCredentials() {
        return true;
    }

    @Override
    public void dialogValidated() {
        try {
            portSpinner.commitEdit();
        } catch (ParseException ignored) {
            // editor commits unconditionally; ignored
        }
        updateValues();
    }
}
