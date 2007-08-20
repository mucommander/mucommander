/*
 *  Licensed to the Apache Software Foundation (ASF) under one or more
 *  contributor license agreements.  See the NOTICE file distributed with
 *  this work for additional information regarding copyright ownership.
 *  The ASF licenses this file to You under the Apache License, Version 2.0
 *  (the "License"); you may not use this file except in compliance with
 *  the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 *
 */

package com.mucommander.file.impl.zip.provider;

import java.util.zip.ZipException;

/**
 * General format of extra field data.
 *
 * <p>Extra fields usually appear twice per file, once in the local
 * file data and once in the central directory.  Usually they are the
 * same, but they don't have to be.  {@link
 * java.util.zip.ZipOutputStream java.util.zip.ZipOutputStream} will
 * only use the local file data in both places.</p>
 *
 * <p>--------------------------------------------------------------------------------------------------------------<br>
 * <br>
 * This class is based off the <code>org.apache.tools.zip</code> package of the <i>Apache Ant</i> project. The Ant
 * code has been modified under the terms of the Apache License which you can find in the bundled muCommander license
 * file. It was forked at version 1.7.0 of Ant.</p>
 *
 * @author Apache Ant, Maxence Bernard
 */
public interface ZipExtraField {

    /**
     * The Header-ID.
     * @return the header id
     */
    ZipShort getHeaderId();

    /**
     * Length of the extra field in the local file data - without
     * Header-ID or length specifier.
     * @return the length of the field in the local file data
     */
    ZipShort getLocalFileDataLength();

    /**
     * Length of the extra field in the central directory - without
     * Header-ID or length specifier.
     * @return the length of the field in the central directory
     */
    ZipShort getCentralDirectoryLength();

    /**
     * The actual data to put into local file data - without Header-ID
     * or length specifier.
     * @return the data
     */
    byte[] getLocalFileDataData();

    /**
     * The actual data to put central directory - without Header-ID or
     * length specifier.
     * @return the data
     */
    byte[] getCentralDirectoryData();

    /**
     * Populate data from this array as if it was in local file data.
     * @param data an array of bytes
     * @param offset the start offset
     * @param length the number of bytes in the array from offset
     *
     * @throws ZipException on error
     */
    void parseFromLocalFileData(byte[] data, int offset, int length)
        throws ZipException;
}
