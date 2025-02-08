#!/bin/sh

set -ve

find /sample -type f -name "*.apk" > /sample/apk_index.txt
java -cp app.jar StaticUIAnalyzer.Main -i /sample/apk_index.txt -o /sample/result -p /opt/android-sdk/platforms
