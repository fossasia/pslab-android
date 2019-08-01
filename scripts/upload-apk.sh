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
    rm -rf pslab-master*
else
    rm -rf pslab-dev*
fi

find ../app/build/outputs -type f -name '*.apk' -exec cp -v {} . \;
find ../app/build/outputs -type f -name '*.aab' -exec cp -v {} . \;

for file in app*; do
    if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
        if [[ ${file} =~ ".aab" ]]; then
            mv $file pslab-master-${file}
        else
            mv $file pslab-master-${file:4}
        fi
    elif [ "$TRAVIS_BRANCH" == "$DEVELOPMENT_BRANCH" ]; then
        if [[ ${file} =~ ".aab" ]]; then
                mv $file pslab-dev-${file}
        else
                mv $file pslab-dev-${file:4}
        fi
    fi
done

git checkout --orphan temporary
# Push generated apk files to apk branch
git add .
git commit -am "Travis build pushed to [$TRAVIS_BRANCH]"

# Delete current apk branch
git branch -D apk
# Rename current branch to apk
git branch -m apk

git push origin apk --force --quiet > /dev/null

# Publish App to Play Store
if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
    ls
    gem install fastlane
    fastlane supply --aab pslab-master-app.aab --track alpha --json_key ../scripts/fastlane.json --package_name $PACKAGE_NAME
fi
