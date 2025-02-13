# Dynamic Analysis

## Setup

```bash
# download system image 
$ sdkmanager "system-images;android-33;google_apis;arm64-v8a"

# install appium and driver
$ npm i --location=global appium
$ appium driver install uiautomator2

# create emulator
$ avdmanager create avd \
    -n "Pixel_6_0" \
    -d "pixel_6" \
    -k "system-images;android-33;google_apis;arm64-v8a"
```

## Run

```bash
# start emulator (new terminal window)
$ emulator -avd Pixel_6_0 \
    -partition-size 8192 \
    -wipe-data

# start Appium server (new terminal window)
$ appium

# run the dynamic analyzer
$ java -cp ./build/libs/AgeScope-1.0-SNAPSHOT.jar DynamicAnalyzer.Main \
    -i sample/apk_index.txt \
    -v 13 \
    -o sample/result \
    -u emulator-5554 # adb devices -l
```