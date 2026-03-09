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
package com.mucommander.text;

import java.util.Collections;
import java.util.Locale;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * A test case for {@link Translator}
 *
 * @author Nicolas Filotto (nicolas.filotto@gmail.com)
 */
public class TranslatorTest {

    /**
     * Initializes the Translator.
     */
    @BeforeAll
    public static void init() {
        Locale locale = Activator.loadLocale();
        Translator.init(Activator.getDictionaryBundle(locale), Activator.getLanguageBundle(locale), Collections.emptyList());
    }

    /**
     * Translator can reuse keys
     */
    @Test
    public void reusesKey() {
        assert "Hello".equals(Translator.get("key1"));
        assert "World".equals(Translator.get("key2"));
        assert "Hello".equals(Translator.get("key3"));
        assert "\"Hello the World!\"".equals(Translator.get("key4"));
        assert "\"Hello the $[key0]!\"".equals(Translator.get("key5"));
        assert "-\"Hello the World!\"-\"Hello the $[key0]!\"-$[key7]-".equals(Translator.get("key6"));
    }
}
