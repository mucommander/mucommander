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

        // Secret store. Picks an OS keychain backend on macOS,
        // libsecret on Linux, or no-op (the AES-GCM file backend is
        // opt-in via -Dbarebones.secretStore=aes-gcm-file). Must
        // register BEFORE the credentials code in barebones-core's
        // Activator runs — credentials.xml parsing reads the store on
        // first lookup.
        invoke("dev.barebones.commander.secret.Activator", "register");

        // Best-effort cleanup at JVM exit: closes the SecretStore
        // (frees libsecret schema / zeroes AES-GCM key material) and
        // leaves room to add other native-resource releases later.
        Runtime.getRuntime().addShutdownHook(new Thread(Bootstrap::shutdown,
            "barebones-shutdown"));

        // Protocols. S3 reintroduced in Phase 11 on AWS SDK v2; the
        // jets3t-based module from upstream was deleted in Phase 4.
        invoke("dev.barebones.commander.commons.file.protocol.sftp.Activator", "register");
        invoke("dev.barebones.commander.commons.file.protocol.nfs.Activator", "register");
        invoke("dev.barebones.commander.commons.file.protocol.s3.Activator", "register");

        // Phase-10 connectivity helpers. Both register no-op when the
        // backing OS feature isn't available (mount on Windows, tailscale
        // when the binary isn't installed) — the UI hides the related
        // menu items rather than failing.
        invoke("dev.barebones.commander.mount.Activator", "register");
        invoke("dev.barebones.commander.tailscale.Activator", "register");

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
     * JVM shutdown hook: best-effort cleanup of anything that holds
     * resources we need to release explicitly. All steps are
     * reflective so the root project doesn't compile-depend on
     * leaf modules; each step is independently guarded so one
     * failure can't skip the others.
     */
    private static void shutdown() {
        // Best-effort unmount of any mounts we created so the user
        // doesn't find stale mountpoints after a clean exit.
        drainMounts();
        // Close cached S3 connections (releases AWS SDK Netty pools).
        invokeStatic("dev.barebones.commander.commons.file.protocol.s3.Activator", "shutdown");
        // Close the secret store (frees libsecret GObjects, zeroes
        // AES-GCM key material). Last because credentials may be
        // referenced by the modules above.
        closeSecretStore();
    }

    private static void drainMounts() {
        try {
            Class<?> serviceCls = Class.forName("dev.barebones.commander.mount.MountService");
            Object executor = serviceCls.getMethod("executor").invoke(null);
            if (executor == null) {
                return; // platform without mount support
            }
            Class<?> registryCls = Class.forName("dev.barebones.commander.mount.MountRegistry");
            Object registry = registryCls.getMethod("instance").invoke(null);
            Class<?> executorCls = Class.forName("dev.barebones.commander.mount.MountExecutor");
            registryCls.getMethod("drainAtShutdown", executorCls).invoke(registry, executor);
        } catch (ClassNotFoundException notFound) {
            // mount-helper module not present.
        } catch (ReflectiveOperationException ignored) {
            // best-effort
        }
    }

    private static void closeSecretStore() {
        try {
            Class<?> service = Class.forName("dev.barebones.commander.secret.SecretStoreService");
            Object store = service.getMethod("store").invoke(null);
            if (store instanceof AutoCloseable closeable) {
                try {
                    closeable.close();
                } catch (Exception ignored) {
                    // Shutdown — log channels may already be down.
                }
            }
        } catch (ClassNotFoundException notFound) {
            // SecretStore module not present; nothing to close.
        } catch (ReflectiveOperationException ignored) {
            // best-effort
        }
    }

    private static void invokeStatic(String className, String methodName) {
        try {
            Class<?> cls = Class.forName(className);
            cls.getMethod(methodName).invoke(null);
        } catch (ClassNotFoundException notFound) {
            // module not present
        } catch (ReflectiveOperationException ignored) {
            // best-effort
        }
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
