/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2010 Maxence Bernard
 *
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.file.filter;

import com.mucommander.commons.file.AbstractFile;

/**
 * This interface defines a {@link #getCriterionValue(AbstractFile)} method that generates a criterion value for
 * a specified {@link AbstractFile}. It is used by {@link CriterionFilter} to match files based on their criteria
 * values.
 *
 * @see CriterionFilter
 * @author Maxence Bernard
 */
public interface CriterionValueGenerator<C> {

    public C getCriterionValue(AbstractFile file);
}
