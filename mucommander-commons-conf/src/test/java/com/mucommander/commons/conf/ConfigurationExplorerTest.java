/**
 * This file is part of muCommander, http://www.mucommander.com
 * Copyright (C) 2002-2016 Maxence Bernard
 * <p>
 * muCommander is free software; you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * <p>
 * muCommander is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * <p>
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package com.mucommander.commons.conf;

import org.testng.annotations.BeforeMethod;
import org.testng.annotations.Test;

/**
 * A test case for the {@link ConfigurationExplorer} class.
 * @author Nicolas Rinaudo
 */
public class ConfigurationExplorerTest {
    // - Test constants ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Root name of test sections. */
    protected static final String SECTION_NAME = "section";
    /** Root name of test variables. */
    protected static final String VARIABLE_NAME = "variable";
    /** Root name of test values. */
    protected static final String VARIABLE_VALUE = "value";
    /** Name for non-existing sections. */
    protected static final String FAKE_SECTION = "fake";
    /** Depth of the tests. */
    protected static final int DEPTH = 4;


    // - Instance variables --------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------
    /** Configuration used for tests. */
    protected Configuration conf;


    // - Initialisation ------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Fills the configuration instance with test values.
     */
    @BeforeMethod
    public void setUp() {
        StringBuilder buffer;

        buffer = new StringBuilder();
        conf = new Configuration();
        for (int i = 0; i < DEPTH; i++) {
            conf.setVariable(buffer.toString() + VARIABLE_NAME + i, VARIABLE_VALUE + i);
            buffer.append(SECTION_NAME);
            buffer.append(i);
            buffer.append('.');
        }
    }


    // - Tests ---------------------------------------------------------------------------------------------------------
    // -----------------------------------------------------------------------------------------------------------------

    /**
     * Returns a configuration explorer on the test section.
     * @return a configuration explorer on the test section.
     */
    protected ConfigurationExplorer getExplorer() {
        return new ConfigurationExplorer(conf.getRoot());
    }

    /**
     * Moves to the specified depth.
     * @param  explorer explorer to use when moving to the specified depth.
     * @param  depth    depth in the configuration tree at which to move.
     * @return section that was found at the specified depth.
     */
    protected ConfigurationSection moveTo(ConfigurationExplorer explorer, int depth) {
        ConfigurationSection section;

        section = conf.getRoot();
        for (int i = 0; i < depth; i++) {
            assert explorer.moveTo(SECTION_NAME + i, false);
            section = explorer.getSection();
        }

        return section;
    }

    /**
     * Tests the {@link ConfigurationExplorer#moveTo(String, boolean)} method.
     */
    private void assertSectionNotFound(boolean create) {
        ConfigurationExplorer explorer;
        ConfigurationSection section;

        // Checks what happens when sections are not found and create is set to false.
        for (int i = 0; i < DEPTH; i++) {
            section = moveTo(explorer = getExplorer(), i);

            // Makes sure the 'fake' section doesn't exist.
            assert !explorer.moveTo(FAKE_SECTION + i, false);

            if (create) {
                // Tries to create the section and makes sure the explorer
                // did move to it.
                assert explorer.moveTo(FAKE_SECTION + i, true);
                assert section.getSection(FAKE_SECTION + i).equals(explorer.getSection());
            } else
                // Makes sure the explorer didn't change section.
                assert explorer.getSection().equals(section);
        }
    }

    /**
     * Tests configuration navigation to non-existing sections (without section creation).
     */
    @Test
    public void testSectionNotFoundWithoutCreate() {
        assertSectionNotFound(false);
    }

    /**
     * Tests configuration navigation to non-existing sections (with section creation).
     */
    @Test
    public void testSectionNotFoundWithCreate() {
        assertSectionNotFound(true);
    }

    /**
     * Test configuration navigation to existing sections.
     */
    @Test
    public void testSectionFound() {
        ConfigurationExplorer explorer;

        for (int i = 0; i < DEPTH; i++) {
            moveTo(explorer = getExplorer(), i);
            assert (VARIABLE_VALUE + i).equals(explorer.getSection().getVariable(VARIABLE_NAME + i));
        }
    }
}
