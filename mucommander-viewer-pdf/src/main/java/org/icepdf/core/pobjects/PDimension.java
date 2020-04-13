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
package org.icepdf.core.pobjects;

import java.awt.*;
import java.awt.geom.Dimension2D;


/**
 * <p>This class represents a dimension similar to java.awt.geom.Dimension2D.Dimension
 * but ensures that width and height are stored using floating point values.</p>
 *
 * @since 2.0
 */
public class PDimension extends Dimension2D {
    private float width;
    private float height;

    /**
     * Creates a new instance of a PDimension.
     *
     * @param w width of new dimension.
     * @param h height of new dimension.
     */
    public PDimension(float w, float h) {
        set(w, h);
    }

    /**
     * Creates a new instance of a PDimension.
     *
     * @param w width of new dimension.
     * @param h height of new dimension.
     */
    public PDimension(int w, int h) {
        set(w, h);
    }

    /**
     * Sets the width and height of the dimension.
     *
     * @param w new width value.
     * @param h new height value.
     */
    public void set(float w, float h) {
        width = w;
        height = h;
    }

    /**
     * Sets the width and height of the dimension.
     *
     * @param w new width value.
     * @param h new height value.
     */
    public void set(int w, int h) {
        width = w;
        height = h;
    }

    /**
     * Gets the width of the dimension object.
     *
     * @return width
     */
    public double getWidth() {
        return width;
    }

    /**
     * Gets the height of the dimension object.
     *
     * @return height
     */
    public double getHeight() {
        return height;
    }

    /**
     * Converts this object to a java.awt.geom.Dimension2D.Dimension.  The
     * floating point accuracy of the width and height are lost when converted
     * to int.
     *
     * @return a new java.awt.geom.Dimension2D.Dimension
     */
    public Dimension toDimension() {
        return new Dimension((int) width, (int) height);
    }

    /**
     * String representation of this object.
     *
     * @return string summary of width and height of this object.
     */
    public String toString() {
        return "PDimension { width=" + width + ", height=" + height + " }";
    }

    @Override
    public void setSize(double width, double height) {
        this.width = (float) width;
        this.height = (float) height;
    }
}
