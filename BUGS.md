# BUGS.md — bug & improvement backlog

Compiled by sweeping the codebase after Phase 12 landed. Each item
includes a file:line pointer where useful and a one-line rationale
so triage is cheap. Severity buckets:

- **HIGH** — correctness, security, or data-loss risk. Fix soon.
- **MED**  — broken-but-not-on-fire behaviour, observable bugs,
  resource leaks that take a long-running session to bite.
- **LOW**  — quality / hygiene / consistency.

> **Architecture & refactor sections are review-only.** Do NOT change
> the architecture or run a refactoring sweep based on these notes
> without explicit approval — the user reviews and decides.

---

## 0. Source-code accounting

|                       | Files (.java) | LOC      |
|-----------------------|--------------:|---------:|
| **Upstream baseline** at fork point (`8662144bb`, all `.java`)              |  1,352 | 223,391 |
| upstream non-test                                                              |  1,291 | 212,254 |
| **Current barebones-commander** (post Phase 12, all `.java`)                    |  1,189 | 194,887 |
| current non-test                                                                |  1,128 | 183,368 |
| current test                                                                    |     61 |  11,519 |
| **Net delta** (non-test)                                                        | **−163** | **−28,886 (−13.6%)** |

What "−13.6% non-test LOC" really represents:
- Out-of-scope module deletions in Phase 1 (HTTP, SMB, FTP, Dropbox,
  GDrive, OneDrive, GCS, Azure, Hadoop, vSphere, ADB, Win-Registry,
  RAR, 7z, ISO, RPM, cpio, ar, lst, libguestfs, terminal, JediTerm).
- The Apache Felix OSGi runtime (Phase 2).
- The legacy jets3t S3 module (Phase 4) — partly re-added by Phase 11
  on the AWS SDK v2 (~1,400 LOC of new module + ~600 LOC of tests).

What grew:
- 6 brand-new modules: `barebones-mount-helper`, `barebones-tailscale`,
  `barebones-secret-store`, `barebones-protocol-s3` + Phase-7 build
  scaffolding + Phase-8 `release.yaml`.
- Test count rose from ~11k → 11.5k (small, but every Phase-since-9
  PR added tests rather than the historical "smoke test only" pattern).

---

## 1. Real bugs

### 1.1 HIGH — Zip-slip / TAR-slip on extraction
**`barebones-format-zip/.../ZipArchiveFile.java:112-147`**,
**`barebones-format-tar/.../TarEntryIterator.java:97-116`**

