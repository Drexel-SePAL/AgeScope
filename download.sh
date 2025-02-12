#!/bin/zsh

set -ve

curl -o temp/sample_apks.tar.gz https://s3.us-east-1.amazonaws.com/temp-file-sharing.fanfanishere.org/sample_apks.tar.gz
tar -xvzf temp/sample_apks.tar.gz -C sample/apk
