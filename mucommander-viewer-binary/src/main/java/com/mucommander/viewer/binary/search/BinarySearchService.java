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

import javax.annotation.ParametersAreNonnullByDefault;

/**
 * Binary search service.
 */
@ParametersAreNonnullByDefault
public interface BinarySearchService {

    void performFind(SearchParameters dialogSearchParameters, SearchStatusListener searchStatusListener);

    int getMatchPosition();

    void setMatchPosition(int matchPosition);

    int getMatchesCount();

    void performReplace(SearchParameters searchParameters, ReplaceParameters replaceParameters);

    void clearMatches();

    @ParametersAreNonnullByDefault
    public interface SearchStatusListener {

        void setStatus(FoundMatches foundMatches);

        void clearStatus();
    }

    public static class FoundMatches {

        private int matchesCount;
        private int matchPosition;

        public FoundMatches() {
            matchesCount = 0;
            matchPosition = -1;
        }

        public FoundMatches(int matchesCount, int matchPosition) {
            if (matchPosition >= matchesCount) {
                throw new IllegalStateException("Match position is out of range");
            }

            this.matchesCount = matchesCount;
            this.matchPosition = matchPosition;
        }

        public int getMatchesCount() {
            return matchesCount;
        }

        public void setMatchesCount(int matchesCount) {
            this.matchesCount = matchesCount;
        }

        public int getMatchPosition() {
            return matchPosition;
        }

        public void setMatchPosition(int matchPosition) {
            this.matchPosition = matchPosition;
        }

        public void next() {
            if (matchPosition == matchesCount - 1) {
                throw new IllegalStateException("Cannot find next on last match");
            }

            matchPosition++;
        }

        public void prev() {
            if (matchPosition == 0) {
                throw new IllegalStateException("Cannot find previous on first match");
            }

            matchPosition--;
        }
    }
}
