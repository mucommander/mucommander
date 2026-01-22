/**
 * This file is part of muCommander, http://www.mucommander.com
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
package com.mucommander.module;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.ServiceLoader;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.mucommander.viewer.FileEditorService;

/**
 * Registration tracker for file editor service.
 *
 * @author Miroslav Hajda
 */
public class FileEditorsLoader {

    private static final Logger LOGGER = LoggerFactory.getLogger(FileEditorsLoader.class);

    private static final List<FileEditorService> SERVICES = new ArrayList<>();

    public static void load() {
        for (FileEditorService service : ServiceLoader.load(FileEditorService.class)) {
            addEditorService(service);
            LOGGER.info("FileEditorService is registered: " + service);
        }
    }

    private static void addEditorService(FileEditorService service) {
        SERVICES.add(service);
        SERVICES.sort(Comparator.comparing(FileEditorService::getOrderPriority).reversed());
    }

    public static List<FileEditorService> getEditorServices() {
        return SERVICES;
    }
}
