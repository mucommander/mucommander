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
package org.icepdf.core.pobjects.annotations;

import org.icepdf.core.pobjects.Dictionary;
import org.icepdf.core.util.Library;

import java.util.HashMap;

/**
 * <h2>Refer to: 8.4.3 Border Styles</h2>
 * <p/>
 * <table border=1>
 * <tr>
 * <td>Key</td>
 * <td>Type</td>
 * <td>Value</td>
 * </tr>
 * <tr>
 * <td><b>S</b></td>
 * <td>name</td>
 * <td><i>(Optional)</i> A name representing the border effect to apply. Possible values are:
 * <table border=0>
 * <tr>
 * <td>S</td>
 * <td>No effect: the border is as described by the annotation dictionary's <b>BS</b> entry.</td>
 * </tr>
 * <tr>
 * <td>C</td>
 * <td>The border should appear "cloudy". The width and dash array specified by <b>BS</b>
 * are honored.</td>
 * </tr>
 * </table>
 * Default value: S.</td>
 * </tr>
 * <tr>
 * <td><b>I</b></td>
 * <td>number</td>
 * <td><i>(Optional; valid only if the value of <b>S</b> is C)</i> A number describing the intensity of the effect.
 * Suggested values range from 0 to 2. Default value: 0.</td>
 * </tr>
 * </table>
 *
 * @author Mark Collette
 * @since 2.5
 */
public class BorderEffect extends Dictionary {
    /**
     * Creates a new instance of a BorderEffect.
     *
     * @param l document library.
     * @param h dictionary entries.
     */
    public BorderEffect(Library l, HashMap h) {
        super(l, h);
    }
}
