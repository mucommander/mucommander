# muCommander Security Review

Audit performed against `mucommander/mucommander` HEAD `8662144` (2026-04-30). Threat model assumed: a desktop end-user running the file manager who may browse remote filesystems (FTP/SMB/HTTP/cloud), open archives received from third parties, and load themes/bookmarks/credentials from the local config dir.

---

## TL;DR — Currently-applicable Critical & High issues

| # | ID / Source | Class | Severity | Where | Exploitable today? |
|---|---|---|---|---|---|
| 1 | **mucommander code** (no CVE) | TLS validation completely disabled, JVM-wide | **Critical** | `mucommander-protocol-http/.../HTTPProtocolProvider.java:73-99` | Yes — the HTTP bundle's static initializer installs a permissive `X509TrustManager` and `HostnameVerifier` as the JVM default for **all** HTTPS, including cloud SDKs. |
| 2 | **mucommander code** (no CVE) | TLS validation disabled (vSphere only) | High | `mucommander-protocol-vsphere/.../VSphereClient.java:285-307` (`TrustAllTrustManager`) | Yes — only when using vSphere. |
| 3 | CVE-2026-28208 | Path traversal / arbitrary file write on archive extract | High | dep `com.github.junrar:junrar:7.5.5` (used by `mucommander-format-rar`) | Yes — opening a malicious `.rar` in a default workflow. |
| 4 | CVE-2026-41245 | Path traversal in `LocalFolderExtractor` | High | dep `com.github.junrar:junrar:7.5.5` | Yes — same code path. |
| 5 | CVE-2025-27553 | Path traversal in `commons-vfs2 resolveFile`/`DESCENDENT` | High (CVSS 7.5) | dep `org.apache.commons:commons-vfs2:2.3` (used by RAR module) | Possible — depends on whether DESCENDENT scope is invoked on user URIs. |
| 6 | CVE-2025-27821 | OOB write in HDFS native client | High (CVSS 7.3) | dep `org.apache.hadoop:hadoop-client:3.4.1` | Possible only when the user browses HDFS. |
| 7 | **mucommander code** | Hard-coded XOR "encryption" of stored credentials | High (data-at-rest) | `mucommander-core/.../bookmark/XORCipher.java` (`credentials.xml`) | Yes — anyone with read access to the user's profile dir recovers FTP/SMB/cloud passwords trivially. The class header literally says "this obviously is weak encryption at most". |
| 8 | Stale legacy bundled jar | JavaMail 1.4 (bundled `mail.osgi-1.4.jar`) | High (multiple CVEs in JavaMail 1.4 era) | `mucommander-protocol-s3/libs/mail.osgi-1.4.jar` | Low practical impact — only loaded indirectly via jets3t. Still a known-vulnerable artifact in the build. |
| 9 | Distro packaging | Most Linux distro packages are years out of date | High (downstream) | NixOS 1.5.2; Mageia/OpenMandriva/Rosa 0.9.x; SlackBuilds 1.3.0; RPM Sphere 0.8.5 | Yes — the older the package, the more issues 1–8 (and many more) apply. |

There are **no CVEs published against the muCommander project itself** other than the historical CVE-2008-1970 (insecure permissions on `credentials.xml`, fixed in 0.8.2). The project's GitHub Security Advisories tab is empty. All current Critical/High findings are either in dependencies or in muCommander's own code (TLS bypass, XOR cipher).

---

## 1. Project & maintainer health

| Signal | Observation |
|---|---|
| Total commits | 6,462 |
| Lifetime range | 2002 → 2026-04-30 |
| Top contributor (lifetime) | Maxence Bernard (2,434 commits) — **last commit 2013-05-16**; project effectively transitioned |
| Current maintainer | Arik Hadas (`@ahadas`) — 2,151+ commits; sole `*` CODEOWNER |
| Active contributors last 12 months | 13 (Arik Hadas, Shay Artzi, Piotr Skowronek, Daniel Erez, Douglas Cabral, hajdam, Krzysztof Wolny, Nicolas Filotto, Olivier Paul, ugurtafrali, VenusGirl, jobukkit, dependabot[bot]) |
| Suspicious onboarding / new committers | None observed. All recent committers either have prior history in the repo or made small isolated PRs (e.g. translation, single fixes). |
| Commit signing | **None** — `git log --pretty=%G?` returns `N` for every recent commit. No GPG/SSH signatures. |
| GitHub Security Advisories | Empty (no published advisories) |
| Funding | GitHub Sponsors (`mucommander` org) |
| Bus factor | **1** — Arik Hadas merges essentially everything; no co-maintainer with admin. |

