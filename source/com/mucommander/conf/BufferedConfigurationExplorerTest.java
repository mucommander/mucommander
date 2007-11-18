/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2007 Maxence Bernard
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

package com.mucommander.conf;

/**
 * A test case for the {@link BufferedConfigurationExplorer} class.
 * @author Nicolas Rinaudo
 */
public class BufferedConfigurationExplorerTest extends ConfigurationExplorerTest {
    // - Helper methods ------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Returns an instance of BufferedConfigurationExplorer.
     * @return an instance of BufferedConfigurationExplorer.
     */
    protected ConfigurationExplorer getExplorer() {return new BufferedConfigurationExplorer(conf.getRoot());}

    /**
     * Backtracks through the section history and makes sure it contains the correct values.
     */
    private void backtrack(BufferedConfigurationExplorer explorer, int depth) {
        for(int i = depth; i > 0; i--) {
            assertTrue(explorer.hasSections());
            assertEquals(VARIABLE_VALUE + i, explorer.popSection().getVariable(VARIABLE_NAME + i));
        }
        assertFalse(explorer.hasSections());
    }



    // - Test code -----------------------------------------------------------------------
    // -----------------------------------------------------------------------------------
    /**
     * Tests path bufferisation when the requested sections are found.
     */
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
    public void testSectionNotFoundBuffer() {
        BufferedConfigurationExplorer explorer;

        for(int i = 0; i < DEPTH; i++) {
            moveTo(explorer = (BufferedConfigurationExplorer)getExplorer(), i);

            // Makes sure that a failed moveTo call doesn't corrupt section history.
            assertFalse(explorer.moveTo(FAKE_SECTION + i, false));
            backtrack(explorer, i);
        }
    }

    /**
     * Tests path bufferisation when the requested sections are not found but created.
     */
    public void testSectionNotFoundAndCreateBuffer() {
        BufferedConfigurationExplorer explorer;

        for(int i = 0; i < DEPTH; i++) {
            moveTo(explorer = (BufferedConfigurationExplorer)getExplorer(), i);

            // Creates the section, makes sure it was created and takes it off
            // the section stack.
            assertTrue(explorer.moveTo(FAKE_SECTION + i, true));
            assertTrue(explorer.hasSections());
            assertNotNull(explorer.popSection());

            // Makes sure backtracking through history works.
            backtrack(explorer, i);
        }
    }
}
