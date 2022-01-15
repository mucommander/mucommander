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
package com.mucommander.search;

/**
 * @author Gerolf Scherr
 */
public enum SizeUnit {
    bytes(1),
    kB(1024),
    MB(1024*1024),
    GB(1024l*1024*1024),
    TB(1024l*1024*1024*1024);
    final long factor;
    SizeUnit(long factor) {
        this.factor = factor;
    }
    public final static SizeUnit[] VALUES = values();

}
