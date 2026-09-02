# Third-party notices

RPX is released under the MIT License (see [LICENSE](LICENSE)). The published artifacts also contain third-party
components, which keep their own licenses as listed below.

## Bundled in the runnable JAR and in every native bundle

| Component                                | Version | License                     |
|------------------------------------------|---------|-----------------------------|
| Apache Commons IO                        | 2.15.1  | Apache License 2.0          |
| Apache Commons Lang                      | 3.13.0  | Apache License 2.0          |
| juniversalchardet                        | 1.0.3   | Mozilla Public License 1.1  |

juniversalchardet is used unmodified. The MPL 1.1 requires the source of the covered files to stay available: the
original project is archived at <https://code.google.com/archive/p/juniversalchardet/>.

## Bundled in the native installers only

The Windows, macOS and Linux bundles embed an Eclipse Temurin 17 runtime, assembled by `jpackage`. OpenJDK is
licensed under the **GNU General Public License version 2 with the Classpath Exception**, which is what allows the
runtime to be shipped together with an application released under a different license.

## Build-time only, not redistributed

Lombok (MIT), JUnit 5 (Eclipse Public License 2.0), Mockito (MIT). These are used to compile and test the project
and are not part of any published artifact.

## Application icon

`packaging/RPX.ico` and `packaging/RPX.png` are not third-party assets: they were drawn by the author and are
covered by the project's own MIT License.
