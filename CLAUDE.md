# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project

RPX (Rekordbox Playlist Exporter) is a single-module Java 17 Swing desktop app. It reads playlist files exported
from Rekordbox ("Export as txt file") and copies the referenced audio files into a per-playlist output folder.

## Commands

```sh
mvn clean verify                  # build + run all tests (what CI runs, as `mvn -B clean verify`)
mvn clean package                 # produces target/rpx.jar and target/rpx-jar-with-dependencies.jar
java -jar target/rpx-jar-with-dependencies.jar   # run the GUI
mvn test -Dtest=OsUtilsTest                       # single test class
mvn test -Dtest=OsUtilsTest#testGetDesktopPath    # single test method
```

Lombok is a `provided` dependency; annotation processing must be enabled in the IDE.
There is no Maven wrapper — a local `mvn` and JDK 17+ are required.

## Architecture

**Entry point:** `RPX.main` applies the system look and feel and shows `RPXGUI`, both on the Event Dispatch
Thread. All application logic hangs off the export service; there is no CLI mode.

**Export pipeline (template method).** `ExportService` (interface) → `AbstractExportService` (owns the whole
orchestration: resolve output folder, copy files, verify count) → `ExportServiceTxtImpl` (only supplies
`getRekordboxSongs`, the format-specific parsing). Adding a new playlist format means: subclass
`AbstractExportService`, implement `getRekordboxSongs`, and add a case to the `switch (inputFormat)` in
`RPXGUI.getExportButton` — that switch currently has only a `default` branch and is the intended extension point
(an Italian comment there marks it, e.g. for M3U8). `RPXGUI.reloadPanel`/`getExportButton` already thread an
`inputFormat` string through for this purpose.

**Output location.** `exportPlaylists(list, destinationFolderPath)` creates one folder per playlist under the
given path; the one-argument overload delegates to it with `OsUtils.getDesktopPath()`, which is what the GUI
starts from and what keeps the older call sites working. `getDesktopPath()` returns the Desktop on every
platform, falling back to the home directory when there is no Desktop folder. It has to branch per OS:
`FileSystemView.getHomeDirectory()` returns the Desktop on Windows (and follows a relocated one, e.g. OneDrive),
but the plain home directory on macOS and Linux, where the Desktop is resolved from `user.home` instead.
`AbstractExportServiceTest` mocks the method statically rather than touching the real Desktop — keep that mock
even in tests that pass an explicit destination, so a regression cannot write into the developer's own Desktop.

**Post-copy verification.** `AbstractExportService.checkSongsNumber` compares the number of parsed playlist rows
against the number of files with an audio extension (`AUDIO_FORMATS`) present in the output folder, and throws
`RPXException` on mismatch. Because it counts everything in the folder, a re-export into an existing folder or
stray audio files there will change the result. Missing source files are logged and skipped, so they surface as
this mismatch rather than as an immediate error.

**Encoding handling (txt format).** Rekordbox exports are typically UTF-16LE. `ExportServiceTxtImpl` calls
`FileUtils.changeFileEncoding` first, which **rewrites the user's playlist file in place as UTF-8** — a
deliberate but destructive side effect on the input file. Encoding is detected with juniversalchardet and mapped
through `FileUtils.toCharset`, which falls back to UTF-8 for an undetected or unsupported charset; the platform
default charset is deliberately never used, since it differs between Windows and macOS/Linux on JDK 17 and would
decode the same playlist differently. Parsing then strips embedded NUL characters, splits on
tabs by **fixed column index (0–13)**, and passes title/artist/path through `fixDoubleUTF8Encoding`, which detects
the `0x83 0xC2` byte pair and re-decodes ISO-8859-1 → UTF-8. Changes to column order or to encoding assumptions
break parsing silently or with `IndexOutOfBounds`/`NumberFormatException` wrapped in `RPXException`.

**Track ordering.** When "Keep tracks order" is checked, files are copied as `<trackNumber> - <original name>`;
`RekordboxSong.setTrackNumber` zero-pads to three digits so lexical sort matches playlist order.

**Errors.** Everything thrown out of `exportPlaylists` is wrapped in `RPXException` (a `RuntimeException`) and
surfaced by `RPXGUI` as a `JOptionPane` dialog. Logging goes through `LogUtils` over `java.util.logging`.

## Testing

JUnit 5 + Mockito (`mockito-core` / `mockito-junit-jupiter`). Tests lean on `mockStatic` for `OsUtils` and
`FileSystemView`, and use temp directories for filesystem work. `OsUtils` exposes a `SystemPropertyProvider` seam
(Lombok `@Setter` on the static field) so OS detection can be driven in tests — reset it after use.
`AbstractExportServiceTest` defines a local `TestExportService` subclass to exercise the abstract base.

## CI / Release

`.github/workflows/build-release.yml` has three jobs. `build` runs `mvn -B clean verify` on JDK 17 (Temurin) for
pushes to `develop`/`main`, PRs into them, tags, and manual dispatch. On **tags only**, `package` fans out over
`windows-latest`, `macos-latest` (arm64), `macos-15-intel` (x64) and `ubuntu-latest`, and `release` attaches
everything to the GitHub Release.

Each `package` leg builds `--type app-image` once and then derives the distributable from it
(`--type exe --app-image …` on Windows, `--type dmg --app-image …` on macOS, `tar.gz` on Linux), so there is a
single jpackage app build per platform. Constraints worth knowing before editing this workflow:

- **Tags must be `>= 1.0.0`.** `jpackage` refuses a bundle version starting with `0` on macOS (Apple's
  `CFBundleVersion` rule), which is why the project moved to `1.0.0-SNAPSHOT`. The *Resolve bundle version* step
  strips an optional `v` prefix, keeps up to three numeric components as `APP_VERSION`, and fails the build if the
  tag has no numeric prefix. Artifact **file names** use the raw tag, not `APP_VERSION`.
- **WiX, not Inno Setup.** Since JDK 16 the Windows `--type exe` bundler is WiX-based: it needs `candle.exe` /
  `light.exe` from **WiX 3** (WiX 4+ is rejected). Without them jpackage fails with the misleading
  `Error: Invalid or unsupported type: [exe]`. The workflow reuses the runner's copy and falls back to
  `choco install wixtoolset` (that package id is the WiX 3 line), then appends the discovered bin directory to
  `GITHUB_PATH`.
- **Intel macOS.** `macos-13` was retired; `macos-15-intel` is the last x64 image and disappears in August 2027.
  jpackage cannot cross-build architectures, hence the two separate macOS legs.
- **Icons** are tracked in `packaging/` (`RPX.ico` for Windows, `RPX.png` for Linux, both copied from the old
  launch4j assets). The macOS `.icns` is generated on the runner from the PNG via `sips` + `iconutil`.
- **Release step** is idempotent: it creates a *draft* release with the assets when none exists for the tag, and
  otherwise uploads with `--clobber`. It is the only job with `contents: write`.

Bundles are unsigned and not notarized, so SmartScreen/Gatekeeper warn on first launch (documented in the README).

`launch4j/` and `dist/` are leftovers from the pre-`jpackage` Windows packaging and are gitignored; do not treat
them as part of the build — the icons they hold have been copied into the tracked `packaging/` directory. `.m2/`
in the repo root is a local repository cache and is also gitignored.

Default branch for work and PRs is `develop`.
