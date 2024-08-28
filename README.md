# RPX: Rekordbox Playlist Exporter

**RPX** is a Java-based tool designed to create folders containing audio files based on a playlist exported from
Rekordbox, along with additional functionalities.

## Download

The executable version of the software can be
downloaded [here](https://drive.google.com/file/d/1uzTAcDAFcc-TBNREAa6dBSeSgZHN3HeF/view?usp=sharing).

## System Requirements

- **Java Development Kit (JDK) 17** is required. It is recommended to use
  the [Temurin distribution](https://adoptium.net/temurin/releases/?version=17).

## Usage Instructions

To generate playlist folders from a `.txt` playlist file:

1. In Rekordbox, right-click on the desired playlist and select *"Export as txt file"*.
2. Run the RPX tool and select the "SELECT ALL TXT PLAYLIST FILES TO EXPORT" option.
3. Choose one or more `.txt` files from the file selection dialog.
4. For each playlist, the software offers the option to preserve the track order:
    - If enabled, the file names are prefixed with the track's position in the playlist.
    - If disabled, the files are copied in alphabetical order.
5. A folder containing the tracks is generated in the user's home directory for each playlist.

## Support

For technical assistance, contact via email: marco.belligoli98@gmail.com.