**No suspicious recent activity** was observed: no force-pushes to `master` visible in last 100 commits, no surprise `release/*` tags, no new committer with elevated rights, no large auto-generated dep-bump PRs from unfamiliar accounts. Dependabot is the only bot author.

## 2. Patch cadence

Tagged releases and dates:

| Version | Date |
|---|---|
| 0.9.3 | 2018-11-11 |
| 0.9.6 | 2020-12-31 |
| 0.9.7 | 2021-05-05 |
| 1.0.1 | 2022-06-17 |
| 1.1.0 | 2022-10-09 |
| 1.3.0 | 2023-07-07 |
| 1.5.2 | 2024-10-18 (per upstream releases page) |
| 1.6.0 | 2026-03-07 |
| **1.6.1** | **2026-04-21** (current stable) |
| `nightly` | rolling, last 2026-05-01 |

Commit cadence over the last 24 months ranges 1–34/month, with a clear push of activity in 2026-Q1/Q2 (60+ commits in March–April 2026). The project is **actively maintained** but has **no security-release process** visible: no `SECURITY.md`, no `CVE` workflow, no CHANGELOG section labelling security-fix releases, no GPG-signed releases. Fixes ship inside normal feature releases. There is also no Dependabot alert workflow committed — although a `dependabot[bot]` author appears in the last 12 months, which suggests Dependabot PRs are at least configured for dep updates.

## 3. Linux distribution packaging

