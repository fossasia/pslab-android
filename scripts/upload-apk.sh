#!/bin/sh
set -e

export PUBLISH_BRANCH=${PUBLISH_BRANCH:-master}
export DEVELOPMENT_BRANCH=${DEVELOPMENT_BRANCH:-development}

# #setup git
git config --global user.email "noreply@travis.com"
git config --global user.name "Travis CI" 

# #clone the repository
git clone --quiet --branch=apk https://fossasia:$GITHUB_API_KEY@github.com/fossasia/pslab-android apk > /dev/null
cd apk

if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
    rm -rf *
else 
    rm -rf pslab-dev*
fi

# Signing Apps

if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
    ./gradlew bundlePlaystoreRelease

    find ../app/build/outputs -type f \( -name "*.aab" -o -name ".apk" \) -exec cp {} . \;

    echo "Push to master branch detected, signing the app..."
    # Retain apk files for testing
    mv app-playstore-debug.apk pslab-master-debug.apk
    mv app-fdroid-debug.apk pslab-master-debug-fdroid.apk
    # Generate temporary apk for signing
    mv app-playstore-release.apk pslab-master-release.apk
    mv app-fdroid-release.apk pslab-master-release-fdroid.apk
    mv app.aab pslab-master.aab

    git checkout apk
    git add -A
    git commit -am "Travis build pushed [master]"
    git push origin apk --force --quiet> /dev/null
fi

if [ "$TRAVIS_BRANCH" == "$DEVELOPMENT_BRANCH" ]; then
    find ../app/build/outputs -type f -name "*.apk" -exec cp {} . \;

    echo "Push to development branch detected, generating apk..."
    # Rename apks with dev prefixes
    mv app-playstore-debug.apk pslab-dev-debug.apk
    mv app-fdroid-debug.apk pslab-dev-debug-fdroid.apk

    mv app-playstore-release.apk pslab-dev-release.apk
    mv app-fdroid-release.apk pslab-dev-release-fdroid.apk
    # Push generated apk files to apk branch
    git checkout apk
    git add -A
    git commit -am "Travis build pushed [development]"
    git push origin apk --force --quiet> /dev/null
fi

# Publish App to Play Store
if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
    gem install fastlane
    fastlane supply --aab pslab-master.aab --track alpha --json_key ../scripts/fastlane.json --package_name $PACKAGE_NAME
fi

