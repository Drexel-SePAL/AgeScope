#!/bin/zsh

set -ve

PROJ_DIR=$(pwd)
SAMPLE_DIR=${PROJ_DIR}/sample
INDEX_FILE=${SAMPLE_DIR}/apk_index.txt

gradle build
cd "${SAMPLE_DIR}"
find ~+ -type f -name "*.apk" > "${INDEX_FILE}"
cd "${PROJ_DIR}"
java -cp ./build/libs/AgeScope-1.0-SNAPSHOT.jar StaticUIAnalyzer.Main \
-i "${INDEX_FILE}" \
-o "${SAMPLE_DIR}"/result \
-p "${ANDROID_HOME}"/platforms/
