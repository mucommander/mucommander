# PLAN — `barebones-commander`

A **barebones**, security-first fork of muCommander focused on a small mouse-driven dual-pane file manager with **SFTP/SSH** as the only remote protocol, on **Linux + macOS**.

Source: forked from https://github.com/mucommander/mucommander to https://github.com/e6qu/mucommander-fork on 2026-05-08. Will be renamed and cleaned in-place.

> Companion docs in this repo:
> - `LIBRARIES.md` — current architecture & full library inventory.
> - `SECURITY_REVIEW.md` — full Critical/High vulnerability audit.

---

## 1. Goals

1. Ship a **small** dual-pane file manager built on the well-tested muCommander UI core.
2. **One** remote protocol family: SSH/SFTP. Local FS + SFTP only.
3. **Two** OS targets: Linux (x86_64, aarch64) and macOS (Apple Silicon + Intel).
4. **No** unpatched Critical/High vulnerabilities at v1.0 release.
5. **Latest LTS Java** (Java 25 LTS) as the runtime target.
6. **Mouse-driven UX** with drag & drop and full keyboard bindings preserved.
7. Modern, **non-OSGi** packaging — single fat JAR / native installers, no Felix container.
8. Clean **rename and rebrand** to remove muCommander trademark concerns.
9. **PR-only** workflow — every change lands via a reviewed PR on `e6qu/mucommander-fork`.

## 2. Non-goals (explicitly removed scope)

| Removed | Why |
|---|---|
| Windows / OpenVMS / macOS-Java-8 OS adapters | Out of stated scope. |
| FTP, HTTP, HTTPS browsing | Out of scope; HTTP bundle also carries the JVM-wide TLS bypass (SECURITY_REVIEW §5.1). |
| SMB (`jcifs-ng` + `smbj`) | Out of scope. |
| S3 (`jets3t`) | Out of scope. Drops `mail.osgi-1.4.jar` (legacy JavaMail) along with it. |
| Dropbox / Google Drive / OneDrive / Google Cloud Storage / Azure | Out of scope. Drops `azure-identity`, `microsoft-graph`, `dropbox-core-sdk`, `google-api-client`, `google-oauth-client-jetty`. |
| Hadoop / HDFS, NFS, oVirt, vSphere, ADB, Windows Registry | Out of scope. Drops `hadoop-client`, `avro`, `vim25.jar`, `jadb-v1.2.1.jar`. |
| RAR / 7z / ISO / RPM / cpio / ar / lst archive formats | Out of scope. Drops `junrar` (CVE-2026-28208, CVE-2026-41245) + `sevenzipjbinding` (license-grey via UnRAR). |
| `libguestfs` format (WIP upstream) | Out of scope. |
| Image viewer / PDF viewer / binary (hex) viewer | Out of scope; "barebones" GUI. Drops `icepdf-viewer`, all TwelveMonkeys imageio. |
| Embedded terminal widget | Out of scope. Drops `jetbrains-jediterm`, `pty4j`, `purejavacomm`. Users can use a real terminal app for SSH command sessions. |
| Bonjour / mDNS discovery | Out of scope. Drops `jmdns`. |
| OSGi runtime (Apache Felix) | Replaced by a plain JVM application + `jpackage`. |

## 3. Target stack

| Layer | Choice |
|---|---|
| Runtime | **Java 25 LTS** (latest LTS as of 2026-05-08) |
| Build | Gradle 8.x with **Kotlin DSL** (migrate from Groovy DSL gradually, optional) |
| Module system | Plain JAR + classpath (or JPMS modules if cheap). **No OSGi.** |
| UI | Swing + FlatLaf 3.x (post-3.0 line) |
| L&F on macOS | FlatLaf macOS variants (drop VAqua — GPLv3 already, but unmaintained for newer macOS). |
| Logging | SLF4J + Logback 1.5.x |
| SFTP | `com.github.mwiede:jsch` (latest, ≥ 0.2.21 — fixes Terrapin CVE-2023-48795) |
| Native interop | JNA 5.14+ (single pinned version; macOS quarantine, trash-to-bin, etc.) |
| YAML config | SnakeYAML 2.4+ |
| Packaging | `jpackage` for DMG (macOS) and DEB/RPM/AppImage (Linux) |
| CI | GitHub Actions (`ubuntu-latest`, `macos-15` matrix only) |
| Tests | JUnit 5 (migrate off TestNG over time) |

