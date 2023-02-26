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

package com.mucommander.snapshot;

import java.util.Arrays;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.commons.conf.Configuration;

/**
 * Configuration snapshoting support for modules.
 *
 * @author Miroslav Hajda, Arik Hadas
 */
public abstract class MuSnapshotable<T>{
    final Logger LOGGER = LoggerFactory.getLogger(getClass());

    /** lists the snapshot properties */
    private Supplier<T[]> ls;
    /** get the value of a snapshot property */
    private Function<T, String> get;
    /** set the value of a snapshot property */
    private BiConsumer<T,String> set;
    /** returns the key in the snapshot.xml file of a snapshot property */
    private Function<T, String> key;

    /**
     * @param ls lists the snapshot properties
     * @param get get the value of a snapshot property
     * @param set set the value of a snapshot property
     * @param key returns the key in the snapshot.xml file of a snapshot property
     */
    protected MuSnapshotable(Supplier<T[]> ls, Function<T, String> get, BiConsumer<T,String> set, Function<T, String> key) {
        this.ls = ls;
        this.get = get;
        this.set = set;
        this.key = key;
    }

    /**
     * Performs loading/reading of snapshot preferences.
     * 
     * @param configuration configuration
     */
    public void read(Configuration configuration) {
        var values = ls.get();
        LOGGER.info("Loading snapshot configuration for: {}", values[0].getClass());
        for (T pref : values) {
            var prefKey = key.apply(pref);
            if (prefKey != null) {
                set.accept(pref, configuration.getVariable(prefKey, get.apply(pref)));
            }
        }
    }
    
    /**
     * Performs storing/writing of snapshot preferences.
     * 
     * @param configuration configuration
     */
    public void write(Configuration configuration) {
        Arrays.stream(ls.get()).forEach(pref -> write(configuration, pref));
    }

    protected void write(Configuration configuration, T pref) {
        var prefKey = key.apply(pref);
        if (prefKey != null) {
            configuration.setVariable(prefKey, get.apply(pref));
        }
    }
}
