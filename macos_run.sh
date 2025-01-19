#!/bin/zsh

set -ve

RAMDISK=/Volumes/ramdisk/
PROJ_DIR=$(pwd)
SAMPLE_DIR=${PROJ_DIR}/sample
INDEX_FILE=${SAMPLE_DIR}/apk_index.txt

# create ramdisk if not exist
if [ ! -d "${RAMDISK}" ]; then
    diskutil erasevolume HFS+ 'ramdisk' `hdiutil attach -nobrowse -nomount ram://16777216`
fi

# build
gradle build

# generate sample index file
cd "${SAMPLE_DIR}"
find ~+ -type f -name "*.apk" > "${INDEX_FILE}"

# execute
cd "${PROJ_DIR}"
java -cp ./build/libs/AgeScope-1.0-SNAPSHOT.jar StaticUIAnalyzer.Main \
-i "${INDEX_FILE}" \
-o "${SAMPLE_DIR}"/result \
-p "${ANDROID_HOME}"/platforms/