## 4. Licensing & trademark posture

### 4.1 Project license

- Upstream is **GPLv3**. We **stay on GPLv3** — there is no relicensing path without re-collecting CLAs from every contributor, and GPLv3 is fine for our purposes.
- We add **`SECURITY.md`**, **`CONTRIBUTING.md`**, and a **`NOTICE`** file aggregating third-party licenses.
- Every preserved upstream file keeps its existing GPL header. New files we author also use the GPLv3 header.

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
| `commons-compress` | Apache 2.0 | ✅ |
| XZ for Java | Public Domain | ✅ |
| Apache bzip2 (vendored from Ant) | Apache 2.0 | ✅ |
| `mbassador` | MIT | ✅ |
| `jcommander` | Apache 2.0 | ✅ |
| `log4j-core`, `log4j-1.2-api` | Apache 2.0 | ✅ |
| TestNG / JUnit 5 (test-only) | Apache 2.0 / EPL 2.0 | ✅ |

**Removing** the cloud/SMB/RAR/PDF/image deps **also removes the awkward licenses** — e.g. `junrar`'s effective UnRAR-license (no-modification clause for unrar code), `sevenzipjbinding`'s embedded UnRAR DLL bundle, and the abandoned `jets3t` chain.

`jsr305` (FindBugs annotations, `compileOnly`) has a non-standard license the FSF flags. Action: replace with `org.jetbrains:annotations` (Apache 2.0) — it's already pulled in transitively.

### 4.3 Trademark

- "muCommander" is the project's brand. There is no clearly registered USPTO trademark, but **common-law trademark** rights exist from continuous use since 2002 by Maxence Bernard / Arik Hadas. The mucommander.com domain, the icon set, and the logo are owned by the project.
- Forking a GPLv3 codebase is fine — keeping the **brand** while substantially diverging is **not** fine and creates legal & ethical issues.
- **Decision: rename the entire project to `barebones-commander`** for both correctness and clarity (per user direction).
- The fork repo is named `mucommander-fork` to keep upstream-fork lineage visible to GitHub; the **product name** is `barebones-commander`. We can later rename the repo to `barebones-commander` once the rename PR-train completes.

### 4.4 What rename touches

