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
package com.mucommander.viewer.binary;

/**
 * Mode for calculation of the go-to position in binary document.
 */
public enum GoToBinaryPositionMode {
    /**
     * Count from start of the document.
     */
    FROM_START,
    /**
     * Count from end of the document.
     */
    FROM_END,
    /**
     * Count from current position of the cursor in the document.
     */
    FROM_CURSOR
}
