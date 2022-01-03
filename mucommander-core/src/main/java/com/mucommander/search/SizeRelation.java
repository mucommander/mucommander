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
public enum SizeRelation {
    eq(0,"=") {
        @Override
        public boolean matches(long sz, long limit, SizeUnit unit) {
            return sz == limit * unit.factor;
        }
    },
    lt(1,"<"){
        @Override
        public boolean matches(long sz, long limit, SizeUnit unit) {
            return sz < limit * unit.factor;
        }
    },
    gt(2,">"){
        @Override
        public boolean matches(long sz, long limit, SizeUnit unit) {
            return sz > limit * unit.factor;
        }
    };

    final int index;
    final String title;
    SizeRelation(int index, String title) { this.index = index; this.title = title; }

    // title for combo box display
    public String toString() {
        return title;
    }

    public final static SizeRelation[] VALUES = values();

    abstract public boolean matches(long sz, long limit, SizeUnit searchSizeUnit);

}
