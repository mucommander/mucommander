/*
 * This file is part of muCommander, http://www.mucommander.com
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package com.mucommander.viewer.image;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Image viewer preferences.
 */
@ParametersAreNonnullByDefault
public enum ImageViewerPreferences {

    INITIAL_ZOOM("initial_zoom", "image_viewer.initial_zoom_menu", "native"),
    SHOW_STATUS_BAR("show_status_bar", "image_viewer.show_status_bar", Boolean.TRUE.toString())
    ;

    private String prefKey;
    private String i18nKey;
    private String value;

    ImageViewerPreferences(String prefKey, String i18nKey, String defaultValue) {
        this.prefKey = prefKey;
        this.i18nKey = i18nKey;
        value = defaultValue;
    }

    @Nonnull
    public String getPrefKey() {
        return prefKey;
    }

    @Nonnull
    public String getI18nKey() {
        return i18nKey;
    }

    @Nonnull
    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
