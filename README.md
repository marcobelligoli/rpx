# RPX: Rekordbox Playlist Exporter

**RPX** is a Java-based tool designed to create folders containing audio files based on a playlist exported from
Rekordbox, along with additional functionalities.

## Download

The executable version of the software can be downloaded from
the [project releases](https://github.com/marcobelligoli/rpx/releases) when available.
Each tagged build produces downloadable artifacts for Windows, macOS, Linux, and a runnable JAR.

## System Requirements

- **Java Development Kit (JDK) 17 or newer** is required for development and for running the JAR directly.
- The recommended JDK distribution is [Temurin](https://adoptium.net/temurin/releases/?version=17).

## Usage Instructions

To generate playlist folders from a `.txt` playlist file:

1. In Rekordbox, right-click on the desired playlist and select *"Export as txt file"*.
2. Run the RPX tool and select the "SELECT ALL TXT PLAYLIST FILES TO EXPORT" option.
3. Choose one or more `.txt` files from the file selection dialog.
4. For each playlist, the software offers the option to preserve the track order:
    - If enabled, the file names are prefixed with the track's position in the playlist.
    - If disabled, the files are copied in alphabetical order.
5. A folder containing the tracks is generated in the user's home directory for each playlist.

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

Pushing a tag triggers the release packaging workflow. The workflow first runs build and tests, then creates
downloadable artifacts for each supported platform using `jpackage`:

- Windows: ZIP containing the RPX app image with `RPX.exe`
- macOS: ZIP containing `RPX.app`
- Linux: TAR.GZ containing the RPX app image and launcher
- Runnable JAR: `rpx-jar-with-dependencies.jar`

Example tag flow:

```sh
git tag v0.2.0
git push origin v0.2.0
```

After the workflow completes, the artifacts are available from the GitHub Actions run and can be attached manually to a
GitHub Release.

## Support

For technical assistance, contact via email: marco.belligoli98@gmail.com.