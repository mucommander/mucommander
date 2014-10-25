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

package com.mucommander.commons.conf;

import org.testng.annotations.Test;

/**
 * A test case for the {@link BufferedConfigurationExplorer} class.
 * @author Nicolas Rinaudo
 */
public class BufferedConfigurationExplorerTest extends ConfigurationExplorerTest {
    // - Helper methods ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Returns an instance of {@link BufferedConfigurationExplorer}.
     * @return an instance of {@link BufferedConfigurationExplorer}.
     */
    @Override
    protected ConfigurationExplorer getExplorer() {
        return new BufferedConfigurationExplorer(conf.getRoot());
    }

    /**
     * Backtracks through the section history and makes sure it contains the correct values.
     */
    private void backtrack(BufferedConfigurationExplorer explorer, int depth) {
        for(int i = depth; i > 0; i--) {
            assert explorer.hasSections();
            assert (VARIABLE_VALUE + i).equals(explorer.popSection().getVariable(VARIABLE_NAME + i));
        }
        assert !explorer.hasSections();
    }



    // - Test code -----------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /**
     * Tests path bufferisation when the requested sections are found.
     */
    @Test
    public void testSectionFoundBuffer() {
        BufferedConfigurationExplorer explorer;

        for(int i = 0; i < DEPTH; i++) {
            moveTo(explorer = (BufferedConfigurationExplorer)getExplorer(), i);
            backtrack(explorer, i);
        }
    }

    /**
     * Tests path bufferisation when the requested sections are not found.
     */
    @Test
    public void testSectionNotFoundBuffer() {
        BufferedConfigurationExplorer explorer;

        for(int i = 0; i < DEPTH; i++) {
            moveTo(explorer = (BufferedConfigurationExplorer)getExplorer(), i);

            // Makes sure that a failed moveTo call doesn't corrupt section history.
            assert !explorer.moveTo(FAKE_SECTION + i, false);
            backtrack(explorer, i);
        }
    }

    /**
     * Tests path bufferisation when the requested sections are not found but created.
     */
    @Test
    public void testSectionNotFoundAndCreateBuffer() {
        BufferedConfigurationExplorer explorer;

        for(int i = 0; i < DEPTH; i++) {
            moveTo(explorer = (BufferedConfigurationExplorer)getExplorer(), i);

            // Creates the section, makes sure it was created and takes it off
            // the section stack.
            assert explorer.moveTo(FAKE_SECTION + i, true);
            assert explorer.hasSections();
            assert explorer.popSection() != null;

            // Makes sure backtracking through history works.
            backtrack(explorer, i);
        }
    }
}
