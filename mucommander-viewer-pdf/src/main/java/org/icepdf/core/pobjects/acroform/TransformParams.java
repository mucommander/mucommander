package org.icepdf.core.pobjects.acroform;

import org.icepdf.core.pobjects.Name;

/**
 * Base interface for TransformParams.
 */
public interface TransformParams {
    /**
     * (Optional) The type of PDF object that this dictionary describes; if present, shall be TransformParams for a
     * transform parameters dictionary.
     */
    public static final Name TRANSFORM_PARAMS_TYPE_VALUE = new Name("TransformParams");

    /**
     * The UR transform parameters dictionary version.
     */
    public static final Name VERSION_KEY = new Name("V");
}
