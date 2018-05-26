#!/bin/bash

openssl aes-256-cbc -K $encrypted_d4de000c59f7_key -iv $encrypted_d4de000c59f7_iv -in ./scripts/secrets.tar.enc -out ./scripts/secrets.tar -d
tar xvf ./scripts/secrets.tar -C scripts/

git config --global user.email "noreply@travis.com"
git config --global user.name "Travis CI" 

#clone the repository
git clone --quiet --branch=apk https://fossasia:$GITHUB_API_KEY@github.com/fossasia/pslab-android apk > /dev/null

cd apk

cp app-release-unsigned.apk app-release-unaligned.apk
jarsigner -verbose -tsa http://timestamp.comodoca.com/rfc3161 -sigalg SHA1withRSA -digestalg SHA1 -keystore ../scripts/key.jks -storepass $STORE_PASS -keypass $KEY_PASS app-release-unaligned.apk $ALIAS
${ANDROID_HOME}/build-tools/27.0.3/zipalign -v -p 4 app-release-unaligned.apk app-release.apk
