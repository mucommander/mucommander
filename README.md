# barebones-commander

[![License](http://img.shields.io/badge/License-GPLv3-blue.svg)](https://www.gnu.org/licenses/gpl-3.0.html)

`barebones-commander` is a small, security-first dual-pane file manager focused on
**SFTP/SSH** as the only remote protocol, on **Linux** and **macOS**.

It is a **fork of [muCommander](https://github.com/mucommander/mucommander)**, with most
upstream features removed in favor of a smaller, easier-to-audit codebase.

## Status

Early — v0.1.0 is in active development. See [`PLAN.md`](PLAN.md) on the
[`docs/initial-audit-and-fork-plan`](https://github.com/e6qu/barebones-commander/tree/docs/initial-audit-and-fork-plan)
branch for the phased roadmap.

## Scope

| Kept | Removed |
|---|---|
| Local file system | FTP, HTTP/HTTPS, SMB, S3, Dropbox, Google Drive, OneDrive, GCS, NFS, oVirt, vSphere, Hadoop, ADB, Windows Registry, Bonjour |
| **SFTP / SSH** | All cloud and enterprise protocols |
| Linux + macOS OS adapters | Windows, OpenVMS, macOS-Java-8 |
| Basic archive formats: zip, tar, gzip, bzip2, xz | RAR, 7z, ISO, RPM, ar, cpio, lst, libguestfs |
| Text viewer | Image viewer, PDF viewer, hex (binary) viewer |
| Mouse-driven dual-pane GUI, drag & drop, keyboard bindings | Embedded terminal widget (use a real terminal app for SSH command sessions) |

See [`LIBRARIES.md`](LIBRARIES.md) for the upstream architecture and library inventory,
and [`SECURITY_REVIEW.md`](SECURITY_REVIEW.md) for the audit that motivated this fork.

## Build

Requires JDK 25+ (LTS). Once OSGi is removed in Phase 2 of the plan, the application
will run as a single fat JAR.

```sh
./gradlew run        # run from sources (currently still uses upstream OSGi runtime)
./gradlew tgz        # produce a Linux tarball
./gradlew dmg        # produce a macOS DMG  (-PskipDmgSign for unsigned)
```

## License

`barebones-commander` is released under the **GNU General Public License v3** (GPLv3),
preserving the original muCommander license. See [`LICENSE`](LICENSE) and
[`NOTICE`](NOTICE).

## Trademark

`muCommander` is the brand of the upstream project (https://www.mucommander.com,
copyright the original authors). This fork does **not** claim affiliation with the
upstream muCommander project. The fork has been renamed and re-iconified to avoid
brand confusion. All upstream copyright headers in source files are preserved.
