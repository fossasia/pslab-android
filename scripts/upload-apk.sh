#!/bin/sh
set -e

export PUBLISH_BRANCH=${PUBLISH_BRANCH:-master}
export DEVELOPMENT_BRANCH=${DEVELOPMENT_BRANCH:-development}

# #setup git
git config --global user.email "noreply@travis.com"
git config --global user.name "Travis CI" 

# Generate Playstore bundle
./gradlew bundlePlaystoreRelease

echo "------- app/build/outputs"
ls app/build/outputs
echo "------- app/build/outputs/apk"
ls app/build/outputs/apk
echo "------- app/build/outputs/apk/fdroid"
ls app/build/outputs/apk/fdroid
echo "------- app/build/outputs/apk/playstore"
ls app/build/outputs/apk/playstore


# #clone the repository
git clone --quiet --branch=apk https://fossasia:$GITHUB_API_KEY@github.com/fossasia/pslab-android apk > /dev/null
cd apk
echo "------- apk"
ls

if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
    ls
    rm -rf pslab-master*
else
    ls
    rm -rf pslab-dev*
fi

find ../app/build/outputs -type f \( -name "*.aab" -o -name "*.apk" \) -exec cp -v {} . \;
    
if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then

    echo "Push to master branch detected, generating apk..."
    ls
    # Retain apk files for testing
    mv app-playstore-debug.apk pslab-master-playstore-debug.apk
    mv app-fdroid-debug.apk pslab-master-fdroid-debug.apk
    # Generate temporary apk for signing
    mv app-playstore-release.apk pslab-master-playstore-release.apk
    mv app-fdroid-release.apk pslab-master-fdroid-release.apk
    mv app.aab pslab-master-app-bundle.aab
fi

if [ "$TRAVIS_BRANCH" == "$DEVELOPMENT_BRANCH" ]; then

    echo "Push to development branch detected, generating apk..."
    ls
    # Rename apks with dev prefixes
    mv app-playstore-debug.apk pslab-dev-playstore-debug.apk
    mv app-fdroid-debug.apk pslab-dev-fdroid-debug.apk

    mv app-playstore-release.apk pslab-dev-playstore-release.apk
    mv app-fdroid-release.apk pslab-dev-fdroid-release.apk
    mv app.aab pslab-dev-app-bundle.aab
fi

git checkout --orphan temporary
# Push generated apk files to apk branch
# Debug printing TODO: Remove
echo "Content in apk directory"
ls
echo "Content in one level up"
ls ..
git add .
git commit -am "Travis build pushed to [$TRAVIS_BRANCH]"

# Delete current apk branch
git branch -D apk
# Rename current branch to apk
git branch -m apk

git push origin apk --force --quiet > /dev/null

# Publish App to Play Store
if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
    gem install fastlane
    fastlane supply --aab pslab-master-app-bundle.aab --track alpha --json_key ../scripts/fastlane.json --package_name $PACKAGE_NAME
fi
