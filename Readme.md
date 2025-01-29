# AgeScope

## Background

AgeScope is a prototype of the Static/Dynamic analysis tool for Android applications to identify the existence of age
identify behaviors. Currently, we have collected ~100k packages which has been labeled as "age restricted/adult only"
from Google Play and other Android App Markets.

- Static Analysis is based on Soot, which will provide us the intermediate code from JVM bytecodes.
- Dynamic Analysis is based on Appium, it will be running on Android Emulator and capture/analysis text from UI to
  conduct age verification.

At this point, we have manually download few Android Application from Chinese Market and established a basic static
analysis for them. And we are expecting to expand our scope with more Apps.

## Requirements

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
- Install Android SDK: `$ sdkmanager "build-tools;33.0.2" "platform-tools" "platforms;android-33"`

## Docker

```
$ docker build -t agescope .
$ docker run -it --tmpfs /mnt/ramdisk:rw,size=4g -v $PWD/sample/:/sample agescope
```

## Quick Start in macOS

### Corresponding Path

- apk: ./sample/apk
- index: ./sample/apk_index.txt
- result: ./sample/result/apk_index_result.txt

### Run Everything

```bash
$ chmod +x ./macos_run.sh
$ ./macos_run.sh
```

## More Details

### Create Ramdisk

```bash
# Linux (permanently, will exist when reboot)
$ sudo mkdir -p /mnt/ramdisk
$ sudo chown -R user:user /mnt/ramdisk
$ nano /etc/fstab
    # add line above to the EOF
    # tmpfs /mnt/ramdisk tmpfs nodev,nosuid,noexec,nodiratime,size=8192M 0 0
$ sudo mount -a

# macOS (temporary, require re-execution after reboot/unmount)
$ diskutil erasevolume HFS+ 'ramdisk' `hdiutil attach -nobrowse -nomount ram://16777216`

# Microsoft Windows: please create your own Ramdisk and remember to change **ramdiskLocation** variable in 
# StaticUIAnalyzer.Main
```

### Build

```bash
$ gradle build
```

### Execute

```bash
$ java -cp ./build/libs/AgeScope-1.0-SNAPSHOT.jar StaticUIAnalyzer.Main \
-i <index_file> \
-o <result_dir> \
-p ${ANDROID_HOME}/platforms/
```

### Sample Index File

Contains path of each apk file, separated by linebreak (this is an example, it is not necessary to put everything into `sample` directory):

```
/Users/username/AgeScope/sample/apk/D063343811C7E160A4421FDFD0715D14A2EC9EE400777DD68F2E41CAAFF524EF.apk
```