# RPX: Rekordbox Playlist Exporter

**RPX** is a Java-based tool designed to create folders containing audio files based on a playlist exported from
Rekordbox, along with additional functionalities.

## Download

Ready-to-use builds are published on the [project releases](https://github.com/marcobelligoli/rpx/releases):

| File                                 | Platform            | Notes                                                |
|--------------------------------------|---------------------|------------------------------------------------------|
| `RPX-<tag>-windows-x64.exe`          | Windows             | Installer, per-user (no administrator rights needed) |
| `RPX-<tag>-windows-x64-portable.zip` | Windows             | Portable app, unzip and run `RPX.exe`                |
| `RPX-<tag>-macos-arm64.dmg`          | macOS Apple Silicon | Installer, drag RPX into Applications                |
| `RPX-<tag>-macos-x64.dmg`            | macOS Intel         | Installer, drag RPX into Applications                |
| `RPX-<tag>-linux-x64.tar.gz`         | Linux               | Portable app image                                   |
| `RPX-<tag>.jar`                      | Any                 | Runnable JAR, requires Java 17+                      |

Every platform bundle ships its own Java runtime, so no separate Java installation is needed. Only the JAR
requires Java to be installed.

### Unsigned builds

The installers are not code-signed, so the operating system warns the first time RPX is launched:

- **Windows**: SmartScreen shows *"Windows protected your PC"* → click *More info* → *Run anyway*.
- **macOS**: *"RPX cannot be opened because the developer cannot be verified"* → open RPX from the Applications
  folder with right click → *Open*, or run `xattr -dr com.apple.quarantine /Applications/RPX.app`.

## System Requirements

- **Java Development Kit (JDK) 17 or newer** is required for development and for running the JAR directly.
- The recommended JDK distribution is [Temurin](https://adoptium.net/temurin/releases/?version=17).

## Usage Instructions

To generate playlist folders from a `.txt` playlist file:

1. In Rekordbox, right-click on the desired playlist and select *"Export as txt file"*.
2. Run the RPX tool and select the "SELECT ALL TXT PLAYLIST FILES TO EXPORT" option.
3. Choose one or more `.txt` files from the file selection dialog. The *BACK* button returns to the initial screen,
   so a wrong selection can be discarded and made again.
4. For each playlist, the software offers the option to preserve the track order:
    - If enabled, the file names are prefixed with the track's position in the playlist.
    - If disabled, the files are copied in alphabetical order.
5. A folder containing the tracks is generated on the Desktop for each playlist (in the home directory when no
   Desktop folder exists). The behaviour is the same on Windows, macOS and Linux.

## Development

Build and run the test suite locally with Maven:

```sh
mvn clean verify
```

Create the runnable JAR with dependencies:

```sh
mvn clean package
```

The packaged JAR is generated at:

```text
target/rpx-jar-with-dependencies.jar
```

It can be launched with:

```sh
java -jar target/rpx-jar-with-dependencies.jar
```

## Continuous Integration

GitHub Actions runs the build and tests automatically on:

- pushes to `develop`
- pushes to `main`
- pull requests targeting `develop` or `main`
- tags
- manual workflow dispatches

The CI uses JDK 17 and runs:

```sh
mvn -B clean verify
```

## Release Artifacts

Pushing a tag runs the whole pipeline: build and tests, then a packaging matrix that produces a native bundle per
platform with `jpackage`, and finally a job that attaches every artifact to the GitHub Release of that tag.

```sh
git tag 1.0.0
git push origin 1.0.0
```

When the workflow finishes:

- if no release exists yet for the tag, a **draft** release is created with all the assets already attached — review
  it on GitHub, edit the notes and press *Publish release*;
- if the release already exists, the assets are uploaded to it (with `--clobber`, so re-running the workflow
  refreshes them).

### Packaging notes

- The Windows installer is produced by `jpackage --type exe`, which requires the WiX Toolset 3 (`candle.exe` /
  `light.exe`; WiX 4+ is not supported by jpackage). The workflow uses the copy shipped with the runner image and
  falls back to installing it via Chocolatey.
- Icons live in `packaging/`: `RPX.ico` is used on Windows, `RPX.png` on Linux, and the macOS `RPX.icns` is generated
  on the runner from the PNG with `sips` and `iconutil`.
- macOS Intel bundles are built on the `macos-15-intel` runner, the last Intel image GitHub Actions offers
  (available until August 2027); Apple Silicon bundles are built on `macos-latest`.
- Tags must start with a numeric version of at least `1.0.0` (an optional `v` prefix is stripped): `jpackage`
  rejects a bundle version whose major number is zero, because of Apple's `CFBundleVersion` rule. The project
  version in `pom.xml` follows the same line.
- Building the installers locally is possible with the same commands used by
  `.github/workflows/build-release.yml`; only `jpackage` from JDK 17+ and, on Windows, WiX 3 are needed.

## License

RPX is released under the [MIT License](LICENSE).

The published artifacts bundle third-party components that keep their own licenses (Apache Commons, juniversalchardet,
and the Eclipse Temurin runtime embedded in the native installers): see [THIRD-PARTY-NOTICES.md](THIRD-PARTY-NOTICES.md).

## Support

For technical assistance, contact via email: marco.belligoli98@gmail.com.