Entry paths are taken straight from `ZipEntry.getName()` /
`TarEntry.getName()` and turned into `ArchiveEntry`s with no `..`
normalisation, no leading-`/` rejection, no Windows `\` rejection.
A crafted archive with `../../etc/passwd` writes outside the
extraction root.

### 1.2 HIGH — SFTP host-key verification not configured
**`barebones-protocol-sftp/.../SFTPConnectionHandler.java:88-94`**

`session.connect()` is reached with no `setConfig("StrictHostKeyChecking","yes")`,
no known-hosts file, no host-key repository. JSch defaults to "ask",
which is bypassable. MITM-able on any non-trusted network.

### 1.3 HIGH — Static mutable collections without synchronisation
**`barebones-core/.../auth/CredentialsManager.java`** (vector),
**`barebones-core/.../bookmark/BookmarkManager.java:52`** (`Vector`),
**`barebones-core/.../ui/action/ActionProperties.java:42`** (`Hashtable`).

Pre-Java-5 `Vector`/`Hashtable` provide method-level synchronisation
but iteration / read-then-write sequences race. Bookmark or
credential corruption possible under concurrent access (file watcher
+ user save).

### 1.4 HIGH — NFS / Sun-vendored RPC has no timeouts
**`barebones-protocol-nfs/src/main/java/com/sun/nfs/`** (multiple files)

The vendored Sun NFS code uses raw socket ops with no timeouts.
A hung NFS server blocks the calling thread forever; if that thread
is the EDT, the whole UI freezes. Not easily fixable without
patching vendored code.

### 1.5 HIGH — TransferFileJob has no network timeouts
**`barebones-core/.../job/impl/TransferFileJob.java`**, plus the
underlying SFTP connect:
**`barebones-protocol-sftp/.../SFTPConnectionHandler.java:91`**

Connect timeout is 5 s (probably too short on slow VPN), but the
ongoing transfer has no socket-read timeout — a half-open TCP
connection wedges the copy-job thread indefinitely.

### 1.6 HIGH — macOS Keychain item-ref leaked on every successful lookup
**`barebones-secret-store/.../macos/KeychainSecretStore.java:80-104`**

`SecKeychainFindGenericPassword` returns an `itemRef` that needs
`CFRelease`, but `lookup()` never frees it on the success path
(only `deleteByLookup()` releases its own ref). One leak per
lookup → accumulates over a long session.

### 1.7 HIGH — libsecret schema pointer never unref'd
**`barebones-secret-store/.../linux/LibsecretSecretStore.java:46-50`**

`secret_schema_new()` returns a refcounted GObject that needs
`secret_schema_unref()`. The constructor stores it in a field and
never frees it. One GObject per `LibsecretSecretStore` instance,
forever.

### 1.8 HIGH — AES-GCM derived key never zeroed
**`barebones-secret-store/.../aesgcm/AesGcmFileSecretStore.java:93-94`**

PBKDF2-derived key sits in a `byte[] keyMaterial` field for the
object's lifetime. `Arrays.fill(keyMaterial, (byte)0)` never runs.
A heap dump exposes the key. The store has no `close()`.

### 1.9 HIGH — `equals()` without `hashCode()` on `Bookmark` and `CredentialsMapping`
**`barebones-core/.../bookmark/Bookmark.java:127-134`**,
**`barebones-core/.../auth/CredentialsMapping.java`** (per the SpotBugs baseline)

Putting these into a `HashMap`/`HashSet` silently produces wrong
results. `BookmarkManager` keeps them in a `Vector` so the bug is
latent today, but a future contributor switching to a `Map` ships a
broken release.

### 1.10 MED — `ZipInputStream` leaked on iteration error
**`barebones-format-zip/.../ZipArchiveFile.java:250-258`**

If an exception is thrown mid-iteration the stream is never closed
in a finally. Each failed lookup leaks an FD.

### 1.11 MED — S3 connection-cache key contains plaintext access+secret keys
**`barebones-protocol-s3/.../S3ProtocolProvider.java:67-68`**

Cache key is `host|port|region|accessKey|pathStyle|useHttps` — but
the code path I added accidentally also uses the secret key for
disambiguation in some places. Anywhere the key string is logged,
serialised, or appears in a heap dump, the secret is exposed.
Fix: hash credentials (SHA-256) into the cache key.

### 1.12 MED — `S3TransferManager.completionFuture().join()` blocks EDT
**`barebones-protocol-s3/.../S3Object.java:329-330`**

`SpillingPutOutputStream.close()` is reachable from a Swing copy-job
on the EDT. A 40 MiB upload then freezes the UI for the full upload
duration. Needs a SwingWorker shim like Phase-10c's `MountTask`.

### 1.13 MED — Decompression-bomb / per-entry size limits absent
**`barebones-format-zip/`**, **`barebones-format-tar/`**, **`barebones-archiver/`**

No per-entry or cumulative size cap during extraction. A 1 MB zip
that expands to 50 GB will exhaust memory or disk. Same for entry
count: a million-entry zip parses its central directory unbounded.

### 1.14 MED — Text viewer loads entire file into memory
**`barebones-viewer-text/.../TextViewer.java`**

No size check before handing the bytes to `RSyntaxTextArea`.
Opening a multi-GB log file crashes the JVM.

### 1.15 MED — `AbstractArchiveFile.createEntriesTree()` is not thread-safe (TODO admits)
**`barebones-commons-file/.../AbstractArchiveFile.java:122`**

Multiple threads calling `ls()` simultaneously can race on the
shared tree-build state. Latent because the file table mostly
serialises calls, but parallel directory listings hit it.

### 1.16 MED — `AppleScript.outputBuffer` is unbounded
**`barebones-os-macos/.../AppleScript.java:181-236`**

A misbehaving / hostile AppleScript that emits forever exhausts
heap. Add a hard ceiling (e.g. 1 MiB) and truncate with a marker.

### 1.17 MED — Polling loops with `Thread.sleep` on the EDT
- **`barebones-core/.../ui/dialog/file/PropertiesDialog.java:185-189`** (100 ms loop)
- **`barebones-core/.../core/FolderChangeMonitor.java:177-218`** (TODO admits this)
- **`barebones-core/.../ui/quicksearch/QuickSearch.java:362`** (100 ms loop)
- **`barebones-core/.../ui/text/CompletionType.java:110`**

All freeze the EDT in 100-ms ticks. `wait()/notify()` or a Timer
fixes each.

### 1.18 MED — S3 `isDirectory()` / `exists()` swallow IOException → false-negative
**`barebones-protocol-s3/.../S3Object.java:96-116`**

`HeadObject` returning a transient 5xx makes the file look like it
doesn't exist; the user sees their files vanish until refresh.

### 1.19 MED — S3 connection cache grows unbounded, never closed
**`barebones-protocol-s3/.../S3ProtocolProvider.java:43`**

`ConcurrentHashMap<String, S3Connection>` never evicts. No
`close()` on the provider; on shutdown every cached `S3Client` +
`S3AsyncClient` + `S3TransferManager` is leaked.

### 1.20 MED — 31+ empty catch blocks in `barebones-core`
Sample sites: `ThemeManager.java:495,532,685,817`,
`EditBookmarksDialog.java:248,334`, `ClipboardSupport.java:55`,
`AddBookmarkDialog`, `LicenseDialog`, `PropertiesDialog`,
`AppearancePanel`, `BookmarkManager.java:184`,
`ActionKeymapReader.java:76`. Auth / bookmark / keymap silently
fall back to defaults on parse failure.

### 1.21 MED — `EditBookmarksDialog.java:381` NPE on empty selection
`bookmarkList.getSelectedValue()` is dereferenced without a null
check; clicking the action with no selection NPEs.

### 1.22 MED — `ThemeManager` stream-leak pattern
**`barebones-core/.../ui/theme/ThemeManager.java:489,528,685,817`**

`try { writeThemeData(data, out = new FileOutputStream(file)); }` —
if the constructor throws after assignment but before the body,
the stream leaks. Use try-with-resources.

### 1.23 MED — `LocalFile` channel-extraction leak
**`barebones-commons-file/.../LocalFile.java:683,693,703`**

`new LocalInputStream(new FileInputStream(file).getChannel())` —
if `getChannel()` throws, the `FileInputStream` is unreachable and
unclosed.

### 1.24 MED — Mount username with `=` or `,` injects mount options
**`barebones-mount-helper/.../LinuxMountCommand.java:67`**

For SMB the argv slot is `"user=" + spec.username()`. ProcessBuilder
prevents shell injection, but `mount.cifs` interprets the `-o`
value as comma-separated options. A username `alice,uid=0` would
inject `uid=0`. `MountSpec` should reject these characters.

### 1.25 MED — Tailscale process stderr read after `waitFor`, can deadlock on big stderr
**`barebones-tailscale/.../TailscaleClient.java:117-120`**

For commands that fill stderr beyond pipe-buffer size (~64 KB),
`waitFor` deadlocks waiting for stderr to drain while we wait for
the process to exit. Read stderr concurrently with the wait or use
`redirectErrorStream(true)` plus stream-tee.

### 1.26 LOW — `System.err.println` in `Application.java:142,144`
CLI code, but inconsistent with logger usage everywhere else.

### 1.27 LOW — `EncodingDetector.main()` writes to `System.out`
**`barebones-commons-io/.../EncodingDetector.java:187`**. Looks
like leftover debug code in production sources.

### 1.28 LOW — `ZipArchiveFile.java:154` hard-codes UTF-8 for symlink targets
Zip spec allows non-UTF-8; an EFS-flagged entry is fine but legacy
encoded ones (CP932 etc) round-trip wrong.

---

## 2. UX gaps

### 2.1 No progress for S3 multipart uploads
`SpillingPutOutputStream` blocks at `completionFuture().join()`
with no progress callback wired in. AWS SDK v2 emits
`TransferListener` events — surface them in a `JProgressBar` like
`MountTask` does.

### 2.2 No progress for folder browses / large directory listings
Loading a 50k-entry SFTP directory freezes the panel; no spinner
or partial-load indicator.

### 2.3 Destructive ops missing confirmation
- Bookmark deletion is currently *disabled* per a TODO in
  `BookmarkManager.java:228` ("quick fix for #329"). Re-enable
  with a confirmation prompt.
- Batch rename has no preview-before-apply.

### 2.4 "Operation failed" with no root cause
`TransferFileJob` and several siblings catch the underlying
exception and surface a generic translator string. Reveal the
root cause in an expandable detail section.

### 2.5 Mount errors don't suggest next step
"Operation not permitted" on `mount.nfs` should hint *try
sudo / use the system mount helper / install nfs-common*.
Currently the user sees raw stderr.

### 2.6 S3 errors don't distinguish 401 / 403 / 404
All wrap into a generic `IOException`. The user can't tell whether
to fix credentials, fix the bucket name, or check IAM.

### 2.7 Tailscale "not installed" surfaces only when invoked
`TailscalePeerPanel` reads it from status, but the menu item itself
doesn't disable / hide. A startup banner or a disabled menu item
would be clearer than the per-action error.

### 2.8 Preferences dialog: Cancel doesn't revert
`AppearancePanel`, `ShortcutsPanel` apply changes immediately. The
Cancel button is misleading.

### 2.9 Dialogs without default-button focus
`InformationDialog`, `QuestionDialog` open with no preselected
button; pressing Enter does nothing until you tab.

### 2.10 No file-size prompt before opening huge archives / files
Both archive opening and the text viewer happily try to load a
multi-GB blob and freeze.

### 2.11 Keychain prompts unexpected for first-time users
Phase 12: a new install on macOS pops the keychain authorisation
prompt the first time credentials are saved. A status-bar one-liner
explaining what's happening would help.

### 2.12 Drag-and-drop doesn't validate target writability
**`barebones-core/.../ui/dnd/FileDropTargetListener.java`** accepts
the drop and only fails after the user releases. Reject the drop
gesture if target is read-only.

---

## 3. Logging gaps

### 3.1 S3 module has zero log lines
None of `S3File`, `S3Bucket`, `S3Object`, `S3Listing`,
`S3ProtocolProvider` calls `LOGGER.*`. Diagnosing user reports
("my upload hangs") is blind. Match the SFTP module's pattern.

### 3.2 `MountExecutor` doesn't log stderr on failure
`MountResult.stderr()` is captured but never auto-logged. Headless
runs lose the failure reason.

### 3.3 Tailscale timeout messages drop context
"tailscale file cp timed out after 5s" — but which peer, which
file? Include argv (sanitised) in the timeout exception.

### 3.4 `CredentialsMapping.toString()` may dump full URL with credentials
**`barebones-core/.../auth/CredentialsMapping.java`** — used in
several `LOGGER.info` lines via `+ url`. If the URL embeds
`user:pass@host`, that lands in logs. Override `toString()` to mask
the password segment.

### 3.5 `ThemeManager` exception sites lack file paths
**`ThemeManager.java:495,532,563,593,823`** catch and log
generically; the actual theme-file path is not in the message.

### 3.6 `AppleScript` decoder doesn't log invalid sequences
**`barebones-os-macos/.../AppleScript.java:196-227`** — the
`carryover` buffer falls into the `REPLACE` branch silently; a
DEBUG line would help diagnose macOS encoding regressions like
the chunk-boundary bug.

### 3.7 SFTP authentication failures logged at `info` instead of `warn`
**`barebones-protocol-sftp/.../SFTPConnectionHandler.java:98,110`**
— failed auth is the user-visible failure mode; `warn` is more
appropriate for triage.

---

## 4. Reliability gaps

### 4.1 No timeouts on libsecret D-Bus calls
**`barebones-secret-store/.../linux/LibsecretSecretStore.java:66-121`**
— `secret_password_*_sync` calls block indefinitely if the Secret
Service daemon hangs. Use the existing `cancellable` parameter
with a `GCancellable` we time out via a watchdog timer.

### 4.2 No retry / backoff on transient mount failures
A flaky NFS portmap rejects the first `mount.nfs` and the user has
to click Mount again. Most mount workflows include an internal
retry. Wrap with bounded retry + exponential backoff.

### 4.3 S3 connection cache never closes connections (see 1.19)

### 4.4 AES-GCM key never zeroed on close (see 1.8)

### 4.5 `WeakHashMap`-keyed listeners GC'd silently
**`barebones-core/.../ui/theme/ThemeManager.java:73`** and similar.
A listener registered from an anonymous inner class loses its
strong reference and stops firing. Use `EventListenerList` /
`ListenerSupport`.

### 4.6 No registered shutdown hook for cleanup
`ShutdownHook` exists in core but its registration site is unclear.
Mount-helper temp files, S3 clients, secret-store key material —
none get cleaned up on JVM exit.

### 4.7 S3 `SpillingPutOutputStream` temp file: deletion-error masks upload error
**`barebones-protocol-s3/.../S3Object.java:319-338`** — if the
finally's `Files.deleteIfExists` throws, it shadows the original
upload exception. Catch + log the deletion failure, never let it
escape from the finally.

### 4.8 NFS code → see 1.4

### 4.9 SFTP fixed 5s connect timeout (see 1.5) — make configurable

---

## 5. Architecture observations *(REVIEW-ONLY — do NOT change without approval)*

### 5.1 `AbstractFile` is a 2,022-line god-object
**`barebones-commons-file/.../AbstractFile.java`** combines:
attributes, I/O, URL handling, archive entry caching, comparator
hooks, custom-property bag. ~30 abstract methods, many of which
are correctly `UnsupportedFileOperationException` for non-POSIX
backends but make the contract enormous.

### 5.2 Other large UI classes
| File | LOC |
|------|----:|
| `MainFrame.java` | 1,951 |
| `ThemeManager.java` | 1,114 |
| `FileTable.java` | 958 |
| `FileTableModel.java` | 869 |
| `DesktopManager.java` | 654 |

Each mixes orchestration with leaf logic; classic
"refactor candidate but not a bug" set.

### 5.3 OSGi naming persists after Phase 2 dropped the runtime
`dev.barebones.commander.commons.file.osgi.*` package names,
`Activator` class names, `register()` static-method discovery via
`Class.forName`. The runtime is a plain reflection driver in
`Bootstrap.java`; the names mislead a new contributor into looking
for a Felix container that no longer exists.

### 5.4 Per-format Activator pattern is boilerplate
Each of `barebones-format-{zip,tar,gzip,bzip2,xz}` has its own
`Activator.register()` doing essentially the same thing. A single
`ServiceLoader<FormatProvider>` would eliminate the boilerplate
and let new formats register without touching `Bootstrap.java`.

### 5.5 Connectivity panels don't belong in `barebones-protocol-*`
`MountPanel`, `TailscalePeerPanel` live next to `S3Panel` /
`SFTPPanel` even though they aren't real protocols (they shell out
to the OS / build a redirect URL). Better to live in a
`barebones-ui-connect` module that depends on
`barebones-protocol-api`.

### 5.6 Action system: 100+ enum entries × 100+ classes × Map<String,Object> params
**`barebones-os-api/.../ActionType.java`** + every
`ui/action/impl/*Action.java`. The `Map<String, Object>` parameter
bag is loose-typed; sealed interface or one class per param shape
would be safer. New actions force ActionType-enum edits across
modules.

### 5.7 Reflection-based Bootstrap discovery
**`src/main/java/.../bootstrap/Bootstrap.java`** uses
`Class.forName("dev.barebones....Activator").register()` to load
every module. A `java.util.ServiceLoader` registry with explicit
provider files would be more idiomatic and gives compile-time
checking. (The current pattern is intentional after Phase 2 — it
keeps the root project from compile-depending on every leaf — but
it pays at runtime in error messages.)

### 5.8 Vendored `apache-bzip2` module
A copy of Apache Commons Compress's bzip2 lives as its own module.
A direct dep on `org.apache.commons:commons-compress` (already
pulled in by `barebones-archiver`!) would let us delete the whole
sub-project. The vendored copy never gets the upstream's bzip2
fixes.

### 5.9 `com.sun.*` internals in `barebones-protocol-nfs` + `sun-net-www`
Vendored from old Sun source trees. Internal-API style; portability
risk across JDK upgrades. Phase 3 (Java 25 LTS) survived it, but a
JDK 30 upgrade may not.

### 5.10 Hard-coded magic numbers across UI
`REFRESH_RATE`, `TICK`, `POPUP_DELAY`, `CELL_EDITING_STATE_PERIOD`,
SwingWorker timeouts, mount timeouts (30s), tailscale timeout (5s),
SFTP connect (5s) — all sprinkled across files. A central
`Tunables` class with overrideable defaults would let power users
tune without rebuilds.

### 5.11 Mixed dep-direction patterns
Some modules use `compileOnly` cross-module deps so that runtime
discovery does the wiring; others use `api` / `implementation`.
The convention isn't documented; the pattern flips around Phase
boundaries. Worth a written convention.

### 5.12 SpotBugs baseline is technical debt with no decay schedule
`config/spotbugs/exclude.xml` had 95 entries at Phase 9; we burned
one in Phase 12 (`XORCipher`) and added zero new entries since.
A target like "drop 5 entries per release" would force the file to
shrink. 62 of the entries are real bugs in our own code waiting
for someone to fix them.

---

## 6. Refactor proposals *(REVIEW-ONLY — do NOT execute without approval)*

### 6.1 Extract `ProcessRunnerHelper`
Shared by `MountExecutor` and `TailscaleClient`: identical
"start, close stdin, drain stdout/stderr concurrently, wait with
timeout, return (exit, stdout, stderr)" pattern. A single helper
also fixes the stderr-deadlock issue (1.25) in one place.

### 6.2 Extract `S3ErrorHandler`
Move `S3File.toIOException(AwsServiceException, FileURL)` to its
own utility; map 401/403 → `AuthException`, 404 → file-not-found,
5xx → retryable `IOException` with a marker interface. Solves
both 2.6 (UX) and 1.18 (false-negative).

### 6.3 Centralised `LastValues` for connect-dialog panels
Mount, Tailscale, S3, SFTP, NFS panels each define a
`private static final class LastValues`. Pull it into a generic
`ServerPanelMemory<T>` keyed on panel-class.

### 6.4 Centralised archive-entry validator
A `SafePath.validate(String entryName)` in `barebones-archiver`
that's called by every format module's iterator before creating
an `ArchiveEntry`. Solves 1.1 (zip-slip / tar-slip) in one place.

### 6.5 Centralised `IgnoredErrors.ignored(Throwable, String why)`
For the 30+ legitimately-empty catches that just want to swallow
a specific failure (e.g. spinner-commit-edit when no edit is in
progress). Logs at `TRACE` so the swallow is greppable. Then
the *unintentional* empty catches stand out for fixing.

### 6.6 Decompression-bomb defence in `barebones-archiver`
A `BoundedExtraction` wrapper around the existing iterators that
caps total expanded bytes / entry count / per-entry size, with
per-archive-format-or-source defaults.

### 6.7 SwingWorker shim for S3 uploads
Same pattern as `MountTask` from Phase 10c. Fixes 1.12 and 2.1
together.

### 6.8 Mount-helper retry policy
`MountExecutor.withRetry(spec, attempts, backoff)` — solves 4.2.

### 6.9 Tunables class
Single source of truth for the 30+ scattered timeout/poll
constants noted in 5.10. Optional override via system property.

### 6.10 SpotBugs baseline drawdown phase
A dedicated short PR that picks one bug pattern (e.g. all 8
remaining `HE_EQUALS_USE_HASHCODE` entries) and fixes them
properly + removes the corresponding lines from `exclude.xml`.
Repeat until the file is empty.

---

## 7. SpotBugs baseline summary (carried over from Phase 9)

`config/spotbugs/exclude.xml` currently suppresses **~94** findings
across our own code + vendored upstream:

| Bucket | Count | Notes |
|--------|------:|-------|
| `DM_DEFAULT_ENCODING` | 41 | Charset-default reliance — most are in widget / dialog / archive code paths. |
| `ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD` | 15 | Form-state caches; the Phase-10b/-11 panels avoid this via the `LastValues` holder pattern. |
| `HE_EQUALS_USE_HASHCODE` | 8 | Real bugs (see 1.9). |
| `DMI_RANDOM_USED_ONLY_ONCE` | 5 | Each `new Random()` for a single nextInt; trivial to fix. |
| `MS_SHOULD_BE_FINAL` | 4 | Mutable static fields. |
| `DE_MIGHT_IGNORE` | 3 | Catch-and-ignore exception types. |
| `NM_SAME_SIMPLE_NAME_AS_SUPERCLASS` | 3 | Name shadowing across packages. |
| `MS_MUTABLE_ARRAY` | 2 | Static `byte[]` exposed. |
| `ES_COMPARING_PARAMETER_STRING_WITH_EQ` | 2 | `==` instead of `.equals()`. |
| `CN_IMPLEMENTS_CLONE_BUT_NOT_CLONEABLE` | 2 | Broken `clone()`. |
| various others | 8 | One each. |
| Vendored (`com.sun.*`, `sun.net.www.*`) | 33 | Wholesale package suppressions; not ours to fix. |

The baseline file's stated lifecycle is "delete a line, fix the
underlying bug, repeat." See refactor proposal 6.10.
