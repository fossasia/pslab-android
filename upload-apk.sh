#!/bin/sh
set -e

export PUBLISH_BRANCH=${PUBLISH_BRANCH:-master}

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
    cp app-release-unsigned.apk app-release-unaligned.apk
	jarsigner -verbose -tsa http://timestamp.comodoca.com/rfc3161 -sigalg SHA1withRSA -digestalg SHA1 -keystore ../scripts/key.jks -storepass $STORE_PASS -keypass $KEY_PASS app-release-unaligned.apk $ALIAS
	${ANDROID_HOME}/build-tools/27.0.3/zipalign -v -p 4 app-release-unaligned.apk app-release.apk
fi

git checkout --orphan workaround
git add -A

#commit

git commit -am "Travis build pushed [skip ci]"

git branch -D apk
git branch -m apk

#push to the branch apk
git push origin apk --force --quiet> /dev/null

if [ "$TRAVIS_PULL_REQUEST" == "false" ]
then 
curl https://$APPETIZE_API_TOKEN@api.appetize.io/v1/apps/4eqye6ea422e5np0gp2jfpemgm -H 'Content-Type: application/json' -d '{"url":"https://github.com/fossasia/pslab-android/blob/apk/app-debug.apk", "note": "PSLab Android App Update"}' 
fi

# Publish App to Play Store
if [ "$TRAVIS_BRANCH" != "$PUBLISH_BRANCH" ]; then
    echo "We publish apk only for changes in master branch. So, let's skip this shall we ? :)"
    exit 0
fi

gem install fastlane
fastlane supply --apk app-release.apk --track alpha --json_key ../scripts/fastlane.json --package_name $PACKAGE_NAME