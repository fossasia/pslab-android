#!/bin/sh
set -e

export PUBLISH_BRANCH=${PUBLISH_BRANCH:-master}
export DEVELOPMENT_BRANCH=${DEVELOPMENT_BRANCH:-development}

# #setup git
git config --global user.email "noreply@travis.com"
git config --global user.name "Travis CI" 

# Generate Playstore bundle
./gradlew bundlePlaystoreRelease
    
# #clone the repository
git clone --quiet --branch=apk https://fossasia:$GITHUB_API_KEY@github.com/fossasia/pslab-android apk > /dev/null
cd apk

if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
    rm -rf *
else 
    rm -rf pslab-dev*
fi

find ../app/build/outputs -type f \( -name "*.aab" -o -name ".apk" \) -exec cp {} . \;
    
if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then

    echo "Push to master branch detected, signing the app..."
    # Retain apk files for testing
    mv app-playstore-debug.apk pslab-master-debug.apk
    mv app-fdroid-debug.apk pslab-master-debug-fdroid.apk
    # Generate temporary apk for signing
    mv app-playstore-release.apk pslab-master-release.apk
    mv app-fdroid-release.apk pslab-master-release-fdroid.apk
    mv app.aab pslab-master-app.aab
fi

if [ "$TRAVIS_BRANCH" == "$DEVELOPMENT_BRANCH" ]; then

    echo "Push to development branch detected, generating apk..."
    # Rename apks with dev prefixes
    mv app-playstore-debug.apk pslab-dev-debug.apk
    mv app-fdroid-debug.apk pslab-dev-debug-fdroid.apk

    mv app-playstore-release.apk pslab-dev-release.apk
    mv app-fdroid-release.apk pslab-dev-release-fdroid.apk
    mv app.aab pslab-dev-app.aab
fi

git checkout --orphan temporary
# Push generated apk files to apk branch
git add .
git commit -am "Travis build pushed [$TRAVIS_BRANCH]"

# Delete current apk branch
git branch -D apk
# Rename current branch to apk
git branch -m apk

git push origin apk --force --quiet > /dev/null

# Publish App to Play Store
if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
    gem install fastlane
    fastlane supply --aab pslab-master-app.aab --track alpha --json_key ../scripts/fastlane.json --package_name $PACKAGE_NAME
fi
