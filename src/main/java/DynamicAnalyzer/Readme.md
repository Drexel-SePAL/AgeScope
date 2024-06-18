# Appium

## Android Emulator

```bash
$ avdmanager create avd -n "Pixel_6_0" -d "pixel_6" -k "system-images;android-33;google_apis;arm64-v8a"
$ emulator -avd Pixel_6_0 -partition-size 8192 -wipe-data
$ java -cp ./AgeScopeAppuim/build/libs/AgeScope-1.0-SNAPSHOT.jar DynamicAnalyzer.Main -i batch/google_play_nsfw_apks/google_play_nsfw_apks_index_slice_00 -v 13 -o dresult -n Pixel_6_0
```