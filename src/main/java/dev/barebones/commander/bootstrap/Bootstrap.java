/*
 * Copyright (C) 2026 barebones-commander contributors
 *
 * This program is free software; you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation; either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 */
package dev.barebones.commander.bootstrap;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Map;

/**
 * Plain-Java replacement for the Apache Felix container.
 *
 * Calls each module's {@code Activator.register()} (or, for modules that
 * need the property map, {@code Activator.register(Map)}) in dependency
 * order. Activator classes are resolved by FQN via {@link Class#forName}
 * so the root project does not need a compile-time dep on every leaf
 * module — only the runtime classpath needs them, which is what
 * {@code runtimeOnly project(...)} in the root build.gradle provides.
 *
 * Order matters: commons-file's no-op activator first, then translator /
 * preferences / preload, then producers (protocols, formats, viewer),
 * then OS adapters, then core last (its {@code register()} ends up
 * showing the Swing UI).
 */
public final class Bootstrap {

    private Bootstrap() {
    }

    public static void start(Map<String, String> properties) {
        // Core SPI / no-op
        invoke("dev.barebones.commander.commons.file.osgi.Activator", "register");

        // Basic services
        invoke("dev.barebones.commander.text.Activator", "register");
        invoke("dev.barebones.commander.conf.Activator", "register", Map.class, properties);
        invoke("dev.barebones.commander.preload.Activator", "register");

        // Protocols (S3 module is removed in Phase 4 pending its own dedicated
        // AWS-SDK-v2 reintroduction phase — see PLAN.md).
        invoke("dev.barebones.commander.commons.file.protocol.sftp.Activator", "register");
        invoke("dev.barebones.commander.commons.file.protocol.nfs.Activator", "register");

        // Archive formats
        invoke("dev.barebones.commander.commons.file.archive.zip.Activator", "register");
        invoke("dev.barebones.commander.commons.file.archive.tar.Activator", "register");
        invoke("dev.barebones.commander.commons.file.archive.gzip.Activator", "register");
        invoke("dev.barebones.commander.commons.file.archive.bzip2.Activator", "register");
        invoke("dev.barebones.commander.commons.file.archive.xz.Activator", "register");

        // Text viewer
        invoke("dev.barebones.commander.viewer.text.Activator", "register");

        // OS adapters — only one will load on a given JVM (the wrong-OS
        // jar can be excluded from per-OS installer images).
        invoke("dev.barebones.commander.desktop.linux.Activator", "register");
        invoke("dev.barebones.commander.desktop.macos.Activator", "register");

        // Core — instantiated with the property map; its register() shows the UI.
        instantiateAndRegister("dev.barebones.commander.Activator", properties);
    }

    /**
     * Calls a static {@code register(...)} on the named class. If the
     * class is not on the runtime classpath (for example, the wrong-OS
     * adapter), the call is silently skipped — the same tolerance the
     * Felix discoverer used to give us.
     */
    private static void invoke(String className, String methodName, Object... typedArgs) {
        Class<?> cls;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException notFound) {
            return; // module not present on this classpath
        }
        Method method;
        Object[] args;
        try {
            if (typedArgs.length == 0) {
                method = cls.getDeclaredMethod(methodName);
                args = new Object[0];
            } else {
                Class<?>[] paramTypes = new Class<?>[typedArgs.length / 2];
                args = new Object[typedArgs.length / 2];
                for (int i = 0; i < typedArgs.length; i += 2) {
                    paramTypes[i / 2] = (Class<?>) typedArgs[i];
                    args[i / 2] = typedArgs[i + 1];
                }
                method = cls.getDeclaredMethod(methodName, paramTypes);
            }
            method.setAccessible(true);
            method.invoke(null, args);
        } catch (NoSuchMethodException | IllegalAccessException e) {
            throw new IllegalStateException("Activator " + className + " missing register(...)", e);
        } catch (InvocationTargetException e) {
            Throwable cause = e.getCause();
            throw new IllegalStateException("Activator " + className + " failed to register", cause != null ? cause : e);
        }
    }

    /**
     * Instantiates the core Activator with the property map and calls
     * its {@code register()}. The core Activator is the only one that
     * needs an instance: its accessor methods (assoc(), bookmark(), ...)
     * read CLI args from the map, and {@code Application.run(activator)}
     * holds the instance for the lifetime of the UI.
     */
    private static void instantiateAndRegister(String className, Map<String, String> properties) {
        Class<?> cls;
        try {
            cls = Class.forName(className);
        } catch (ClassNotFoundException notFound) {
            throw new IllegalStateException("Required core Activator " + className + " not on classpath", notFound);
        }
        try {
            Object activator = cls.getDeclaredConstructor(Map.class).newInstance(properties);
            cls.getMethod("register").invoke(activator);
        } catch (ReflectiveOperationException e) {
            Throwable cause = e instanceof InvocationTargetException && e.getCause() != null ? e.getCause() : e;
            throw new IllegalStateException("Failed to start core Activator", cause);
        }
    }
}
