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
| **0** | partly done | Bootstrap: audit docs, rename project, CI cleanup, plan adjustments | ✅ landed in #2; finished by #3 |
| **6** | done early | Rename to `barebones-commander` | ✅ landed in #2 |
| **1** | next | Strip every out-of-scope module (protocols, archive formats, viewers, terminal, OS adapters) | one PR |
| **2** | pending | Drop OSGi runtime — replace Felix + bnd manifests + bundle activators with a plain Java app + fat JAR | one PR |
| **3** | pending | Java 25 LTS upgrade | one PR |
| **4** | in flight | Dependency upgrades + Dependabot + dependency-review CI; **drop the abandoned `jets3t`-based S3 module** (its AWS-SDK-v2 reintroduction moves to Phase 11) | one PR |
| **5** | in flight | Code-level security fixes (XXE-harden SAX, refactor `KdeConfig.exec`, CI grep gate against `setDefaultSSLSocketFactory`). XOR-cipher → keychain split into Phase 12. | one PR |
| **7** | pending | Build polish (Kotlin DSL + version catalog) | one PR |
| **8** | pending | Release pipeline (DMG/DEB/RPM/AppImage via `jpackage`) + commit signing + SBOM | one PR |
| **9** | in flight | SAST in CI (SpotBugs + FindSecBugs PR-triggered + OWASP Dependency-Check weekly) | one PR |
| **10a** | done | Connectivity backends: `barebones-mount-helper` + `barebones-tailscale` modules (services, ProcessBuilder shell-out, parsing, full unit tests). | landed in #13 |
| **10b** | in flight | Connectivity UI tabs in the existing Connect-to-server dialog: `MountPanel` + `TailscalePeerPanel` registered via `ProtocolPanelRegistry`. | one PR |
| **10c** | pending | Connectivity polish: SwingWorker wrapper for long-running mount calls, active-mounts management dialog, dedicated `TaildropSendAction`. | one PR |
| **11** | pending | Re-add S3 backend on AWS SDK v2 (`software.amazon.awssdk:s3`); rewrite the S3 module's File / Bucket / Object / Root classes; verify against AWS S3 + MinIO. (Was Phase 4's stretch goal; lifted out because the rewrite is too large for Phase 4's bump-scope.) | one PR |
| **12** | pending | Replace `XORCipher`-based credential storage with OS keychain integration (macOS Keychain via JNA `Security.framework`; Linux libsecret via JNA). Passphrase-derived AES-GCM fallback when no keychain is available. Migrate any legacy `XORCipher`-protected `credentials.xml` once on first run, then delete the field. (Lifted from Phase 5 because keychain JNA bindings are non-trivial.) | one PR |

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
AES-GCM fallback path:

- Delete `dev.barebones.commander.bookmark.XORCipher` (the existing
  hard-coded-XOR "encryption" of stored credentials).
- Add `dev.barebones.commander.auth.SecretStore` SPI with three
  implementations:
  * macOS Keychain via JNA → `Security.framework` (`SecKeychainAddGenericPassword` / `SecKeychainFindGenericPassword`).
  * Linux libsecret via JNA → `secret_password_store_sync` / `secret_password_lookup_sync` (or fall back to D-Bus call to `org.freedesktop.secrets`).
  * Passphrase-derived AES-GCM blob in `~/.barebones-commander/credentials.bin` for headless / no-keychain environments. Key derivation via PBKDF2-HMAC-SHA-256 from a user-prompted passphrase, stored only in memory while the app runs.
- One-shot migration on first run: detect the legacy XOR-encrypted
  `credentials.xml`, decrypt it with the well-known XOR key, re-store
  via the chosen `SecretStore`, then delete the XML record.
- Surface `SecretStore` choice in preferences UI (auto-detect by
  default; user can override).

### Phase 11 — Re-add S3 backend on AWS SDK v2 (one PR)

Lifted out of the original Phase 4 plan because the refactor is too
large for a dep-upgrade PR. The Phase-4 PR removed the `jets3t`-based
module to clear the dep tree; this phase adds it back, properly
implemented:

- New `barebones-protocol-s3` subproject.
- `software.amazon.awssdk:bom:2.x` + `software.amazon.awssdk:s3` deps.
- `S3ProtocolProvider`, `S3Root`, `S3Bucket`, `S3Object`, `S3File`
  rewritten against the AWS SDK v2 API.
- `S3Panel` (the connection dialog) preserved from the deleted module's
  Git history; UI fields adjusted for AWS-SDK-style endpoint /
  credentials / region inputs.
- Manual smoke against AWS S3 + at least one S3-compatible
  endpoint (MinIO) before merge.
- CI integration test gated on `-Dtest_properties.s3_test.temp_folder`
  remains a SkipException in CI; the AWS SDK has its own LocalStack
  test harness that we may or may not adopt.

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

**10c exit criteria** (deferred): mount runs off the EDT via
SwingWorker; "Active mounts" dialog supports unmount per row;
"Send via Taildrop" file-action ships a selected file to a chosen
peer.

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
