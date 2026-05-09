# PLAN — `barebones-commander`

A **barebones**, security-first fork of muCommander focused on a small mouse-driven dual-pane file manager with **SFTP/SSH** as the only remote protocol, on **Linux + macOS**.

Source: forked from https://github.com/mucommander/mucommander to https://github.com/e6qu/barebones-commander on 2026-05-08. Renamed in-place via PR #2.

> Companion docs in this repo:
> - `LIBRARIES.md` — current architecture & full library inventory.
> - `SECURITY_REVIEW.md` — full Critical/High vulnerability audit.

---

## Phase summary (at-a-glance)

| Phase | Status | What | One PR |
|---|---|---|---|
| **0** | done | Bootstrap: audit docs, rename project, CI cleanup, plan adjustments | landed in #2 + #3 |
| **6** | done early | Rename to `barebones-commander` | landed in #2 |
| **1** | done | Strip every out-of-scope module (protocols, archive formats, viewers, terminal, OS adapters) | landed in #5 |
| **2** | done | Drop OSGi runtime — replace Felix + bnd manifests + bundle activators with a plain Java app + fat JAR | landed in #6 |
| **3** | done | Java 25 LTS upgrade | landed in #7 |
| **4** | done | Dependency upgrades + Dependabot + dependency-review CI; **drop the abandoned `jets3t`-based S3 module** (its AWS-SDK-v2 reintroduction moved to Phase 11) | landed in #8 |
| **5** | done | Code-level security fixes (XXE-harden SAX, refactor `KdeConfig.exec`, CI grep gate against `setDefaultSSLSocketFactory`). XOR-cipher → keychain split into Phase 12. | landed in #9 |
| **7** | done | Build polish (Kotlin DSL + version catalog) | landed in #10 |
| **8** | done | Release pipeline (DMG/DEB/RPM via `jpackage`) + SBOM + provenance attestation. AppImage / x86_64 macOS / notarization / commit-signing deferred. | landed in #11 |
| **9** | done | SAST in CI (SpotBugs + FindSecBugs PR-triggered + OWASP Dependency-Check weekly) | landed in #12 |
| **10a** | done | Connectivity backends: `barebones-mount-helper` + `barebones-tailscale` modules | landed in #13 |
| **10b** | done | Connectivity UI tabs in the existing Connect-to-server dialog: `MountPanel` + `TailscalePeerPanel` | landed in #14 |
| **10c** | done | Connectivity polish: SwingWorker mount, active-mounts dialog, Taildrop send + AppleScript chunk-boundary fix | landed in #15 |
| **11a** | done | S3 backend on AWS SDK v2 — headless | landed in #16 |
| **11**  | done | Finish S3: `S3Panel`, `S3TransferManager` multipart upload, LocalStack integration tests, AppleScript race-fix follow-up | landed in #17 |
| **12** | done | `XORCipher` → OS keychain (macOS Keychain via JNA, Linux libsecret via JNA, AES-GCM file fallback) + one-shot migration of legacy `credentials.xml` | landed in #18 |
| **13** | done | Archive safety hardening — `SafePath` validator + `BoundedExtraction` caps + viewer file-size prompt + archive-tree thread safety + `ZipInputStream` / `LocalFile` stream-leak fixes | landed in #19 |
| **14** | done | Credentials & SecretStore hardening — SFTP host-key verification, JNA pointer hygiene (Keychain item-ref + libsecret schema unref + AES-GCM key zeroing), 4 `equals`/`hashCode` contracts, S3 cache-key SHA-256, `CredentialsMapping.toString` masking, `SecretStore` AutoCloseable + Bootstrap shutdown hook | landed in #20 |
| **15** | done | Dead-code sweep — 21 whole files deleted, 4 dead top-level dirs gone, ~1.1k stale i18n keys across 28 dictionaries, logback config moved to classpath + sanitised. **Net −6,012 LOC.** | landed in #21 |
| **16a** | done | **Network reliability — process & timeout core** — `ExternalCommand` extraction (fixes stderr-pipe deadlock for mount + tailscale), SFTP connect / read / serverAlive timeouts, polling-loop → `Timer` for `PropertiesDialog` + `QuickSearch`, shutdown hook drains `MountRegistry` + closes S3 `S3Connection` cache | this PR |
| **16b** | pending | **Network reliability — remainders** — NFS / Sun-RPC `setSoTimeout`, libsecret D-Bus `GCancellable` timeouts, mount retry/backoff, `SwingWorker` shim for S3 uploads (paired with Phase 19 progress UI), `WeakHashMap` listener fix, remaining polling loops (`FolderChangeMonitor` daemon tick, `CompletionType`) | one PR |
| **17** | pending | **Concurrency + correctness sweep** — mutable static collections (`Vector`/`Hashtable` in `BookmarkManager` / `ActionProperties` / `CredentialsManager`), 31+ empty catches → `IgnoredErrors` helper, NPE / stream-leak patterns, mount username injection | one PR |
| **18** | pending | **Observability + logging** — S3 module logging from zero, mount stderr on failure, tailscale timeout context, `ThemeManager` file paths, AppleScript REPLACE branch, SFTP warn-level on failures, AppleScript output bound + truncation marker, structured-logging conventions doc | one PR |
| **19** | pending | **UX polish** — progress dialogs for S3 / folder browse, "operation failed" details, mount-error next-step hints, S3 401/403/404 distinction, tailscale-not-installed banner, prefs Cancel-reverts, default-button focus, huge-file open prompts, keychain-prompt explainer, drop-target writability | one PR (may split into UX-A / UX-B) |
| **20** | pending | **SpotBugs baseline drawdown to zero** — fix the remaining ~62 own-code suppressions in `config/spotbugs/exclude.xml` (DM_DEFAULT_ENCODING ×41, ST_WRITE_TO_STATIC ×15, HE_EQUALS_USE_HASHCODE ×8, etc) and delete the file. | one PR (may split per bug pattern) |
| **21+** | open | **Architecture refactors — REVIEW REQUIRED.** Tracked separately; do NOT execute without explicit approval per `BUGS.md` §5/§6. | n/a |

**Hard rule**: only one branch / one PR is in flight at a time. The user — not the LLM — decides when a PR is ready and when the next one starts. The LLM does not autonomously open new PRs to fan out work in parallel.

---

## 1. Goals

