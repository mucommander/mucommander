# muCommander — Architecture & Libraries

Inventory generated from a clone of `mucommander/mucommander` at HEAD `8662144` (2026-04-30).

## At a glance

| | |
|---|---|
| Type | Cross-platform desktop file manager (dual-pane, Total-Commander-style) |
| Primary language | **Java 11** (compiled with `--release 11`, source ~212k LOC across 1,352 files) |
| Other JVM languages | Kotlin (no first-party Kotlin code; `kotlin-stdlib` is pulled in for transitive deps and `kotlin-reflect` is shipped as an OSGi bundle) |
| Build system | **Gradle** (multi-module, OSGi manifests via `biz.aQute.bnd`) |
| Runtime model | **OSGi (Apache Felix 7.0.5)** — every protocol/format/viewer is its own bundle |
| UI toolkit | **Swing** + **FlatLaf** look-and-feel + **VAqua** on macOS |
| Min JDK to build | 11 (CI runs 17/18) |
| License | GPL |
| Top maintainer | Arik Hadas (`@ahadas`, `* @ahadas` in CODEOWNERS) |

## Module / architecture layout

The repo is one root Gradle project with ~70 subprojects. They group cleanly into layers:

### Core
- `mucommander-core` — main UI, jobs, search, bookmarks, auth, snapshots, OSGi container glue
- `mucommander-core-preload` — early bootstrap bundle
- `mucommander-commons-{file,io,collections,conf,runtime,util}` — generic file abstractions, streams, XML config, OS-runtime detection
- `mucommander-preferences`, `mucommander-translator`, `mucommander-encoding`, `mucommander-process`, `mucommander-command`, `mucommander-bonjour`

### Protocol bundles (`mucommander-protocol-*`)
`adb`, `dropbox`, `ftp`, `gcs`, `gdrive`, `hadoop`, `http`, `nfs`, `onedrive`, `ovirt`, `registry`, `s3`, `sftp`, `smb`, `vsphere`, plus the `protocol-api` SPI.

### Archive-format bundles (`mucommander-format-*`)
`ar`, `bzip2`, `cpio`, `gzip`, `iso`, `libguestfs` (work-in-progress), `lst`, `rar`, `rpm`, `sevenzip`, `tar`, `xz`, `zip`, plus `mucommander-archiver` (writes archives).

### Viewers (`mucommander-viewer-*`)
`api`, `binary`, `image`, `pdf`, `text` (separate Swing-based viewers).

### OS adapters (`mucommander-os-*`)
`api`, `linux`, `macos`, `macos-java8` (legacy), `openvms`, `win`.

### Vendored helpers
`apache-bzip2`, `gson`, `jetbrains-jediterm`, `kotlin-reflect`, `sevenzipjbindings`, `sun-net-www` — re-bundled or wrapped third-party code (mostly to make non-OSGi jars OSGi-friendly).

## Main libraries

### Core / UI
| Library | Version | Purpose |
|---|---|---|
| Apache Felix `org.apache.felix.main` | 7.0.5 | OSGi container at runtime |
| FlatLaf (`com.formdev:flatlaf`) | 2.6 (and 2.2 in pdf viewer) | Modern Swing look-and-feel |
| VAqua (`org.violetlib:vaqua`) | 10 | Native-style L&F on macOS |
| JediTerm (`org.jetbrains.jediterm:jediterm-{core,ui}`) | 3.57 | Embedded terminal widget |
| pty4j (`org.jetbrains.pty4j`) | 0.13.11 | PTY for the embedded terminal |
| MBassador (`net.engio:mbassador`) | 1.3.0 | In-process event bus |
| JCommander (`com.beust`) | 1.82 | CLI argument parsing |
| ICU4J (`com.ibm.icu`) | 78.3 | Collation / locale-aware sorting |
| JNA (`net.java.dev.jna`) | 5.5.0 + 5.12.1 | Native interop (mac/win OS adapters) |
| SLF4J + Logback | 1.7.36 / 1.2.13 | Logging |
| Log4j (`log4j-core`, `log4j-1.2-api`) | 2.25.3 | Logging compat (transitive) |
| Gson | 2.11.0 | JSON (cloud SDK helpers) |
| SnakeYAML | 2.3 | Custom-command/config YAML parsing |
| dd-plist (`com.googlecode.plist`) | 1.23 | Apple plist parsing |
| Bouncy Castle (`bcprov-jdk18on`) | 1.79 | Crypto (transitive via SMB/cloud SDKs) |

