#!/bin/sh
set -e

export PUBLISH_BRANCH=${PUBLISH_BRANCH:-master}
export DEVELOPMENT_BRANCH=${DEVELOPMENT_BRANCH:-development}

#setup git
git config --global user.email "noreply@travis.com"
git config --global user.name "Travis CI" 

#clone the repository
git clone --quiet --branch=apk https://fossasia:$GITHUB_API_KEY@github.com/fossasia/pslab-android apk > /dev/null

cd apk

\cp -r ../app/build/outputs/apk/*/**.apk .
\cp -r ../app/build/outputs/apk/debug/output.json debug-output.json
\cp -r ../app/build/outputs/apk/release/output.json release-output.json
\cp -r ../README.md .

# Signing Apps

if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
    echo "Push to master branch detected, signing the app..."
    # Retain apk files for testing
    mv app-debug.apk app-master-debug.apk
    # Generate temporary apk for signing
    cp app-release-unsigned.apk app-release-unaligned.apk
    # Sign APK
    jarsigner -tsa http://timestamp.comodoca.com/rfc3161 -sigalg SHA1withRSA -digestalg SHA1 -keystore ../scripts/key.jks -storepass $STORE_PASS -keypass $KEY_PASS app-release-unaligned.apk $ALIAS
    # Remove previous release-apk file
    \rm -f app-release.apk
    # Generate new release-apk file
    ${ANDROID_HOME}/build-tools/27.0.3/zipalign -v -p 4 app-release-unaligned.apk app-release.apk
    # Rename unsigned release apk to master
    rm -f app-release-unaligned.apk
    mv app-release-unsigned.apk app-master-release.apk
    # Push generated apk files to apk branch
    git checkout apk
    git add -A
    git commit -am "Travis build pushed [master]"
    git push origin apk --force --quiet> /dev/null
fi

if [ "$TRAVIS_BRANCH" == "$DEVELOPMENT_BRANCH" ]; then
    echo "Push to development branch detected, generating apk..."
    # Rename apks with dev prefixes
    mv app-debug.apk app-dev-debug.apk
    mv app-release-unsigned.apk app-dev-release.apk
    # Push generated apk files to apk branch
    git checkout apk
    git add -A
    git commit -am "Travis build pushed [development]"
    git push origin apk --force --quiet> /dev/null
fi

# Publish App to Play Store
if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
    gem install fastlane
    fastlane supply --apk app-release.apk --track alpha --json_key ../scripts/fastlane.json --package_name $PACKAGE_NAME
fi

