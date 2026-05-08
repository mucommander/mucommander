# Security Policy

## Scope

`barebones-commander` is a small file manager. The project's main security
properties to preserve are:

- **TLS hygiene** — the application must never silently weaken transport
  security (e.g. `HttpsURLConnection.setDefaultSSLSocketFactory` to a permissive
  manager). The upstream HTTP bundle's blanket TLS bypass is removed in this
  fork.
- **Stored credential safety** — SFTP passwords / keys must not be persisted in
  a recoverable plaintext or with hard-coded "encryption". OS keychain
  integration is the target; anything weaker requires an explicit user opt-in.
- **Archive / file-input safety** — extracting an archive or opening a file
  must never let attacker-controlled metadata write outside the intended
  destination directory (Zip-Slip class).

## Supported versions

Pre-`v1.0`. Until `v1.0`, only the latest tagged release is supported. Once
`v1.0` ships, the policy will move to "latest two minor releases."

## Reporting a vulnerability

Until a dedicated channel is set up, report vulnerabilities **privately** by
opening a GitHub Security Advisory at
<https://github.com/e6qu/barebones-commander/security/advisories/new>.

Please **do not** open a public issue or pull request describing an exploit.

What to include:

- Affected version (commit SHA or tag).
- A clear, minimal proof-of-concept or reproduction steps.
- Your assessment of impact (confidentiality / integrity / availability).
- Whether the issue is reachable in the default install (or only with non-default config).
- Suggested fix, if you have one.

## Response targets

- **Acknowledgement**: within 5 business days of report.
- **Triaged severity**: within 14 days.
- **Fix in main branch** (for High / Critical): within 30 days when feasible;
  longer for issues that require a refactor or a third-party dependency
  upstream change.
- **Public advisory**: published once a fix is released.

## Out of scope

- Issues in dependencies whose fix has not been released by the upstream
  maintainer (we will track and bump as soon as a patched version is
  available).
- Issues that require local code execution as the same user already running
  the application — the threat model assumes the user trusts the OS account
  the app runs under.
- Issues in upstream `muCommander` that have not been ported into this fork.

## Hall of fame

Credit for valid reports will be added to release notes (and, with the
reporter's permission, the README).
