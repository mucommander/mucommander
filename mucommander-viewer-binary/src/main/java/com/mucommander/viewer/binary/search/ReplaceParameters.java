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
package com.mucommander.viewer.binary.search;

import javax.annotation.Nonnull;
import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Parameters for action to replace for occurrences of text or data.
 */
@ParametersAreNonnullByDefault
public class ReplaceParameters {

    private SearchCondition condition = new SearchCondition();
    private boolean performReplace;
    private boolean replaceAll;

    public ReplaceParameters() {
    }

    @Nonnull
    public SearchCondition getCondition() {
        return condition;
    }

    public void setCondition(SearchCondition condition) {
        this.condition = condition;
    }

    public void setFromParameters(ReplaceParameters replaceParameters) {
        condition = replaceParameters.getCondition();
    }

    public boolean isPerformReplace() {
        return performReplace;
    }

    public void setPerformReplace(boolean performReplace) {
        this.performReplace = performReplace;
    }

    public boolean isReplaceAll() {
        return replaceAll;
    }

    public void setReplaceAll(boolean replaceAll) {
        this.replaceAll = replaceAll;
    }
}