1. Ship a **small** dual-pane file manager built on the well-tested muCommander UI core.
2. **Remote-data backends**: SSH/SFTP, in-process NFSv2/v3 (via the existing Yanfs-based module), and — via Phase 10's mount helper — anything the OS can mount (NFSv4, SMB/CIFS, SSHFS). Local FS is always available. **S3-compatible object storage** is on the roadmap via Phase 11 but is not present in the current build (the legacy jets3t-based module was deleted in Phase 4).
3. **Two** OS targets: Linux (x86_64, aarch64) and macOS (Apple Silicon + Intel).
4. **No** unpatched Critical/High vulnerabilities at v1.0 release.
5. **Latest LTS Java** (Java 25 LTS) as the runtime target.
6. **Mouse-driven UX** with drag & drop and full keyboard bindings preserved.
7. Modern, **non-OSGi** packaging — single fat JAR / native installers, no Felix container.
8. Clean **rename and rebrand** to remove muCommander trademark concerns. *(Done in #2.)*
9. **PR-only** workflow on `e6qu/barebones-commander` — every change lands via a reviewed PR. **One PR in flight at a time.** The user decides scope and pacing of the next PR.
10. **Preserve the VFS extensibility** — the upstream `barebones-commons-file` abstraction (`AbstractFile`) and the `barebones-protocol-api` SPI stay, so future backends (rsync, WebDAV, etc.) can be added without core changes.
11. **Be Tailscale-aware** — discover tailnet peers, surface them as quick-connect targets for SFTP / NFS / mount-helper, and (optional) integrate Taildrop send/receive. See Phase 10.
12. **Mount-as-local UX on Linux & macOS** — pick a remote share (NFSv4, SMB, SSHFS), the app shells out to the OS mount command, and the share opens in a panel as if it were local. See Phase 10.

## 2. Non-goals (explicitly removed scope)

| Removed | Why |
|---|---|
| Windows / OpenVMS / macOS-Java-8 OS adapters | Out of stated scope. |
| FTP, HTTP, HTTPS browsing | Out of scope; HTTP bundle also carries the JVM-wide TLS bypass (SECURITY_REVIEW §5.1). |
| SMB (`jcifs-ng` + `smbj`) | Out of scope. |
| Dropbox / Google Drive / OneDrive / Google Cloud Storage / Azure | Out of scope. Drops `azure-identity`, `microsoft-graph`, `dropbox-core-sdk`, `google-api-client`, `google-oauth-client-jetty`. (S3-compatible providers are covered by the kept S3 backend in §5.1.) |
| Hadoop / HDFS, oVirt, vSphere, ADB, Windows Registry | Out of scope. Drops `hadoop-client`, `avro`, `vim25.jar`, `jadb-v1.2.1.jar`. (NFS is kept — see §5.1.) |
| RAR / 7z / ISO / RPM / cpio / ar / lst archive formats | Out of scope. Drops `junrar` (CVE-2026-28208, CVE-2026-41245) + `sevenzipjbinding` (license-grey via UnRAR). |
| `libguestfs` format (WIP upstream) | Out of scope. |
| Image viewer / PDF viewer / binary (hex) viewer | Out of scope. Drops `icepdf-viewer` and the entire TwelveMonkeys imageio set. |
| Embedded terminal widget | Out of scope. Drops `jetbrains-jediterm`, `pty4j`, `purejavacomm`. Users can use a real terminal app for SSH command sessions. |
| Bonjour / mDNS discovery | Out of scope. Drops `jmdns`. |
| OSGi runtime (Apache Felix) | Replaced by a plain JVM application + `jpackage`. |
| External contributions (for now) | We are not yet ready to receive code, translation, or — until further notice — security reports from outsiders. No `CONTRIBUTING.md` is published; `SECURITY.md` is published only because the disclosure channel is cheap to set up. The fork is heavily refactoring; outside contributions before v1.0 would create churn we cannot absorb. |

## 3. Target stack

| Layer | Choice |
|---|---|
| Runtime | **Java 25 LTS** (latest LTS as of 2026-05-09) |
| Build | Gradle 8.x with **Kotlin DSL** (Phase 7; optional but pays dividends) |
| Module system | Plain JAR + classpath (or JPMS if cheap). **No OSGi.** |
| UI | Swing + FlatLaf 3.x (post-3.0 line) |
| L&F on macOS | FlatLaf macOS variants (drop VAqua — GPLv3, but stagnant) |
| Logging | SLF4J + Logback 1.5.x |
| SFTP | `com.github.mwiede:jsch` (latest, ≥ 0.2.21 — fixes Terrapin CVE-2023-48795) |
| Native interop | JNA 5.14+ (single pinned version; macOS quarantine, trash-to-bin, etc.) |
| YAML config | SnakeYAML 2.4+ |
| Packaging | `jpackage` for DMG (macOS) and DEB / RPM / AppImage (Linux) |
| CI | GitHub Actions (`ubuntu-latest`, `macos-15` matrix only) |
| Tests | JUnit 5 (migrate off TestNG over time) |

## 4. Licensing & trademark posture

### 4.1 Project license

- Upstream is **GPLv3**. We **stay on GPLv3** — there is no relicensing path without re-collecting CLAs from every contributor, and GPLv3 is fine for our purposes.
- We add **`NOTICE`** (already in repo) aggregating third-party licenses and crediting upstream.
- We add **`SECURITY.md`** (this PR) with a vulnerability-disclosure path.
- We **do not** add `CONTRIBUTING.md` until the fork is feature-complete and ready to accept outside code (the user decides when). Until then, outside PRs will be closed.
- Every preserved upstream file keeps its existing GPL header. Files we author also use the GPLv3 header.

### 4.2 Compatibility audit of the libraries we keep

For the pruned dependency set (SFTP-only barebones build):

| Library | License | GPLv3-compatible? |
|---|---|---|
| `slf4j-api` | MIT | ✅ |
| `logback-classic`, `logback-core` | EPL 1.0 OR LGPL 2.1 | ✅ via LGPL leg |
| FlatLaf | Apache 2.0 | ✅ |
| ICU4J | Unicode-DFS-2016 (MIT-like) | ✅ |
| JNA | Apache 2.0 / LGPL 2.1 dual | ✅ |
| Gson (if kept for config) | Apache 2.0 | ✅ |
| SnakeYAML | Apache 2.0 | ✅ |
| Bouncy Castle | MIT-style | ✅ |
| `mwiede:jsch` | BSD-3 | ✅ |
| `software.amazon.awssdk:s3` (Phase 4) | Apache 2.0 | ✅ |
| `commons-compress` | Apache 2.0 | ✅ |
| XZ for Java | Public Domain | ✅ |
| Apache bzip2 (vendored from Ant) | Apache 2.0 | ✅ |
| `mbassador` | MIT | ✅ |
| `jcommander` | Apache 2.0 | ✅ |
| `log4j-core`, `log4j-1.2-api` | Apache 2.0 | ✅ |
| TestNG / JUnit 5 (test-only) | Apache 2.0 / EPL 2.0 | ✅ |

**Removing** the cloud / SMB / RAR / PDF / image deps **also removes the awkward licenses** — e.g. `junrar`'s effective UnRAR-license (no-modification clause for unrar code), `sevenzipjbinding`'s embedded UnRAR DLL bundle, and the abandoned `jets3t` chain.

`jsr305` (FindBugs annotations, `compileOnly`) has a non-standard license the FSF flags. Action: replace with `org.jetbrains:annotations` (Apache 2.0) — already pulled in transitively.

### 4.3 Trademark

- "muCommander" is the upstream's brand. There is no clearly registered USPTO trademark, but **common-law trademark** rights exist from continuous use since 2002. The mucommander.com domain, icons, and logo are upstream's.
- Forking a GPLv3 codebase is fine — keeping the **brand** while substantially diverging is **not** fine.
- The fork is renamed `barebones-commander` (PR #2). Repo URL: `https://github.com/e6qu/barebones-commander`.

### 4.4 What rename touched (PR #2, done)

| Surface | Status |
|---|---|
| Display name / app name | `muCommander` → `barebones-commander` ✅ |
| Java package root | `com.mucommander.*` → `dev.barebones.commander.*` ✅ |
| Gradle root group | `org.mucommander` → `dev.barebones.commander` ✅ |
| Gradle root version | reset to `0.1.0-SNAPSHOT` ✅ |
| JAR / executable name | `barebones-commander.jar`, `barebones-commander.exe` ✅ |
| App bundle id (macOS) | `dev.barebones.commander.app` ✅ |
| Linux desktop entry | `barebones-commander` ✅ |
| URLs | upstream URLs in metadata → `https://github.com/e6qu/barebones-commander` ✅ |
| Code-format spec | `barebones-commander-code-format.xml` ✅ |
| `i18n` keys with literal "muCommander" | rebranded across 27 dictionaries ✅ |
| `LICENSE` / source-file copyright headers | preserved verbatim ✅ |
| `README` | rewritten ✅ |
| `NOTICE` | added ✅ |
| Icons | **deferred** — upstream icon assets still in tree as placeholders. Replace as part of Phase 8 release polish (or a dedicated brand PR when the user OKs it). |

## 5. Module triage — keep / drop

### 5.1 KEEP (with small touches)

| Module | Notes |
|---|---|
| `barebones-core` | Main Swing UI. Touch points: remove menu items / actions referring to dropped protocols & viewers (Phase 1 cleanup pass). |
| `barebones-core-preload` | Bootstrap — keep. |
| `barebones-commons-file` | File abstraction. Strip protocol-specific subpackages; keep `local`. |
| `barebones-commons-io` | Stream / I/O utils. Keep. |
| `barebones-commons-collections` | Keep. |
| `barebones-commons-conf` | XML config. **Apply XXE hardening (SECURITY_REVIEW §5.5).** |
| `barebones-commons-runtime` | Keep. |
| `barebones-commons-util` | Keep. |
| `barebones-preferences` | Keep. |
| `barebones-translator` | Keep. Optional: prune languages we won't maintain — though shipping them is cheap. |
| `barebones-encoding` | Keep. |
| `barebones-process` | Keep. |
| `barebones-command` | Custom-command feature. **Apply XXE hardening.** |
| `barebones-protocol-api` | SPI. Keep — this is the VFS plug-in contract; future backends (rsync, WebDAV) can hook in here. |
| `barebones-protocol-sftp` | SFTP backend. Bump `jsch` to fix Terrapin (Phase 4). |
| `barebones-protocol-s3` | **Deleted in Phase 4.** Will be reintroduced in Phase 11 on top of AWS SDK v2 (`software.amazon.awssdk:s3`). |
| `barebones-protocol-nfs` | In-process NFSv2/v3 backend (Yanfs-based via the vendored `sun-net-www`). NFSv4 is delivered via Phase 10's OS mount helper rather than this module — Yanfs has no v4 support and a Java NFSv4 client is not worth carrying. |
| `sun-net-www` (vendored) | Keep — required by `barebones-protocol-nfs` (Yanfs / NFS RPC support). |
| `barebones-os-api` | Keep. |
| `barebones-os-linux` | Keep. **Refactor `KdeConfig` to `ProcessBuilder(List)` in Phase 5.** |
| `barebones-os-macos` | Keep. |
| `barebones-archiver` | Keep — needed for "compress to zip/tar" actions. |
| `barebones-format-zip` | Keep. |
| `barebones-format-tar` | Keep. |
| `barebones-format-gzip` | Keep. |
| `barebones-format-bzip2` | Keep. |
| `barebones-format-xz` | Keep. |
| `apache-bzip2` (vendored) | Keep — needed by bzip2 module. |
| `barebones-viewer-api` | Keep. |
| `barebones-viewer-text` | Keep — minimal text viewer. |

### 5.2 REMOVE in Phase 1

- **Protocols**: `adb`, `bonjour`, `dropbox`, `ftp`, `gcs`, `gdrive`, `hadoop`, `http`, `onedrive`, `ovirt`, `registry`, `smb`, `vsphere`. (13 modules. **`s3` and `nfs` are kept** — the S3 module's `jets3t` internals are rewritten on top of AWS SDK v2 in Phase 4; the NFS module keeps the Yanfs-based implementation via `sun-net-www`.)
- **Archive formats**: `ar`, `cpio`, `iso`, `libguestfs`, `lst`, `rar`, `rpm`, `sevenzip`. Removes `junrar` (CVEs), `commons-vfs2` (CVE-2025-27553), `sevenzipjbinding` (license-grey). (8 modules.)
- **Viewers**: `binary` (hex), `image`, `pdf`. Drops `icepdf-viewer` and the entire TwelveMonkeys imageio set. (3 modules.)
- **OS adapters**: `win`, `openvms`, `macos-java8`. (3 modules.)
- **Vendored helpers**: `jetbrains-jediterm`, `sevenzipjbindings`, `gson` (re-bundled), `kotlin-reflect`. (4 modules. `sun-net-www` is kept because NFS needs it.)
- **Embedded terminal**: the `barebones-core/.../ui/terminal/*` package + its `pty4j` / `purejavacomm` dependency lines.

### 5.3 Effective module count

- **Before Phase 1**: 56 sub-projects (post-rename).
- **After Phase 1**: ~25 sub-projects (S3, NFS, and `sun-net-www` retained on top of the original keep list).
- Source LOC drop estimate: ≥ 30 %.

## 6. Phased delivery — one PR per phase

Each phase = **one** branch + **one** PR against `main`. Squash-merge. Tests must remain green at the merge point. The user signals when a PR is "good" and when the next branch starts. The LLM does not open the next PR autonomously.

### Phase 0 — Bootstrap *(ongoing — last PR closes it)*

Already landed in PR #2 (`main`):
- `LIBRARIES.md`, `SECURITY_REVIEW.md`, `PLAN.md`.
- Project rename (Phase 6 work, done early).
- CI cleanup: removed `.travis.yml`, `nightly.yml`, `stable.yml`; kept only `tests.yaml`.

This PR (#3) finishes Phase 0:
- Adds `SECURITY.md` (vulnerability disclosure path only — no contribution scaffold yet).
- Drops the originally-planned `CONTRIBUTING.md` (deferred until v1.0 per user direction).
- Fixes the missed `libs/mucommander-gradle-macappbundle.jar` → `libs/barebones-gradle-macappbundle.jar` rename so CI compiles again.
- Rewrites this `PLAN.md` to lock in the single-PR-at-a-time rule and collapse multi-PR phases to one PR each.

**Exit criteria**: green CI on master, planning docs final.

### Phase 1 — Strip everything out-of-scope (one PR)

Single sweeping PR doing the full §5.2 deletion list:

- Delete the 15 out-of-scope **protocol** modules.
- Delete the 8 out-of-scope **archive-format** modules.
- Delete the 3 **viewer** modules (binary, image, pdf).
- Delete the 3 **OS adapter** modules (win, openvms, macos-java8).
- Delete the 5 **vendored helper** modules (jediterm, sevenzipjbindings, gson, kotlin-reflect, sun-net-www).
- Delete the embedded **terminal** package inside `barebones-core` and its dependencies.
- Update `settings.gradle` (remove `include` lines).
- Update root `build.gradle` (remove `osgiRuntime project(...)` lines, drop `vaqua`, drop launch4j Windows EXE / `msi` / `winAppImage` tasks, drop `mucommanderBundleJRE` Windows-only branches).
- Cleanup pass: orphaned UI menu actions / `dictionary_*.properties` keys / action keymap entries / image resources for removed features.
- **Verify** no cross-module imports reach into deleted packages (CI greps post-merge will catch anything missed).

**Side-effects**:
- Kills the JVM-wide TLS bypass (SECURITY_REVIEW §5.1) by deleting the HTTP bundle.
- Removes junrar CVEs (§4.1.1, §4.1.2) by deleting the RAR module.
- Removes `commons-vfs2` CVE-2025-27553 by deleting the RAR module's transitive dep.
- Removes `hadoop-client` CVE-2025-27821 by deleting the Hadoop module.
- The S3 module **stays** but still uses `jets3t` (and pulls `mail.osgi-1.4.jar` as a transitive dep) until Phase 4 modernizes it to AWS SDK v2. This is acceptable for Phase 1's exit because no Critical/High CVEs are filed against `jets3t 0.9.7` directly; the concern is staleness, addressed in Phase 4.

**Exit criteria**: app builds on Linux + macOS with local + SFTP + S3 + NFS file panels; `./gradlew test` green.

### Phase 2 — Drop OSGi runtime (one PR)

OSGi via Apache Felix is upstream's modularity choice; for a one-protocol app it is pure overhead.

- Remove `biz.aQute.bnd.builder` plugin and per-subproject `bnd { ... }` blocks.
- Replace each `Activator` class with explicit registration calls in a new `dev.barebones.commander.bootstrap.Bootstrap` invoked from `main`.
- Replace `runOsgi` / Felix runtime with the Gradle `application` plugin + a single fat JAR.
- Drop the `osgi/`, `bundle/`, `app/`, `conf/` runtime layout and adjust `jpackage` invocations.
- Drop Apache Felix from dependencies.

**Exit criteria**: `./gradlew run` launches the app without Felix; produced JAR runs via `java -jar barebones-commander.jar`.

### Phase 3 — Java 25 LTS upgrade (one PR)

- `compileJava.options.compilerArgs += ['--release', '25']` everywhere.
- Set `JavaVersion.VERSION_25` via Gradle's `java.toolchain` block.
- Update `tests.yaml` matrix to `java-version: '25'` (Temurin / Adoptium).
- Fix `--add-opens` / `--add-exports` lists for current JDK module names; prune any that became unnecessary.
- Replace deprecated APIs (`SecurityManager`, finalize-related, `Thread.stop`, etc.).
- Apply `var` / switch expressions / pattern matching where they cleanly improve readability.

**Exit criteria**: app builds & passes tests on Java 25.

### Phase 4 — Dependency upgrades (one PR)

**Drop the jets3t-based S3 module wholesale.** The original Phase-4 plan
called for an in-place rewrite from `jets3t` to AWS SDK v2; that is a
~1.7k-LOC refactor with a very different API surface, no live test
endpoint in CI, and so an outsized risk inside a phase that is otherwise
about safe dep bumps. Phase 4 therefore deletes the
`barebones-protocol-s3` subproject entirely (along with its bundled
`mail.osgi-1.4.jar`); reintroducing S3 on AWS SDK v2 is split into a
dedicated **Phase 11**.

After this commit there is no `org.jets3t` dependency anywhere in the
build, and no `mail.osgi-1.4.jar` artifact in `libs/`.

**Bumps**:
- `mwiede:jsch` 0.2.10 → ≥ 0.2.21 (fixes Terrapin CVE-2023-48795).
- Logback 1.2.13 → 1.5.x.
- SLF4J 1.7.36 → 2.0.x.
- SnakeYAML 2.3 → 2.4.
- JNA — pin a **single** version (≥ 5.14) project-wide (fixes the 5.5.0 / 5.12.1 split).
- FlatLaf 2.6 / 2.2 → 3.x — pin a **single** version (fixes the 2.6 / 2.2 split).
- ICU4J 78.3 → latest.
- Gson 2.11.0 → latest.
- Bouncy Castle 1.79 → latest 1.x.
- `commons-compress` 1.28.0 → latest.
- `log4j-core` 2.25.3 → latest.
- `jcommander` 1.82 → latest.

**Tooling**:
- Add **Dependabot config** (`.github/dependabot.yml`) for `gradle` ecosystem, weekly cadence.
- Add `dependency-review-action` step to `tests.yaml`.

**Exit criteria**: no Critical / High dependency CVEs in `SECURITY_REVIEW.md` §4.1 still apply; `jets3t` and `mail.osgi-1.4.jar` are gone from the build.

### Phase 5 — Code-level security fixes (one PR)

Maps 1:1 to `SECURITY_REVIEW.md` §5 — except item 2 (XOR cipher
replacement) which is split into a dedicated **Phase 12** because
keychain integration requires JNA bindings to macOS Security.framework
and Linux libsecret plus a passphrase-derived AES-GCM fallback plus
on-first-run migration logic — too much for a single security-fix PR
that's otherwise mechanical.

1. Add a CI **grep gate** that fails the build if `setDefaultSSLSocketFactory` or `setDefaultHostnameVerifier` reappear in the tree (the actual call sites died in Phase 1 with the HTTP bundle).
2. *(deferred to Phase 12)* Replace `XORCipher`-based credential storage with OS keychain integration.
3. **XXE-harden** the 9 SAX entry points (theme, bookmarks, action keymap, toolbar, command bar, association, command, credentials, configuration): set `FEATURE_SECURE_PROCESSING=true` and `disallow-doctype-decl=true`. Add a small test asserting `<!DOCTYPE>` → throws.
4. Refactor `KdeConfig.exec(String + key)` to `ProcessBuilder(List.of(...))`.

**Exit criteria**: items 1, 3, 4 done. Item 2 lives in Phase 12.

### Phase 6 — Rename to `barebones-commander`

✅ **Done in PR #2.** Class-rename (`muCommander.java` → e.g. `BareCommander.java`) and icon replacement deferred — they can be folded into Phase 8 release polish or a small dedicated PR if the user requests.

### Phase 7 — Build polish (one PR)

- Migrate `build.gradle` (root + subprojects) to **Kotlin DSL** `build.gradle.kts`.
- Introduce **version catalog** (`gradle/libs.versions.toml`) — ends per-module pinning drift permanently.

### Phase 8 — Release pipeline + supply chain (one PR)

**Automated in this phase**:
- New `release.yml` triggered on `v*` tag push (and `workflow_dispatch` for testing):
  - Builds fat-jar + SBOM on `ubuntu-latest`.
  - Native installers via `jpackage`: **DEB + RPM on Linux** (matrix), **DMG on macOS aarch64** (`macos-15` runner).
  - SLSA-style provenance via `actions/attest-build-provenance@v1` covering every artifact.
  - Draft GitHub Release auto-created with all artifacts attached.
- **CycloneDX SBOM** via `org.cyclonedx.bom` Gradle plugin → `bom.json` + `bom.xml` published per release.

**Deferred to follow-up phases** (each needs out-of-band setup the LLM cannot do alone):
- **AppImage** for Linux: `jpackage` does not produce AppImage natively. Needs `appimagetool` integration + a Linux app-image directory build step.
- **macOS x86_64 DMG**: requires a `macos-13` runner in the matrix; left out for the initial cut to keep CI minutes low. Single-line addition when Intel Mac coverage becomes priority.
- **macOS notarization**: requires an Apple Developer ID + `APPLE_ID` / `APPLE_TEAM_ID` / `APPLE_APP_PASSWORD` repo secrets. Until configured, the DMG is unsigned (Gatekeeper will warn on first launch).
- **Commit signing + branch protection**: must be configured in the GitHub repo settings UI by the repo owner. The PR cannot toggle these from CI. Recommended:
  * Settings → Rules → New ruleset → require signed commits + linear history on `main`.
  * Local: `git config commit.gpgsign true` (or sigstore via `gitsign`).
- **Icon artwork**: needs a new icon set (PNG/ICNS/ICO) before passing to `--icon` in `jpackage`. Until then, jpackage uses the default Java cup icon.

### Phase 9 — SAST in CI (one PR)

- Add **SpotBugs + FindSecBugs** as a Gradle-driven CI step
  (`.github/workflows/spotbugs.yaml`, PR + push-to-main triggered).
  Fails the build on any HIGH-confidence finding not listed in
  `config/spotbugs/exclude.xml`. SARIF uploaded to GitHub Code
  Scanning.
- Add **OWASP Dependency-Check** as a scheduled weekly CI run
  (`.github/workflows/dependency-check.yaml`, Monday 06:00 UTC +
  workflow_dispatch). Fails on CVSS ≥ 7.0 not suppressed in
  `config/dependency-check/suppression.xml`. SARIF uploaded to
  GitHub Code Scanning.
- The Phase-9 SpotBugs baseline (`config/spotbugs/exclude.xml`)
  captures **95 pre-existing HIGH-confidence findings** in the
  brownfield muCommander code. Categorisation:
  * `com.sun.*` / `sun.net.www.*` — vendored upstream (33 findings)
    suppressed wholesale via `<Package>` matches.
  * Per-(class, bug-pattern) suppressions for our own code (62
    findings), each line a real issue to fix in a follow-up.
  * Top patterns: `DM_DEFAULT_ENCODING` (charset reliance),
    `ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD` (static caches),
    `HE_EQUALS_USE_HASHCODE` (broken `equals/hashCode` contract).
- Post-Phase-9 cleanup phases will progressively remove suppressions
  from `config/spotbugs/exclude.xml` until empty (then the file can
  be deleted and SpotBugs runs purely on regression).

### Phase 12 — Keychain-backed credentials (one PR)

Lifted out of Phase 5 because keychain integration involves JNA
bindings to platform-specific APIs and a non-trivial passphrase-derived
AES-GCM fallback path. New module `barebones-secret-store` houses
all of this.

**Shipped**:

- `dev.barebones.commander.bookmark.XORCipher` **deleted**. Its
  decode-only logic lives in
  `dev.barebones.commander.secret.LegacyXorCodec`, used solely by
  the credentials parser for one-shot migration of pre-Phase-12
  `credentials.xml` files. Test fixtures (3 known-passwords) prove
  byte-compatibility with upstream's deleted XORCipher.
- `dev.barebones.commander.secret.SecretStore` SPI with three
  implementations:
  * **macOS Keychain** via JNA → `Security.framework` legacy
    generic-password API (`SecKeychainAddGenericPassword` /
    `SecKeychainFindGenericPassword` / `SecKeychainItemDelete`).
  * **Linux libsecret** via JNA → `secret_password_store_sync` /
    `secret_password_lookup_sync` / `secret_password_clear_sync`
    against a custom schema (`dev.barebones.commander.Credentials`).
  * **AES-GCM file** at `~/.barebones-commander/credentials.bin`
    for headless / no-keychain environments. Key derivation via
    PBKDF2-HMAC-SHA-256 (310k iterations — current OWASP guidance);
    AES-256-GCM with per-save random IV; magic+version bound into
    the AAD; atomic write via tmp+rename; mode 0600.
- Activator picks the OS-appropriate backend automatically. Override
  via `-Dbarebones.secretStore=macos-keychain|linux-libsecret|aes-gcm-file|none`.
  AES-GCM file backend additionally needs
  `-Dbarebones.secretStore.passphrase=<value>` (no auto-prompt — that
  would be a hostile UX for a desktop file manager).
- `CredentialsWriter` updated: emits `<secret-ref/>` (an empty
  marker element) instead of `<password>XOR_BASE64</password>`. The
  actual secret is pushed into the SecretStore before the XML is
  written, keyed by `(service="barebones-commander", account=URL)`.
- `CredentialsParser` updated: accepts both formats. Legacy
  `<password>` entries are decoded via `LegacyXorCodec` and migrated
  into the SecretStore on the fly. New `<secret-ref/>` entries look
  the password up via `SecretStoreService.store().lookup(...)`.
- Bootstrap registers the secret-store Activator early — before the
  credentials code runs.

**Tests**:

- `AesGcmFileSecretStoreTest` (10): round-trip; persistence across
  reopen; wrong-passphrase failure; overwrite semantics; delete +
  delete-of-missing; multiple entries; Unicode secrets; empty
  passphrase rejected; corrupt file rejected.
- `LegacyXorCodecTest` (4): three byte-compat fixtures (ASCII,
  symbol-heavy, UTF-8) + invalid-Base64 rejection.

**Deferred**:

- Preferences-UI panel for backend selection. Auto-detect + system
  property override is functional today; a Swing radio-button
  picker plumbed through `MuPreferences` is its own slice.
- macOS Keychain + libsecret end-to-end tests are not exercised in
  CI: macOS-15 GitHub runners have no logged-in keychain session;
  Ubuntu runners have no libsecret installed by default. The JNA
  bindings are exercised by a manual smoke test on a real desktop.

### Phase 11 — Re-add S3 backend on AWS SDK v2 (split: 11a + 11b + 11c)

Lifted out of the original Phase 4 plan because the refactor is too
large for a dep-upgrade PR. The Phase-4 PR removed the `jets3t`-based
module to clear the dep tree; this phase adds it back, properly
implemented. Split into three because the headless backend, the
Swing UI panel, and the testcontainers integration tests are
mechanically independent and reviewing them together would be
unwieldy.

**Phase 11a** — headless backend:

- New `barebones-protocol-s3` subproject.
- `software.amazon.awssdk:bom` + `software.amazon.awssdk:s3` deps
  (catalog entries `aws-sdk-bom` + `aws-sdk-s3`).
- `S3Connection` — wraps an `S3Client` configured for one
  (endpoint, region, credentials) tuple. Accepts both static
  credentials (URL-supplied access/secret key) and falls back to
  the SDK's `DefaultCredentialsProvider` (env, ~/.aws/credentials,
  IAM instance role) when none are URL-supplied.
- `S3FileURL` — pure value object that decomposes a `FileURL` into
  endpoint host/port + bucket + key; no I/O.
- `S3File` — abstract base extending `ProtocolFile`; surfaces
  `UnsupportedFileOperationException` for everything S3 has no
  analogue for (POSIX permissions, owner/group, free/total space,
  random-access I/O, append, remote copy, change-date).
- `S3Root` — lists buckets via `listBuckets()`.
- `S3Bucket` — lists objects via `ListObjectsV2` with
  delimiter='/'; `headBucket` for `exists()`; create/delete bucket.
- `S3Object` — get/put/delete object; mkdir creates an empty
  zero-byte object whose key ends in `/`; rename does
  copy-then-delete; output stream buffers in memory and PUTs on
  close (multipart streaming deferred to Phase 11c).
- `S3Listing` — shared `ListObjectsV2`-with-paging helper.
- `S3ProtocolProvider` — connection cache keyed by
  (host, port, region, accessKey, pathStyle, useHttps) — different
  credentials never share a client.
- `Activator` — registers the s3 scheme with
  `FileProtocolServiceTracker`.
- Bootstrap entry, root build runtimeOnly dep, fat-jar bundles AWS
  SDK + Netty (jar grows from ~30 MB to ~43 MB).
- Tests: `S3FileURLTest` (root vs bucket-root vs object vs
  trailing-slash directory; MinIO host:port shape);
  `S3ConnectionEndpointTest` (https default-port stripping, http
  default-port stripping, MinIO custom-port retention, blank-host
  rejection).

**Phase 11 finish** — Swing UI + integration tests + multipart streaming
(all in one PR per the user's "no more sub-phases" directive):

- `S3Panel` (the connection dialog) rebuilt for the AWS-SDK-v2
  inputs: endpoint host, bucket, access key, secret key, region,
  port, HTTPS toggle, path-style toggle. Registered via
  `ProtocolPanelRegistry` so the existing Connect-to-server dialog
  grows an "S3" tab.
- `S3TransferManager` plumbed into `S3Connection`; `S3Object`'s
  OutputStream now uses a `SpillingPutOutputStream` that buffers
  in memory up to 32 MiB and spills to a temp file beyond that;
  on close, in-memory payloads PUT in one request and spilled
  files upload via the TransferManager (multipart). Temp files
  are best-effort deleted in a `finally`.
- LocalStack-backed integration tests via `testcontainers-java`,
  gated on Docker availability (Skip on macOS-15 GitHub runner
  where the runner doesn't ship Docker; runs on ubuntu-latest).
  Coverage: full lifecycle (mkdir-bucket → ls-root → put → ls-bucket →
  get → mkdir-prefix → put-nested → ls-prefix → delete);
  paged listing of 1500 objects across multiple continuation
  tokens; rename copy-then-delete; 40 MiB upload exercising the
  TransferManager spill path; 1 KB upload staying in-memory.

### Phase 10 — Connectivity: Tailscale + mount helper (split: 10a + 10b)

The first feature-add phase after the cleanup wave. Originally
scoped as one PR; split into two because the backend services and
the Swing UI integration are mechanically independent and reviewing
them together would be unwieldy.

**Phase 10a** ships the headless backends as new modules with full
unit-test coverage and Activator registration:

- `barebones-mount-helper` — `MountSpec`, `MountKind`, `MountCommand`
  SPI with `LinuxMountCommand` + `MacOSMountCommand`, `MountExecutor`
  (ProcessBuilder shell-out with timeout + stdout/stderr capture),
  `MountRegistry` (active-mount tracking), Activator that picks the
  OS-appropriate command on startup. NFSv3/v4, SMB, SSHFS supported.
- `barebones-tailscale` — `TailscalePeer` record, `TailscaleStatusParser`
  for `tailscale status --json` output, `TailscaleClient` (locate
  binary on $PATH or macOS GUI install path; `peers()`; `sendFile()`
  for Taildrop), Activator that no-ops when tailscale isn't installed.

Tests in 10a verify argv composition for every `(OS, MountKind)` pair
plus an injection-defence regression case (shell metacharacters in
user-supplied fields stay contained in their argv slot), `MountSpec`
validation, `MountRegistry` mutual-exclusion, and `tailscale status
--json` parsing against a real fixture.

**Phase 10b** wires the backends into the existing UI as new tabs in
the Connect-to-server dialog (Cmd-K / Ctrl-K), so the user discovers
them through the existing remote-connect flow with no new menu
plumbing:

- `MountPanel` (new in `barebones-mount-helper`) — kind dropdown
  (NFSv3/NFSv4/SMB/SSHFS), host / remote-path / mountpoint /
  username / port fields. On Connect, calls
  `MountService.executor().mount(spec)` synchronously and returns
  `file:///<mountpoint>` so the active panel navigates into the
  freshly-mounted directory. Records the mount in `MountRegistry`.
- `TailscalePeerPanel` (new in `barebones-tailscale`) — lists peers
  from `TailscaleService.client().peers()` plus a protocol selector
  (SFTP / NFS / SMB). On Connect, returns
  `<scheme>://<peer.dnsName>/` so the existing protocol stacks open
  the chosen peer. Falls back to a clear "Tailscale not installed"
  status when the binary isn't present.

Both panels register via `ProtocolPanelRegistry.register(...)` from
their module Activators. No changes to `ActionType`, `ActionManager`,
or menu wiring needed.

**Phase 10c** delivers connectivity polish entirely inside the
mount-helper / tailscale modules — no `barebones-core` compile-time
dep, no new `ActionType` entries, no menu wiring:

- `MountTask` (`barebones-mount-helper`): SwingWorker that runs
  `MountExecutor.mount(spec)` off the EDT and shows a small modal
  "Mounting…" progress dialog while it works. The EDT keeps pumping
  events (the indeterminate progress bar animates, the dialog
  responds to window-close attempts).
- `ActiveMountsDialog` (`barebones-mount-helper`): modal dialog
  listing every entry in `MountRegistry.instance().active()` with
  per-row Unmount; surfaces `umount` exit code + stderr on failure.
  Opened from a "Manage active mounts…" button on the Mount tab.
- Taildrop send button on the Tailscale tab: when a peer is
  selected, opens a `JFileChooser`; on selection, calls
  `TailscaleClient.sendFile(local, peer.dnsName())`. Failure
  surfaced via `JOptionPane.showMessageDialog`.

The deliberate non-choice: no top-level menu actions / keybindings.
Adding actions would force a `compileOnly` dep on `barebones-core`
(for `MuAction` / `ActionManager`) and an `ActionType` enum entry —
real plumbing for marginal value when the buttons live exactly where
the user already is.

**Phase 10c** (deferred polish):

**OS-level mount helper** — a small Swing dialog that:
- Asks for a remote share URL / host / share-path / credentials.
- Resolves a target mountpoint under `${user.home}/.barebones-commander/mounts/<host>-<share>` (Linux) or `/Volumes/<host>-<share>` (macOS).
- Invokes the OS mount command via `ProcessBuilder(List.of(...))` (never string-concatenated):
  - **Linux**: `mount.nfs4` for NFSv4; `mount.nfs` for v2/v3; `mount -t cifs` for SMB; `sshfs` for SSHFS (FUSE).
  - **macOS**: `mount_nfs` (NFSv2/v3/v4); `mount -t smbfs` for SMB; `sshfs` for SSHFS (macFUSE if installed).
- On success, opens the mountpoint as a regular folder in the active panel.
- Tracks active mounts and offers an "Unmount" action. Best-effort cleanup on app exit.
- Privileged mounts (Linux NFS) require `sudo` or a setuid `mount.*` helper — surface this in the dialog rather than silently failing.

**NFSv4** — delivered by the mount helper above. The in-process `barebones-protocol-nfs` module is unchanged and continues to handle direct NFSv2/v3 sessions for environments where mounting is not desired.

**Tailscale integration**:
- Detect Tailscale by probing for the `tailscale` binary on `$PATH` and the local API socket (`/var/run/tailscale/tailscaled.sock` on Linux, `~/Library/Containers/io.tailscale.ipn.macsys/Data/IPN/tailscaled.sock` on macOS GUI install).
- List tailnet peers via `tailscale status --json`. Surface them in a "Tailscale peers" quick-list (similar in spirit to upstream's deleted Bonjour list).
- Selecting a peer pre-fills the SFTP / NFS / mount dialog with the peer's MagicDNS hostname (`*.ts.net`).
- (Optional) Taildrop send: a "Send to peer (Taildrop)" action shells out to `tailscale file cp <path> <peer>:`.
- (Optional) Taildrop receive: a "Tailscale inbox" panel shows files received via Taildrop (`tailscale file get`).
- All Tailscale invocations go through the OS-mount-style `ProcessBuilder(List<String>)` path — no shell-injection risk.

**Implementation discipline** (enforced in 10a; UI inherits from these
SPIs in 10b):
- All shell-outs use `ProcessBuilder(List<String>)`. No `Runtime.exec(String)`. No string concatenation of user input into command lines. (Same SAST gate from Phase 5.)
- Failure modes (binary missing, daemon not running, mount denied) bubble up as user-visible dialogs in 10b, never silent.
- No bundled Tailscale client. The user installs Tailscale via their OS; we just detect and integrate.
- No bundled `sshfs` / `mount.nfs4` / `mount.cifs`. Same posture.

**10a exit criteria**: `./gradlew test` green for both new modules,
SpotBugs clean (no new entries in the Phase-9 baseline), Activator
registration smoke-tested on Linux and macOS at app startup.

**10b exit criteria**: Connect-to-server dialog grows two tabs
("Mount" + "Tailscale") on Linux and macOS startup. SpotBugs clean
across both new panels (no entries in the Phase-9 baseline). Manual
smoke test: mount an NFSv4 share and browse the local mountpoint;
list tailnet peers and pick one to open via SFTP.

**10c exit criteria**: mount runs off the EDT via SwingWorker with
a modal progress dialog; the Mount tab gains a "Manage active
mounts…" button that opens an unmount-per-row dialog; the
Tailscale tab gains a "Send file via Taildrop…" button. SpotBugs
clean across all three additions (no entries in the Phase-9 baseline).

---

## 6.1 Phases 13–20 — bug & quality drawdown

These phases mostly drain the backlog catalogued in `BUGS.md`.
Each is a "significant chunk" — typically one PR but a reviewer
who finds the diff oversized may ask for a split. The first PR of
each phase always opens with a self-check: re-read `BUGS.md` for
any new findings since the previous merge.

### Phase 13 — Archive safety hardening (one PR)

The biggest open security gap: opening a malicious archive can
write outside the extraction directory, OOM the JVM, or freeze
the UI.

- **Zip-slip / tar-slip**: new `dev.barebones.commander.archive.SafePath`
  in `barebones-archiver`; called by every format module's iterator
  before constructing an `ArchiveEntry`. Rejects `..` segments,
  absolute paths, and Windows `\` separators. (`BUGS.md` 1.1)
- **Decompression bomb caps**: new `BoundedExtraction` wrapper —
  per-entry size limit (default 1 GiB, overridable), cumulative
  limit (default 10× the archive's compressed size), and entry
  count limit (default 100k). Surface limits via `Tunables` (see
  Phase 16). (`BUGS.md` 1.13)
- **Text viewer file-size ceiling**: prompt user before loading
  files > 100 MiB; offer head/tail-only view. (`BUGS.md` 1.14, 2.10)
- **`AbstractArchiveFile.createEntriesTree()` thread safety**:
  add the lock the existing TODO requested. (`BUGS.md` 1.15)
- **`ZipInputStream` + `LocalFile` channel leaks**: try-with-resources
  sweep across all archive iterators. (`BUGS.md` 1.10, 1.23)
- **Tests**: malicious-archive fixtures (zip-slip variants, 4 GB
  spec entry, million-entry index, mid-stream truncation) in
  `barebones-archiver`; viewer-file-size behavioural test.

**Exit criteria**: every archive entry path is `SafePath`-validated;
`BoundedExtraction` integration test trips on a 1.4 GiB-expand
fixture; viewer test passes the 100 MiB threshold.

### Phase 14 — Credentials & SecretStore hardening (one PR)

Closes the security holes left by Phases 5 + 12 plus the
Phase-12 follow-up items.

- **SFTP host-key verification**: load OpenSSH `~/.ssh/known_hosts`
  via JSch; default `StrictHostKeyChecking=yes`; first-connection
  prompt for unknown hosts; persist the user's accept choice.
  (`BUGS.md` 1.2)
- **JNA pointer hygiene**:
  - `KeychainSecretStore.lookup()` releases its `itemRef` via
    `CFRelease` on every path. (`BUGS.md` 1.6)
  - `LibsecretSecretStore` adds `close()` that calls
    `secret_schema_unref(schema)`. The Activator wires it into
    the shutdown hook (Phase 16 deliverable; if 16 is later,
    inline-register a hook here). (`BUGS.md` 1.7)
- **AES-GCM key zeroing**: `AesGcmFileSecretStore` gains
  `close()` → `Arrays.fill(keyMaterial, (byte)0)`. (`BUGS.md` 1.8)
- **Equals/hashCode contracts**: fix `Bookmark`, `CredentialsMapping`,
  `FileComparator`, `BOM` (4 of the 8 SpotBugs entries — drop those
  baseline lines). (`BUGS.md` 1.9)
- **S3 cache-key hashing**: replace the `accessKey`-in-cache-key
  with `SHA-256(accessKey+secretKey)`. (`BUGS.md` 1.11)
- **`CredentialsMapping.toString()` masking**: never include the
  password segment of the URL. (`BUGS.md` 3.4)
- **SecretStore preferences UI**: the Phase-12 deferred item.
  Adds a `SecretStorePanel` to the General preferences dialog with
  radio buttons for the available backends (auto-detect / macOS
  Keychain / libsecret / AES-GCM file / none); writes the choice
  to `MuPreferences` and bypasses `Activator`'s default-detect on
  next start. AES-GCM choice prompts for a passphrase via a
  one-shot dialog, never persisted to disk.

**Exit criteria**: SFTP integration test rejects a known-bad-host;
AES-GCM file `close()` zeroes the key (heap-walk test); the four
fixed equals/hashCode classes work in a `HashSet` round-trip;
SecretStore panel ships and survives a restart.

### Phase 15 — Dead-code removal sweep (one PR)

Per the user note: "I think there's still significant amount [of
dead code]." The post-Phase-1 module purge left orphaned references
behind.

Inputs:
- IntelliJ IDEA's "Unused declaration" inspection across the whole
  source tree.
- Manual scan for references to removed features (terminal,
  Bonjour, the deleted protocols, JediTerm, OSGi bundle activators
  that got reduced to no-ops).
- `grep` for commented-out code blocks (`// public ...`,
  `/* ... */` blocks).
- Resource-bundle entries (`*.properties` translations) that no
  surviving action references — every action key has a label
  string; removed actions left orphan keys behind.
- Unused per-module dependencies (each `build.gradle.kts` reviewed
  with `gradle dependencies --configuration runtimeClasspath` and
  by-grep verification that classes from each dep are actually
  imported).
- Dead `@Deprecated` items that nothing calls anymore.
- Stale TODOs / FIXMEs that point at problems already solved
  (e.g. "needs OSGi bundle…" comments).

Discipline:
- Verify each deletion with a build + a SpotBugs run + (where
  feasible) a manual smoke of the affected feature in the running
  app.
- Keep the diff readable: split into per-module commits within
  the PR.
- **Aim**: net negative LOC. Target ≥ 5,000 lines removed.

**Exit criteria**: `./gradlew clean test spotbugsMain` green; per-module
LOC summary in the PR description; smoke-tested on Linux + macOS;
no functional regressions reported.

### Phase 16 — Network reliability + timeouts + EDT off-loading (split: 16a + 16b)

Nothing in the app should hang the EDT or the JVM forever.

#### Phase 16a — process & timeout core (PR landed)

- **`ExternalCommand` extraction** (`barebones-commons-util/.../cli/ExternalCommand.java`):
  shared by `MountExecutor` and `TailscaleClient`. Drains stdout
  and stderr on dedicated daemon threads concurrently with the
  wait — fixes the stderr-pipe-buffer deadlock that previously
  hung any external invocation that emitted >64 KiB on stderr.
  Closes child stdin so CLI tools that read it don't block.
  Regression test pushes 256 KiB stderr through the helper.
  (`BUGS.md` 1.25, 6.1)
- **SFTP timeouts** (`barebones-protocol-sftp/.../SftpTimeouts.java`):
  three `-D` knobs — `barebones.sftp.connectTimeoutMs` (default
  15 000, was hardcoded 5 000), `barebones.sftp.readTimeoutMs`
  (default 60 000, applied as `Session.setTimeout` → SO_TIMEOUT),
  `barebones.sftp.serverAliveIntervalSec` (default 30, with
  `setServerAliveCountMax(3)` so dead sessions tear down after
  3× the interval). (`BUGS.md` 1.5 partial)
- **Polling-loop conversion** to `Timer`: `PropertiesDialog` (now
  a `javax.swing.Timer` that fires on the EDT — also fixes the
  prior off-EDT `JLabel` mutation), `QuickSearch` (single-shot
  `Timer` that restarts on each search-string change, replacing
  the dedicated polling thread). `FolderChangeMonitor` and
  `CompletionType` deferred to 16b. (`BUGS.md` 1.17 partial)
- **Shutdown hook extension** in `Bootstrap.shutdown()`:
  reflectively drains `MountRegistry` (best-effort unmount with
  exception swallowing) and invokes `S3 Activator.shutdown()`
  which closes every cached `S3Connection` (releases AWS SDK
  Netty pools). Augments the Phase-14 `SecretStoreService` close.
  (`BUGS.md` 4.3, 4.6, 1.19 partial)

#### Phase 16b — remainders (next PR)

- **NFS / Sun-RPC timeouts**: patch the vendored `com.sun.rpc`
  socket call sites in `barebones-protocol-nfs` to set a
  `Socket.setSoTimeout` (default 30 s, configurable via
  `Tunables`). (`BUGS.md` 1.4)
- **libsecret D-Bus timeouts**: thread `GCancellable` into every
  `secret_password_*_sync` call; cancel after 5 s default.
  (`BUGS.md` 4.1)
- **Mount retry/backoff**: `MountExecutor.withRetry(spec, n,
  backoff)` — default 3 attempts with exponential backoff for
  mount NFS portmap flakes. (`BUGS.md` 4.2)
- **`SwingWorker` shim for S3 uploads** + `TransferListener`
  wiring (foundation for Phase 19's progress dialog).
  (`BUGS.md` 1.12, 2.1)
- **Polling-loop conversion** for `FolderChangeMonitor` daemon
  tick and `CompletionType`. (`BUGS.md` 1.17 remainder)
- **WeakHashMap listener fix**: replace with `EventListenerList` /
  `ListenerSupport` in `ThemeManager`, others. (`BUGS.md` 4.5)

**Exit criteria** (whole phase): integration test of a hung NFS
server returns a `SocketTimeoutException` within
`Tunables.nfsReadTimeoutMs`; S3 upload in the app no longer
freezes the UI; shutdown hook fires cleanly on `kill -TERM`.

### Phase 17 — Concurrency + correctness sweep (one PR)

Latent bugs that haven't bitten yet because of single-threaded
luck. Substantial because there are many sites.

- **Mutable static collections** → `ConcurrentHashMap` /
  `CopyOnWriteArrayList` in `BookmarkManager`,
  `CredentialsManager`, `ActionProperties`. (`BUGS.md` 1.3)
- **Empty-catch sweep**: 31+ instances. New
  `dev.barebones.commander.commons.util.IgnoredErrors.ignored(t,
  why)` (logs at `TRACE`) for the legitimate cases; fix or surface
  the rest (auth/bookmark parser failures should warn). (`BUGS.md`
  1.20, 6.5)
- **NPE / stream-leak fixes** in `EditBookmarksDialog`,
  `ThemeManager`, `LocalFile.getChannel`. (`BUGS.md` 1.21, 1.22, 1.23)
- **Mount username injection**: tighten `MountSpec` validation —
  reject `=`, `,`, `:` in usernames; add a regression test.
  (`BUGS.md` 1.24)
- **`AbstractArchiveFile.createEntriesTree()` thread safety**:
  if Phase 13 didn't ship the lock, add it here.
- **Equals/hashCode**: any of the 8 SpotBugs entries Phase 14
  didn't already cover. (`BUGS.md` 1.9)

**Exit criteria**: `IgnoredErrors` adopted; SpotBugs baseline
shrinks by the equals/hashCode + ST_WRITE entries this phase
fixes; no remaining `catch (Exception e) {}` in `barebones-core`
without an `IgnoredErrors` call or a comment explaining why.

### Phase 18 — Observability + logging (one PR)

So that production failures stop being mysteries.

- **S3 module**: zero log lines today; add `LOGGER.debug` at every
  request entry, `LOGGER.warn` on AWS error responses with the
  service-name + key + AWS error code (no credentials, no payload).
  (`BUGS.md` 3.1)
- **`MountExecutor`**: log stderr at `WARN` when exit ≠ 0.
  (`BUGS.md` 3.2)
- **Tailscale**: include sanitised argv in timeout-message context.
  (`BUGS.md` 3.3)
- **`ThemeManager`**: file path + reason in every catch site.
  (`BUGS.md` 3.5)
- **AppleScript**: DEBUG line on REPLACE-branch decoder events.
  (`BUGS.md` 3.6)
- **SFTP**: auth-failure log at `warn`, not `info`. (`BUGS.md` 3.7)
- **AppleScript output bound**: 1 MiB cap with a
  "(... output truncated ...)" marker. (`BUGS.md` 1.16)
- **Lingering `System.out` / `System.err`** → SLF4J. (`BUGS.md` 1.26, 1.27)
- **ZipArchiveFile UTF-8 hard-coding**: respect EFS bit, fall back
  to the per-archive default-encoding hint. (`BUGS.md` 1.28)
- **Documentation**: a short `LOGGING.md` defining the four levels
  (`error` user-visible / `warn` should be triaged / `info`
  user-visible state changes / `debug` developer triage), what
  must NEVER appear (passwords, tokens, full URLs with creds,
  large request payloads), and the SLF4J idioms we use.

**Exit criteria**: every IO/network operation has at least a debug
log; no `System.out`/`System.err` in production source; greppable
"never log" rules in `LOGGING.md`; CI grep gate added (analogous
to the Phase-5 `no-tls-bypass` gate) that fails on
`LOGGER.*password\|LOGGER.*secret\|LOGGER.*credentials`.

### Phase 19 — UX polish (one PR; may split into 19a / 19b)

Depends on the primitives delivered by 13–18.

- **Progress dialogs**: S3 multipart uploads (uses the
  `TransferListener` wired by Phase 16); folder browses ≥ 1 s use
  a deferred spinner. (`BUGS.md` 2.1, 2.2)
- **"Operation failed" details**: every `JOptionPane.ERROR` gets
  an expandable "Show details" pane carrying the underlying
  exception's message + class. (`BUGS.md` 2.4)
- **Mount-error next-step hints**: parse common `mount.nfs` /
  `mount.cifs` / `sshfs` error patterns, show suggestions.
  (`BUGS.md` 2.5)
- **S3 401/403/404 distinction**: powered by the Phase-14 / Phase-16
  `S3ErrorHandler`. (`BUGS.md` 2.6)
- **Tailscale "not installed" banner**: top-of-window status row
  on first launch when the binary is missing. (`BUGS.md` 2.7)
- **Preferences Cancel-reverts**: snapshot at open, restore on
  Cancel. (`BUGS.md` 2.8)
- **Default-button focus** on every dialog (`InformationDialog`,
  `QuestionDialog`, etc.). (`BUGS.md` 2.9)
- **Keychain prompt explainer**: status-bar one-liner the first
  time the OS keychain authorisation pops up. (`BUGS.md` 2.11)
- **Drop-target writability**: reject the drop gesture (cursor
  changes) on read-only targets. (`BUGS.md` 2.12)
- **Bookmark deletion**: re-enable with a confirmation prompt.
  (`BUGS.md` 2.3)
- **Batch rename preview**: show the rename map before applying.
  (`BUGS.md` 2.3)

**Exit criteria**: manual UX checklist (in the PR description)
walks the 11 items above; smoke on Linux + macOS.

### Phase 20 — SpotBugs baseline drawdown to zero (one PR; may split per pattern)

The end-of-PLAN cleanup. Goal: delete `config/spotbugs/exclude.xml`.

After Phases 14, 17, 18 the baseline already shrinks. What's left
gets fixed here:

- **`DM_DEFAULT_ENCODING` × ~41**: every site goes through
  `StandardCharsets.UTF_8`. Likely the largest mechanical chunk.
- **`ST_WRITE_TO_STATIC_FROM_INSTANCE_METHOD` × ~15**: refactor to
  the `LastValues` holder pattern used by Phase 10b/11/12 panels.
- **`DMI_RANDOM_USED_ONLY_ONCE` × ~5**: shared `SecureRandom` field.
- **`MS_SHOULD_BE_FINAL` × ~4**: add `final`.
- **`DE_MIGHT_IGNORE` × ~3**: catch what should be caught.
- **`NM_SAME_SIMPLE_NAME_AS_SUPERCLASS` × ~3**: rename.
- **`MS_MUTABLE_ARRAY` × ~2**: defensive copy or `unmodifiable`.
- **`ES_COMPARING_PARAMETER_STRING_WITH_EQ` × ~2**: `.equals()`.
- **Various ×~8**: case-by-case.
- **Vendored ×33**: keep package-level suppressions; comment why
  ("`com.sun.*`/`sun.net.www.*` is upstream-vendored, not ours
  to fix") and leave them.

**Exit criteria**: `config/spotbugs/exclude.xml` contains only the
vendored-package suppressions; no `<Match>` entry under our own
namespace remains; CI green.

### Phase 21+ — Architecture refactors (parking lot)

`BUGS.md` §5 lists the architecture observations and §6 lists
candidate refactors. **Both are review-only.** The refactors that
graduate to a Phase 21+ PR are up to the user. Candidates:

- 21a — `AbstractFile` decomposition into focused interfaces
  (`FileAttributes`, `FileIO`, `FileURL` handler).
- 21b — Replace reflection-based `Bootstrap` discovery with
  `java.util.ServiceLoader`. Concurrent split of leftover OSGi
  package names (`...commons.file.osgi.*`).
- 21c — Vendored `apache-bzip2` → direct dep on
  `org.apache.commons:commons-compress` (already pulled in by
  `barebones-archiver`).
- 21d — Connectivity panels (Mount, Tailscale, S3) into a new
  `barebones-ui-connect` module, leaving `barebones-protocol-api`
  honestly protocol-only.
- 21e — Per-format `Activator` pattern → single `ServiceLoader`
  registry.
- 21f — Strongly-type Action parameters: replace `Map<String,
  Object>` with sealed interface / records per action shape.
- 21g — Centralised `Tunables` for the 30+ scattered timeout / poll
  constants.

None of the above runs without explicit "do 21x" from the user.

## 7. Compatibility with upstream

We may want to **pull bug fixes from upstream muCommander** for at least 1 year. To keep this cheap:

- Do not rewrite history of `main`. Use **squash merges** on every PR to keep `main` linear.
- Track upstream as `git remote upstream` (already configured locally). Periodically `git fetch upstream` and cherry-pick relevant fixes.
- Resolve path conflicts manually (`com/mucommander/` → `dev/barebones/commander/`).

## 8. Risks

| Risk | Mitigation |
|---|---|
| Phase 1 deletes too much in one shot | Phase 1 is mechanical: deletion + `settings.gradle` + `build.gradle` updates. The grep on cross-module imports before merging catches accidental coupling. CI provides a final gate. |
| Removing OSGi (Phase 2) breaks SPI registration in subtle ways | Phase 2 introduces a single `Bootstrap` class that explicitly registers every provider. Smoke-run on Linux + macOS before merge. |
| Java 25 changes private-API access we relied on | The surviving code touches little reflective API. The `--add-opens` / `--add-exports` lists in `build.gradle` already enumerate them; review and prune. |
| Keychain integration becomes a long thread | Ship the AES-GCM fallback first; keychain integration can land as an additive Phase-5 or follow-up PR if it stretches. |
| Trademark complaint from upstream maintainer | Renaming + `NOTICE` preempts this. We do not claim affiliation with upstream. |
| Maintainer bus factor of 1 | Documented everything; release signing + SBOM (Phase 8) lets a future maintainer verify supply chain. |

## 9. Open questions / decisions deferred

1. **Class rename of `muCommander.java`** — defer until a clear opportunity (Phase 8 polish or its own micro-PR when the user asks).
2. **TestNG → JUnit 5 migration** — non-blocking. Defer.
3. **Apple Developer ID for notarization** — until acquired, ship ad-hoc-signed DMG and document the `xattr -d` workaround.
4. **GPLv3 → AGPLv3** — no. We are a desktop app, not a network service.
5. **Translation maintenance** — keep upstream `dictionary_*.properties` files. New translation contributions: blocked by §2 (no contributions) until v1.0.
6. **macOS L&F: keep VAqua or rely on FlatLaf macOS variant** — drop VAqua in Phase 1 (§5.2 vendored helpers — also covers the upstream `fix #1458` "filter out vaqua for macOS 13+" workaround).
7. **JRE submodule** (`.gitmodules` still points at `mucommander/JRE`) — replace with a build-time-downloaded JDK or unbundled assumption in Phase 8.
8. **rsync support** — not present in upstream and not in scope for v1.0. The kept VFS SPI (`barebones-protocol-api`, see §1.10) means a future `barebones-protocol-rsync` plug-in can be added as an additive PR without core changes when there is a use case.
9. **WebDAV** — same path as rsync: out of scope for v1.0; pluggable later. (SMB is reachable via Phase 10's mount helper; NFS — both v2/v3 in-process and v4 via the mount helper — is in scope per §5.1 / §1.2.)
10. **S3 endpoint configuration UI** — AWS SDK v2 makes `--endpoint-override` for MinIO / Ceph / R2 trivial in code, but a UX surface for non-AWS S3 endpoints needs design. Treat as a follow-up after Phase 4 lands the SDK swap.
11. **Tailscale auth fallback** — `tailscale status --json` requires the local user to be the same user running tailscaled (or `sudo`). Decide what we do on macOS sandboxed installs of Tailscale where the socket isn't reachable: degrade to "Tailscale not detected" and let the user type peer hostnames manually (MagicDNS still resolves them).
12. **Mount-helper privilege escalation** — Linux NFS mounts typically need root. Either prompt for `pkexec` / `sudo` and re-invoke, or document that the user must pre-add their account to `/etc/fstab` with `users` mount option. Phase 10 picks `pkexec` first if available, falls back to documenting fstab.

## 10. Quick reference — workflow conventions

- **One branch / one PR in flight.** The user decides when a PR is ready and signals start of the next branch. The LLM does not autonomously open the next PR.
- **Branch naming**: `phase-N/short-description` (e.g. `phase-1/strip-out-of-scope`).
- **Commit author** for all our commits: `Adrian Mârza <adi11235 at gmail dot com>` (intentional non-RFC email; configured per-repo, not globally).
- **Commits unsigned** until Phase 8; from then on all commits must be signed.
- **PRs always squash-merge** to `main`. PR title = future commit title. PR body = brief "what + why + test plan".
- **No direct pushes to `main`.** No `--no-verify` for hooks. No `--amend` of pushed commits.
- **No CONTRIBUTING.md / no outside contributions** until v1.0. The user lifts this when ready.