### Protocols
| Library | Version | Used by |
|---|---|---|
| jsch — mwiede fork (`com.github.mwiede:jsch`) | 0.2.10 | SFTP |
| commons-net | 3.8.0 | FTP |
| jcifs-ng (`eu.agno3.jcifs`) | 2.1.10 | SMB (legacy path) |
| smbj (`com.hierynomus`) | 0.13.0 | SMB (modern path) |
| jets3t | 0.9.7 | S3 (very old; abandoned upstream) |
| Apache Hadoop client | 3.4.1 | HDFS |
| Avro | 1.11.4 | Hadoop transitive |
| Dropbox SDK | 7.0.0 | Dropbox |
| Google API client + OAuth client | 2.0.0 / 1.34.1 | Google Drive |
| google-api-services-drive | v3-rev20220815-2.0.0 | Google Drive |
| Microsoft Graph SDK | 5.67.0 | OneDrive |
| Azure Identity / Azure XML | 1.9.2 / 1.0.0-beta.2 | OneDrive auth |
| oVirt engine SDK | 4.4.5 | oVirt |
| jaxws-api / javax.xml.soap | 2.2.12 / 10.0-b28 | vSphere SOAP |
| `vim25.jar` (bundled) | n/a | vSphere VMware client |
| `jadb-v1.2.1.jar` (bundled) | 1.2.1 | Android (ADB) |
| jmDNS | 3.5.5 | Bonjour discovery |
| jaxrpc-api / glassfish soap | 1.1 / 10.0-b28 | S3 transitive |

### Archive formats
| Library | Version | Format |
|---|---|---|
| commons-compress | 1.28.0 | tar / cpio / ar / many |
| commons-vfs2 | **2.3** (very old) | RAR streaming wrapper |
| junrar (`com.github.junrar`) | 7.5.5 | RAR extraction |
| sevenzipjbinding | 16.02-2.01 | 7z, ISO, RPM |
| xz (`org.tukaani`) | 1.9 | xz / lzma |
| `mail.osgi-1.4.jar` (bundled) | 1.4 | s3 transitive (very old JavaMail) |

### Viewers
| Library | Version | Purpose |
|---|---|---|
| icepdf-viewer (`com.github.pcorless.icepdf`) | 7.3.1 | PDF viewer |
| TwelveMonkeys imageio (`common-io`, `common-lang`, `imageio-core`/`-jpeg`/`-metadata`/`-psd`/`-tiff`/`-webp`) | 3.12.0 | Extra image-format support |

### Build / packaging
- Gradle plugins: `com.athaydes.osgi-run` 1.6.0, `org.ajoberstar.grgit` 5.0.0, `edu.sc.seis.launch4j` 2.5.4, `biz.aQute.bnd:7.1.0`, `gradle-macappbundle` (vendored), `apple-actions/import-codesign-certs` (CI), Java `jpackage` (DMG/MSI/RPM/DEB).

### CI / tests
- GitHub Actions: `nightly.yml`, `stable.yml`, `tests.yaml` (matrix on ubuntu-latest + macos-15)
- TestNG 7.11.0 across most modules; JUnit 5 (5.14.1 BOM) in onedrive and vsphere
- Apple notarization via `xcrun notarytool` in nightly/stable workflows
- Static analysis: Coverity Scan badge in README (project 3642)

## Things to know about this codebase

- **OSGi-first**. Every protocol or format is a bundle, wired by Felix at startup. Bundles ship in `bundle/`, the OSGi cache lives in `felix-cache/`. Bundling instructions in the root `build.gradle` (`wrapInstructions { manifest("...") { ... } }`) rewrite imports of non-OSGi-friendly transitive deps (okhttp, smbj, msal4j, microsoft-graph-core, azure-core, kotlin-stdlib, etc.) so they can run in Felix.
- **Vendored sub-trees**. `apache-bzip2`, `gson`, `kotlin-reflect`, `sevenzipjbindings`, `sun-net-www`, `jetbrains-jediterm` are checked-in source trees to wrap or shade dependencies. Each has its own `build.gradle`.
- **Loose-jar dependencies** sit under `*/libs/*.jar` (e.g. `mucommander-protocol-vsphere/libs/vim25.jar`, `mucommander-protocol-adb/libs/jadb-v1.2.1.jar`, `mucommander-protocol-s3/libs/mail.osgi-1.4.jar`). These have no version metadata and are not checked by typical SCA tools.
- **Private "release" repo**. The CI checks out `mucommander/release` (private) which carries patches that inject Google Drive / Dropbox / OneDrive client credentials at build time — meaningful only for distributed builds, not source builds.
- **Original author** Maxence Bernard last committed in 2013; the project transitioned to Arik Hadas, who is now the sole CODEOWNER.
