# AgeScope

## Build Requirements

- Java Development Kit (JDK) 21: https://www.oracle.com/java/technologies/downloads/#java21
- Gradle: https://gradle.org/install/
- Android SDK Command-Line Tools (not necessary to have full Android
  Studio): https://developer.android.com/studio#command-tools
- Environment Variable
  ```bash
  $ nano ~/.bashrc
  # add line above to the EOF (change when the actual location of sdk is different)
  # export ANDROID_HOME="/opt/android-sdk"
  $ source ~/.bashrc
  
  # Microsoft Windows: Please add environment variable by yourself
  ```
- Android SDK: `$ sdkmanager "build-tools;33.0.2" "platform-tools" "platforms;android-33"`

## Execute

Create Ramdisk:

```bash
# Linux (permanently, will exist when reboot)
$ sudo mkdir -p /media/ramdisk
$ sudo chown -R user:user /media/ramdisk
$ nano /etc/fstab
    # add line above to the EOF
    # tmpfs /media/ramdisk tmpfs nodev,nosuid,noexec,nodiratime,size=8192M 0 0
$ sudo mount -a

# macOS (temporary, require re-execution after reboot/unmount)
$ erasevolume HFS+ 'ramdisk' `hdiutil attach -nobrowse -nomount ram://16777216`

# Microsoft Windows: please create your own Ramdisk and remember to change **ramdiskLocation** variable in 
# StaticUIAnalyzer.Main
```

StaticUIAnalyzer (Replace `<>` with your own values):

```bash
$ cd <project_dir>
$ gradle build
$ java -cp ./build/libs/AgeScope-1.0-SNAPSHOT.jar StaticUIAnalyzer.Main -i <index_file> -o <result_dir> -p ${ANDROID_HOME}/platforms/
```

### Sample Index File

Contains path of each apk file, separated by linebreak:

```
./3C70295C1177034E9D7C76285304941068471E9E31528D34FEF43962BF2D6993.apk
```