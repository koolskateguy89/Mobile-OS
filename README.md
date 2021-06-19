# Mobile-OS

This is basically replicating a mobile OS, think of it as a homeless man's Android, but on desktop and doesn't have many features

## Applications

- Applications are stored as folders, in which the application has free rein apart from...
- Applications return a `Pane` (450x524) which is displayed. (See [Example_App_Pane.fxml](Example_App_Pane.fxml))


### Application Development

(Use [Maven])

Pre-requisites to develop:
1. Run `mvn install` on the mobile-os project
2. Add `com.github.koolskateguy89.mobileos:mobileos` as a dependency to your application's pom with `provided` scope
```xml
<dependency>
	<groupId>com.github.koolskateguy89.mobileos</groupId>
	<artifactId>mobileos</artifactId>
	<version>${latest.version}</version>
	<scope>provided</scope>
</dependency>
```

The main class needs to extend [`App`](src/main/java/com/github/koolskateguy89/mobileos/app/App.java), and needs a
`public` constructor with parameters `Path, Properties` or `Path, Properties, Preferences`. If both are defined, the one with the
latter parameters (incl. `java.util.Preferences`) is used.

### Folder structure

| Name | Description |
| ---- | ----------- |
| A | B |
| ./info.properties | Properties file containing application details (see [info.properties](#infoproperties-very-important) below) |

The rest is however you want.

#### info.properties (very important)

| Key | Description | Example |
| --- | ----------- | ------- |
| name | The app's name | ExampleApp |
| version | The app's version | 1.0 |
| appType | The app's `AppType` | UTILITY |
| backgroundColor | (Optional - default white) The background color for your icon (HTML or CSS) | `#FFFFFF` or `red` |
| mainClassName | The canonical name of the class that extends `App` | com.github.koolskateguy89.example.MyApp |
| jarPath | The relative path to the application jar (relative from the application directory) | app.jar |
| A | B |

You can define more if you want, a `java.util.Properties` representing this file is passed to the App
in its constructor.

The rest of the folder is _almost_ entirely how you want it (see [Restrictions](#restrictions) below)

#### Restrictions

Basically don't have the same canonical names as anything already here

TODO: this

## Default Applications

- [Browser](src/main/java/com/github/koolskateguy89/mobileos/app/system/browser/Browser.java)
- [Text Editor](src/main/java/com/github/koolskateguy89/mobileos/app/system/texteditor/TextEditor.java)
- [AppInstaller](src/main/java/com/github/koolskateguy89/mobileos/app/system/installer/Installer.java)
- [Notes](src/main/java/com/github/koolskateguy89/mobileos/app/system/notes/Notes.java)  

- [Settings](src/main/java/com/github/koolskateguy89/mobileos/app/system/settings/SettingsApp.java)
- [Explorer](src/main/java/com/github/koolskateguy89/mobileos/app/system/explorer/Explorer.java)
- TODO