| Surface | Change |
|---|---|
| Display name | `muCommander` → `barebones-commander` (or `Barebones Commander` for title-case UI use) |
| Java package root | `com.mucommander.*` → `dev.barebones.commander.*` (one mass-rename PR; preserves history via `git mv`) |
| Gradle root group | `org.mucommander` → `dev.barebones.commander` |
| Gradle root version | reset to `0.1.0-SNAPSHOT` |
| JAR / executable name | `mucommander.jar`, `mucommander.exe` → `barebones-commander.jar`, etc. |
| App bundle id (macOS) | `com.mucommander.muCommander` → `dev.barebones.commander` |
| Linux desktop entry / `.desktop` | `mucommander` → `barebones-commander` |
| Icons | Original `mucommander.ico`, `icon.icns`, `icon128_24.png`, etc. **must** be replaced with new artwork (the upstream icons are GPL but are **identifying brand assets** and a trademark concern). Stub with a simple placeholder until designed. |
| About dialog / splash | Drop the muCommander logo. New text only: "barebones-commander, a fork of muCommander". |
| URLs | `https://www.mucommander.com` → `https://github.com/e6qu/mucommander-fork` (until a project URL exists) |
| Code-format spec | `mucommander-code-format.xml` → `barebones-commander-code-format.xml` |
| `i18n` keys | Change literal strings containing "muCommander" in `dictionary_*.properties` files |
| `LICENSE` | Keep upstream verbatim (we're still GPLv3) but add a clear `NOTICE` saying this is a fork |
| `README` | Rewrite from scratch |

We **do not** remove copyright lines from upstream files — those stay (GPL requires preservation of copyright notices). We **add** a "Forked from muCommander, …" line at the top of files we substantially modify.

## 5. Module triage — keep / drop

### 5.1 KEEP (with small touches)

| Module | Notes |
|---|---|
| `mucommander-core` | Main Swing UI. Touch points: remove menu items / actions referring to dropped protocols & viewers. |
| `mucommander-core-preload` | Bootstrap — keep. |
| `mucommander-commons-file` | File abstraction. Strip protocol-specific subpackages; keep `local`. |
| `mucommander-commons-io` | Stream / I/O utils. Keep. |
| `mucommander-commons-collections` | Keep. |
| `mucommander-commons-conf` | XML config. **Apply XXE hardening (SECURITY_REVIEW §5.5).** |
| `mucommander-commons-runtime` | Keep. |
| `mucommander-commons-util` | Keep. |
| `mucommander-preferences` | Keep. |
| `mucommander-translator` | Keep. Optional: prune languages we won't maintain — though shipping them is cheap. |
| `mucommander-encoding` | Keep. |
| `mucommander-process` | Keep. |
| `mucommander-command` | Custom-command feature. **Apply XXE hardening.** |
| `mucommander-protocol-api` | SPI. Keep. |
| `mucommander-protocol-sftp` | **The only protocol module.** Bump `jsch` to fix Terrapin. |
| `mucommander-os-api` | Keep. |
| `mucommander-os-linux` | Keep. **Refactor `KdeConfig` to `ProcessBuilder(List)` (SECURITY_REVIEW §5.4).** |
| `mucommander-os-macos` | Keep. |
| `mucommander-archiver` | Keep — needed for "compress to zip/tar" actions. |
| `mucommander-format-zip` | Keep. |
| `mucommander-format-tar` | Keep. |
| `mucommander-format-gzip` | Keep. |
| `mucommander-format-bzip2` | Keep. |
| `mucommander-format-xz` | Keep. |
| `apache-bzip2` (vendored) | Keep — needed by bzip2 module. |
| `mucommander-viewer-api` | Keep. |
| `mucommander-viewer-text` | Keep — minimal text viewer. |

### 5.2 REMOVE — protocols

`adb`, `bonjour`, `dropbox`, `ftp`, `gcs`, `gdrive`, `hadoop`, `http`, `nfs`, `onedrive`, `ovirt`, `registry`, `s3`, `smb`, `vsphere`.

### 5.3 REMOVE — archive formats

`ar`, `cpio`, `iso`, `libguestfs`, `lst`, `rar`, `rpm`, `sevenzip`. Removes `junrar` (CVEs), `commons-vfs2` (CVE-2025-27553), `sevenzipjbinding` (license-grey).

### 5.4 REMOVE — viewers

`binary` (hex), `image`, `pdf`. Drops `icepdf-viewer` and the entire TwelveMonkeys imageio set.

### 5.5 REMOVE — OS adapters

`win`, `openvms`, `macos-java8`.

### 5.6 REMOVE — vendored helpers

`jetbrains-jediterm`, `sevenzipjbindings`, `gson` (re-bundled), `kotlin-reflect`, `sun-net-www` (HTTP-protocol helper).

The module `gson` (the vendored re-bundle at `./gson/`) gets removed; if any kept module still needs Gson it can pull `com.google.code.gson:gson` directly.

### 5.7 Effective module count

- **Before**: 70 subprojects
- **After**: ~26 subprojects (≈ 63% reduction)
- Source LOC drop estimate: at least 25–35% (will be measured in Phase 1 close-out).

## 6. Phased delivery

Each phase is **one or more PRs** against `e6qu/mucommander-fork:master`, merged by squash. Tests must remain green throughout. We **do not** rename the project (Phase 6) until Phases 1–5 are done — keeps PR diffs small.

### Phase 0 — Bootstrap (≤ 3 PRs)

- **PR-0.1** *(this PR)*: `LIBRARIES.md`, `SECURITY_REVIEW.md`, `PLAN.md`. No code change.
- **PR-0.2**: Add `SECURITY.md` with disclosure email/policy and short-lived placeholder until v1.0; add `CONTRIBUTING.md`.
- **PR-0.3**: Tighten CI — remove Windows matrix from any workflow we keep, prune nightly DMG signing (we don't have an Apple Developer ID yet), keep `tests.yaml` running on `ubuntu-latest` + `macos-15`. Add `gradle/wrapper-validation-action` already present — leave in.

**Exit criteria**: green CI, planning docs landed.

### Phase 1 — Strip unwanted modules (5–8 PRs)

Each PR removes one logical group, in this order (small-blast-radius first):

1. **PR-1.1** Remove obscure formats: `ar`, `cpio`, `lst`, `libguestfs`. (No deep coupling.)
2. **PR-1.2** Remove RAR / 7z / ISO / RPM / sevenzipjbinding & vendored `sevenzipjbindings/`.
3. **PR-1.3** Remove image / pdf / binary viewers + their TwelveMonkeys / icepdf deps.
4. **PR-1.4** Remove cloud protocols: `s3`, `dropbox`, `gdrive`, `gcs`, `onedrive`. Drop the corresponding entries from CI's "release" patch checkout.
5. **PR-1.5** Remove enterprise/network protocols: `hadoop`, `nfs`, `ovirt`, `vsphere`, `registry`, `adb`, `bonjour`, `smb`, `ftp`, `http`. **This is also where the JVM-wide TLS bypass dies (SECURITY_REVIEW §5.1).**
6. **PR-1.6** Remove embedded terminal: `mucommander-core/.../ui/terminal/*`, `jetbrains-jediterm`, pty4j/purejavacomm dependency lines.
7. **PR-1.7** Remove OS adapters: `win`, `openvms`, `macos-java8`. Adjust top-level build.gradle (drop the launch4j Windows EXE task; drop msi/winAppImage/winshortcut tasks; drop OS-specific `osgiRuntime` lines).
8. **PR-1.8** Cleanup pass: remove now-orphaned UI menu actions, image resources, dictionary entries, settings keys, and removed actions' keymap entries.

After each PR, run `./gradlew test` and a manual smoke run.

**Exit criteria**: app builds and runs on Linux + macOS with only local + SFTP file panels; all CI green.

### Phase 2 — Drop OSGi runtime (3 PRs)

OSGi via Apache Felix is the upstream's modularity choice; for a one-protocol app it's pure overhead.

1. **PR-2.1** Stop generating OSGi manifests in `subprojects { ... }` block — remove `biz.aQute.bnd.builder` plugin and `bnd { ... }` blocks per subproject. Modules become plain Java libraries.
2. **PR-2.2** Replace `Activator` classes (each protocol/format/viewer module has one) with explicit registration calls in a new `dev.barebones.commander.bootstrap.Bootstrap` class invoked from `main`. This keeps the SPI contract (file/format/viewer providers register themselves) without OSGi service tracking.
3. **PR-2.3** Replace `runOsgi` Gradle task & Felix runtime with `application`-plugin `run` task, single fat JAR via `shadowJar` (or simple `jar { from configurations… }`). Drop the `osgi/`, `bundle/`, `app/`, `conf/` runtime layout. Adjust `jpackage` invocations.

**Exit criteria**: `./gradlew run` launches the app without Felix; produced JAR runs via `java -jar barebones-commander.jar`.

### Phase 3 — Java 25 LTS upgrade (2 PRs)

1. **PR-3.1** Bump `compileJava.options.compilerArgs += ['--release', '25']` everywhere, set `JavaVersion.VERSION_25` in toolchain via Gradle's `java.toolchain` block. Update CI matrix to `java-version: '25'` (Temurin or Adoptium). Fix any `--add-opens` / `--add-exports` lists for current JDK module names.
2. **PR-3.2** Modernize: replace deprecated APIs flagged by `--release 25` (e.g. removed `SecurityManager`, finalize-related, `Thread.stop`, etc.). Replace `var` where it improves readability. Adopt switch expressions / pattern matching where it cleans up file-type dispatch code.

**Exit criteria**: app builds & passes tests on Java 25.

### Phase 4 — Dependency upgrades (2 PRs)

(After pruning, the surviving upgradable deps are smaller.)

1. **PR-4.1** Bump:
   - `mwiede:jsch` 0.2.10 → 0.2.21+ (fixes Terrapin CVE-2023-48795)
   - Logback 1.2.13 → 1.5.x (now safe — no longer carry the legacy 1.2 chain since we've moved off Java 8)
   - SLF4J 1.7.36 → 2.0.x
   - SnakeYAML 2.3 → 2.4
   - JNA — pin a **single** version (≥ 5.14) project-wide (fixes the 5.5.0/5.12.1 split)
   - FlatLaf 2.6 / 2.2 → 3.x — pin a **single** version (fixes the 2.6/2.2 split)
   - ICU4J 78.3 → latest
   - Gson 2.11.0 → 2.11.x latest
   - Bouncy Castle 1.79 → latest 1.x
   - commons-compress 1.28.0 → latest
   - log4j-core 2.25.3 → latest
   - jcommander 1.82 → latest
2. **PR-4.2** Add **Dependabot config** (`.github/dependabot.yml`) for `gradle` ecosystem, weekly cadence; add `dependency-review-action` to CI.

### Phase 5 — Code-level security fixes (4 PRs)

These map 1:1 to `SECURITY_REVIEW.md` §5.

1. **PR-5.1** *Already completed in Phase 1 by deleting the HTTP module:* JVM-wide TLS bypass is gone. Add a CI check (grep gate) that fails the build if `setDefaultSSLSocketFactory` or `setDefaultHostnameVerifier` ever reappear in the tree.
2. **PR-5.2** Replace `XORCipher` for stored credentials. Two-stage:
   - **5.2a** Add OS keychain integration: macOS Keychain via JNA (`Security.framework`); Linux libsecret via JNA. Behind a feature flag in preferences.
   - **5.2b** Default to keychain on first run; for back-compat, decrypt legacy `XORCipher`-protected `credentials.xml` once and re-write into keychain, then delete the XML field. Keep an opt-out env var for users who can't use a keychain.
3. **PR-5.3** Harden the 9 SAX entry points (theme, bookmarks, action keymap, toolbar, command bar, association, command, credentials, configuration) — set `FEATURE_SECURE_PROCESSING=true` and `disallow-doctype-decl=true`. Add a small test that loading an XML with a DOCTYPE → throws.
4. **PR-5.4** Refactor `KdeConfig.exec(String + key)` to `ProcessBuilder(List.of(...))`. (Even if unreachable in practice, removes the SAST finding.)

**Exit criteria**: a clean run of `spotbugs` + `dependency-check` + `pmd` (added in Phase 9) reports no Critical / High.

### Phase 6 — Rename to `barebones-commander` (3 PRs)

1. **PR-6.1** Mass `git mv` of `com.mucommander.*` → `dev.barebones.commander.*`. One commit. Includes touch-ups to imports, gradle `Bundle-Activator`, OSGi `Export-Package` (already deleted in Phase 2 but any leftover refs), `Specification-Title`, `Implementation-Title` strings.
2. **PR-6.2** Rebrand: remove muCommander icons, replace with placeholder vector icon for `barebones-commander`. Replace About dialog text. Replace product/title strings everywhere. Update `i18n` dictionaries (`s/muCommander/barebones-commander/g` mechanically; review hand-edits per language).
3. **PR-6.3** Rename outputs: JAR, Linux desktop entry, macOS bundle id, executable name. Update all `jpackage` invocations. Add `NOTICE` aggregating third-party licenses.

**Exit criteria**: `./gradlew run` shows the new name, icon, and About; produced installers are named `barebones-commander-*`.

### Phase 7 — Build system polish (2 PRs)

1. **PR-7.1** Migrate `build.gradle` (root + subprojects) to **Kotlin DSL** `build.gradle.kts`. Optional but pays dividends in IDE help.
2. **PR-7.2** Introduce **version catalog** (`gradle/libs.versions.toml`) — ends per-module pinning drift permanently.

### Phase 8 — Release & supply chain (3 PRs)

1. **PR-8.1** Replace upstream nightly/stable workflows with simpler `release.yml` that produces, on tag push:
   - Linux x86_64/aarch64 AppImage + DEB + RPM via `jpackage`
   - macOS aarch64/x86_64 DMG via `jpackage` + `notarytool` (when Apple Developer ID is configured; until then, ad-hoc-signed DMG).
2. **PR-8.2** Enable **commit signing** in CONTRIBUTING + branch-protection rule on `master` requiring signed commits. (Upstream has `N` for every commit — we won't.)
3. **PR-8.3** Add **SBOM** generation (`org.cyclonedx.bom` Gradle plugin) and publish `bom.cdx.json` on each release. Add **SLSA-style provenance** via `actions/attest-build-provenance`.

### Phase 9 — Static analysis CI (2 PRs)

1. **PR-9.1** Add **SpotBugs + FindSecBugs** as Gradle-driven CI step. Treat any High-severity finding as a CI failure.
2. **PR-9.2** Add **OWASP Dependency-Check** as scheduled weekly CI. Treat CVSS ≥ 7.0 as failure.

## 7. Compatibility with upstream

We may want to **pull bug fixes from upstream muCommander** for at least 1 year. To keep this cheap:

- Keep our package rename (Phase 6) **after** module pruning so upstream rebases of pruned-but-unrenamed modules are trivial 3-way merges.
- Do not rewrite history of `master`. Use **squash merges** on every PR to keep `master` linear.
- Track upstream as a remote (`origin` already set). Periodically `git fetch origin` and cherry-pick relevant fixes from `master`/`stable`.
- Document this dance in `CONTRIBUTING.md` (Phase 0.2).

## 8. Risks

| Risk | Mitigation |
|---|---|
| Removing OSGi (Phase 2) breaks SPI registration in subtle ways | Phase 2 is split into 3 PRs with smoke tests at each step. |
| Java 25 changes some private-API access we relied on | Phase 3 starts after pruning; the surviving code touches little reflective API. The `--add-opens` / `--add-exports` lists in `build.gradle` already enumerate them; review and prune. |
| Keychain integration (PR-5.2a) becomes a long thread | Keep a non-keychain fallback (encrypted with PBKDF2 + AES-GCM, master-passphrase-derived) so we don't block the rest of the plan. |
| Rebrand introduces UI regressions | PR-6.2 only touches strings, icons, and About — keep it isolated. |
| Trademark complaint from upstream maintainer | Renaming + adding NOTICE preempts this. We never claim to be the upstream. |
| Maintainer bus factor of 1 (us) | Document everything; SECURITY.md disclosure path; signed releases. |

## 9. Open questions / decisions deferred

1. **Rename packages now or never?** Phase 6 mass-rename. Decision: do it once at v0.5; not optional for trademark hygiene.
2. **Drop TestNG for JUnit 5?** Most of the codebase is TestNG; not blocking. Defer until Phase 9+ unless a particular module is being heavily refactored.
3. **App identity for macOS notarization** — needs an Apple Developer ID we don't have yet. Until then, ship ad-hoc-signed DMG and document the `xattr -d` workaround.
4. **GPLv3 → AGPLv3?** Probably no — we're a desktop app, not a network service. Stay GPLv3.
5. **Translation maintenance** — We keep the existing `dictionary_*.properties` files. Open question whether we accept new translation PRs from day 1 or freeze translations until v1.0.
6. **macOS L&F: keep VAqua or rely on FlatLaf macOS variant?** VAqua is GPL but stagnant; FlatLaf works fine on macOS in modern releases. Default to **drop VAqua** in Phase 1.5 (or the cleanup pass) — saves a dependency and a "filter out vaqua for macOS 13+" hack already in upstream (`fix #1458`).

## 10. Quick reference — commit & PR conventions

- **Branch naming**: `phase-N/short-description` (e.g. `phase-1/remove-rar-7z-iso`).
- **Commit author** for all our commits: `Adrian Mârza <adi11235 at gmail dot com>` (intentional non-RFC email; configured per-repo, not globally).
- **Commits unsigned** until Phase 8.2; from then on all commits must be signed.
- **PRs always squash-merge** to `master`. PR title = future commit title. PR body = brief "what + why + test plan".
- **No direct pushes to `master`.**
- **No `--no-verify`** to skip hooks. **No `--amend`** of pushed commits.