| Distro / Repo | Version | Behind 1.6.1? | Notes |
|---|---|---|---|
| Debian / Ubuntu / Fedora / RHEL / Arch (extra) / Gentoo / Alpine / openSUSE (official) | — | n/a | **Not packaged** at all in any mainstream distro |
| Arch AUR | **1.6.1** | current | The only Linux package tracking upstream stable |
| NixOS 24.11 → unstable | 1.5.2 | 1 minor behind | Pre-junrar-7.5.8 fix; carries items 3–4 above |
| NixOS 23.11 / 24.05 | 1.3.0 | yes | Multi-year-old |
| SlackBuilds | 1.3.0 | yes | Multi-year-old |
| Mageia 9 / Cauldron | 0.9.2 | yes (~6 yrs) | Predates almost every modern fix |
| OpenMandriva | 0.9.0 | yes | Predates almost every modern fix |
| Rosa 2021.1 / 13 | 0.9.2 | yes | Predates almost every modern fix |
| RPM Sphere (Fedora 3rd-party) | 0.8.5 | yes (~15 yrs) | Predates CVE-2008-1970 fix |
| openSUSE OBS `home:kkirill` | 0.9.2 | yes | Personal repo |
| Flathub | not packaged | n/a | Open request since 2021 (issue #540) |
| Snap | not packaged | n/a | Open request since 2021 (issue #447) |
| macOS Homebrew Cask | 1.6.1 | current | (out of scope for this section) |

Net: **the only Linux distribution shipping a current muCommander is the Arch AUR**. Every other distro package is either stale (NixOS at 1.5.2) or *very* stale (RPM-derived family at 0.9.x / 0.8.5). Anyone running muCommander from a distro package other than AUR is almost certainly running known-vulnerable code.

## 4. Dependency CVEs (Critical / High only)

Pinned versions and their status. Sources: NVD, GitHub Advisory Database, OSV.dev, GitLab Advisory.

### 4.1 Currently applicable (CVSS ≥ 7.0, fix not yet adopted)

| CVE | Component | Pinned version | CVSS | Description | Fixed in | Realistic impact in muCommander |
|---|---|---|---|---|---|---|
| **CVE-2026-28208** | `com.github.junrar:junrar` | 7.5.5 | High | Backslash-based path traversal ("Zip-Slip" variant) in `LocalFolderExtractor` allows arbitrary file write on Linux/Unix during RAR extract. | 7.5.8 | **Real** — RAR extraction is a primary feature. |
| **CVE-2026-41245** | `com.github.junrar:junrar` | 7.5.5 | High | Sibling-directory write via crafted RAR. | 7.5.10 | **Real** — same code path. |
| **CVE-2025-27553** | `org.apache.commons:commons-vfs2` | 2.3 | 7.5 | URL-encoded `..` traversal bypasses `NameScope.DESCENDENT`. | 2.10.0 | Possible — used inside RAR module, depends on whether DESCENDENT scope is invoked on user paths. The dep is also ~7 years out of date (2018 → 2026). |
| **CVE-2025-27821** | `org.apache.hadoop:hadoop-client` | 3.4.1 | 7.3 | OOB write in HDFS native client triggered by crafted URI. | 3.4.2 | Only when user browses HDFS. |
| **CVE-2025-33042** | `org.apache.avro:avro` | 1.11.4 | 7.3 | Code injection during Avro record codegen (CWE-94). | 1.11.5 / 1.12.1 | Unlikely — muCommander does not codegen from schemas at runtime; transitive only. |

### 4.2 Already mitigated by pinned version (no action needed)

`avro 1.11.4` is the *fix* version for CVE-2024-47561 (the high-profile RCE) — clean. `dd-plist 1.23` is post the XXE fix (`1.18`). `snakeyaml 2.3` is post-`2.0` lockdown (CVE-2022-1471). `bcprov-jdk18on 1.79` post-CVE-2025-8916. `json-smart 2.4.10` post-CVE-2023-1370. `logback 1.2.13` is **not** in the affected range for CVE-2024-12798 / 12801 (which target 1.3.x – 1.5.12). `log4j-core 2.25.3`, `commons-compress 1.28.0`, `commons-lang3 3.20.0`, `xz 1.9`, `jcommander 1.82`, `flatlaf 2.6`, `apache.felix.main 7.0.5`, `Microsoft Graph 5.67.0`, `azure-identity 1.9.2 (Java)` — no current Critical/High CVEs.

### 4.3 Below severity threshold but worth noting

- `commons-net 3.8.0` — CVE-2021-37533 (FTP redirect) is **Medium 6.5**. Should still be bumped.
- `mwiede:jsch 0.2.10` — Terrapin (CVE-2023-48795) is **Medium 5.9**, fixed in 0.2.15.
- `azure-identity 1.9.2` — CVE-2024-35255 is **Moderate 6.8**.
- `bcprov-jdk18on 1.79` — newer LDAP/timing issues exist but score Medium.

### 4.4 Stale or hard-to-track artifacts

These are *checked-in jars* not declared by Maven coordinates, so SCA tools won't see them:

| File | Identity | Concern |
|---|---|---|
| `mucommander-protocol-s3/libs/mail.osgi-1.4.jar` | JavaMail 1.4 (~2008) | Multiple CVEs across JavaMail 1.4–1.5 era; only loaded transitively via jets3t. |
| `mucommander-protocol-vsphere/libs/vim25.jar` | VMware vSphere SDK | No version metadata visible; vendored SDK. |
| `mucommander-protocol-adb/libs/jadb-v1.2.1.jar` | jadb v1.2.1 (~2018) | Old ADB client; small attack surface but stale. |
| `mucommander-format-libguestfs/libs/libguestfs.jar` | libguestfs Java binding | Project is "work-in-progress" per build.gradle. |

`org.jets3t:jets3t:0.9.7` itself is from 2015 and effectively abandoned upstream; it transitively pulls aged httpclient / commons-codec. No direct High/Critical CVE on `jets3t` itself, but the transitive surface is concerning.

### 4.5 Inconsistent versions across modules

- `com.formdev:flatlaf` is pinned to **2.6** in core but **2.2** in the PDF viewer. Two L&F versions in the same JVM is brittle and means a fix landed in one and not the other.
- `net.java.dev.jna:jna` is **5.5.0** in some modules and **5.12.1** in others.

## 5. Code-level findings

### 5.1 Critical — JVM-wide TLS validation bypass (HTTP bundle)

`mucommander-protocol-http/src/main/java/com/mucommander/commons/file/protocol/http/HTTPProtocolProvider.java`:

```java
static {
    try { disableCertificateVerifications(); } ...
}
private static void disableCertificateVerifications() throws Exception {
    TrustManager permissiveTrustManager = new X509TrustManager() {
        public X509Certificate[] getAcceptedIssuers() { return null; }
        public void checkServerTrusted(...) {}    // <-- accepts everything
        public void checkClientTrusted(...) {}
    };
    SSLContext sc = SSLContext.getInstance("SSL");
    sc.init(null, new TrustManager[]{permissiveTrustManager}, new SecureRandom());
    HttpsURLConnection.setDefaultSSLSocketFactory(sc.getSocketFactory());   // <-- JVM default
    HttpsURLConnection.setDefaultHostnameVerifier((host, session) -> true); // <-- JVM default
}
```

The static initializer fires the moment the HTTP OSGi bundle loads. It then sets `HttpsURLConnection.setDefaultSSLSocketFactory` and `setDefaultHostnameVerifier` — **JVM-wide defaults**, not per-connection. The class's own javadoc admits: *"This clearly is unsecure for the user, but arguably better from a feature standpoint than systematically failing untrusted connections."*

Impact: any code in the same JVM that uses `HttpsURLConnection` and does not install its own SSLSocketFactory will silently accept self-signed/expired/wrong-hostname certs. That's an active **MITM enabler** — a hostile network can intercept HTTPS browsing, and depending on whether the cloud SDKs (Dropbox/OneDrive/Drive/S3/Azure) use `HttpsURLConnection` underneath, it may also weaken those. Even if the bundled SDKs all bring their own HTTP clients (OkHttp/Apache HttpClient), this is still a strict-liability finding on a file manager that handles credentials over HTTPS. Also weakens protections against cert-pinning bypass for HSTS/HPKP-style assumptions.

Recommendation: prompt-on-untrusted instead of blanket-accept; never write to `HttpsURLConnection.setDefault*`.

### 5.2 High — Per-connection TLS bypass (vSphere)

`mucommander-protocol-vsphere/.../VSphereClient.java:285-307` defines a `TrustAllTrustManager` and installs it via `sc.init(null, trustAllCerts, null)`. Scope: vSphere only, but typical vSphere deployments use private CAs anyway, and a configurable cert-import flow would be safer.

### 5.3 High — Hard-coded XOR "encryption" of credentials

`mucommander-core/src/main/java/com/mucommander/bookmark/XORCipher.java` is used to "encrypt" passwords stored in `credentials.xml` (FTP, SMB, SFTP, S3 secret keys, OAuth tokens, etc.). Its own javadoc says:

> Disclaimer: this obviously is weak encryption at most, the key used being static and public, and XOR encryption being easy to crack.

This is the same class of issue as historical **CVE-2008-1970** (which was about file permissions). Today the file is in `~/.mucommander/`, but the contents themselves provide essentially no protection — anyone (malware, sibling user account, leaked backup) who can read the file can recover plaintext credentials with a few lines of code. For a tool that stores cloud-storage and SSH credentials, this is the most impactful local-attacker risk in the codebase.

Recommendation: integrate with OS keychain APIs (macOS Keychain, libsecret on Linux, DPAPI/Credential Manager on Windows). Until then, switch to a proper KDF + AES-GCM with a key derived from a user passphrase.

### 5.4 Medium — Shell-style argument concatenation in KdeConfig

`mucommander-os-linux/.../kde/KdeConfig.java:49`:

```java
Process process = Runtime.getRuntime().exec(CONFIG_COMMAND + " --key " + key);
```

`Runtime.exec(String)` tokenizes on whitespace, so a `key` containing `; rm -rf $HOME` would split into separate args (no shell-eval), but it would still be passed as a positional arg to `kreadconfig` — limited classic command-injection. However, all current call sites pass hardcoded keys, so practical impact is **low**. Should still be migrated to `ProcessBuilder(Array)` to remove the smell.

### 5.5 Medium — XML parsers without XXE hardening

The codebase has **9** SAX entry points constructed via `SAXParserFactory.newInstance().newSAXParser().parse(...)` without setting `disallow-doctype-decl`, `external-general-entities=false`, or `FEATURE_SECURE_PROCESSING`. Files:

```
AssociationReader.java, CommandReader.java, XmlConfigurationReader.java,
ActionKeymapReader.java, ToolBarReader.java, CommandBarReader.java,
BookmarkParser.java, CredentialsParser.java, ThemeReader.java
```

`ThemeReader` is the **only** one that disables external DTD loading (`load-external-dtd`, `load-dtd-grammar`). Most of these parse local files in `~/.mucommander/` (theme, bookmarks, credentials, action keymap, toolbar, command bar, association, configuration) — primarily *trust-the-user's-own-config* sources, so XXE risk is low **in the default case**. It becomes a real risk if any of these files are imported from an untrusted source (e.g. shared theme XMLs distributed online; user-pasted command/association XML). Hardening is a one-line cleanup per file:

```java
factory.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);
factory.setFeature("http://apache.org/xml/features/disallow-doctype-decl", true);
```

`mucommander-commons-conf/.../XmlConfigurationWriter.java` similarly creates a `SAXTransformerFactory` without `FEATURE_SECURE_PROCESSING`.

### 5.6 Low — Other observations

- `mucommander-core/src/main/java/com/mucommander/auth/CredentialsManager.java:77` — credentials path constants and lifecycle look reasonable; the issue is the cipher (5.3), not the management.
- No `ObjectInputStream.readObject` / `XMLDecoder` deserialization callers found in `src/main/java`. The codebase does not appear to expose a Java-serialization sink.
- No SQL / JNDI / LDAP injection sinks observed (no JDBC code in core).
- Archive extraction code: muCommander's own `TarArchiver`/`ZipArchiver`/`ArArchiveEntryIterator`/etc. write archives but the read path delegates to commons-compress (TAR), `java.util.zip` (ZIP), sevenzipjbinding (7z/RPM/ISO), and junrar (RAR). The known Zip-Slip path is the **junrar** one (§4.1).
- The codebase **does not** appear to ship its own SSRF surface (no embedded HTTP server, no admin port).

## 6. Summary of "Critical & High" you should fix first

In rough priority order:

1. **Remove the global TLS bypass** in `HTTPProtocolProvider` (item 5.1). This is the only finding in the codebase that I'd label *Critical*.
2. **Replace XORCipher** with OS keychain integration or AES-GCM/PBKDF2 (item 5.3).
3. **Bump junrar to ≥ 7.5.10** (CVE-2026-28208, CVE-2026-41245).
4. **Bump commons-vfs2 to ≥ 2.10.0** (CVE-2025-27553) — and modernize away from a 2018 release regardless.
5. **Bump hadoop-client to ≥ 3.4.2** (CVE-2025-27821) if the HDFS protocol is supported in releases.
6. **Replace or contain the bundled `mail.osgi-1.4.jar`** — or migrate off `jets3t 0.9.7` to AWS SDK v2 entirely.
7. **Harden the SAX parsers** in §5.5 with `FEATURE_SECURE_PROCESSING` / `disallow-doctype-decl`.
8. **Pin a single FlatLaf and a single JNA version** across all modules.
9. **Adopt commit signing and a `SECURITY.md`** (no security-disclosure path is documented; the CODEOWNERS file routes everything to one person).

For Linux distro maintainers: only the **Arch AUR** package is current. Every other distro that ships muCommander is exposing its users to one or more of items 1–8 above (and a long tail of fixes that landed across 1.0–1.6).
