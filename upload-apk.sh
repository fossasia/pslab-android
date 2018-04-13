#!/usr/bin/env bash
#create a new directory that will contain out generated apk
mkdir $HOME/buildApk/ 
#copy generated apk from build folder and README.md to the folder just created
cp -R app/build/outputs/apk/debug/app-debug.apk app/build/outputs/apk/release/app-release-unsigned.apk $HOME/buildApk/
cp -R app/build/outputs/apk/debug/output.json $HOME/buildApk/debug_output.json
cp -R app/build/outputs/apk/release/output.json $HOME/buildApk/release_output.json
cp -R README.md $HOME/buildApk/

#setup git
cd $HOME
git config --global user.email "noreply@travis.com"
git config --global user.name "Travis CI" 
#clone the repository in the buildApk folder
git clone --quiet --branch=apk https://fossasia:$GITHUB_API_KEY@github.com/fossasia/pslab-android apk > /dev/null

cd apk
rm -rf *
cp -Rf $HOME/buildApk/*  ./

git checkout --orphan workaround
git add -A

#add files
#git add -f .
#commit and skip the tests

git commit -am "Travis build pushed [skip ci]"

git branch -D apk
git branch -m apk

#push to the branch apk
git push origin apk --force --quiet> /dev/null

if [ "$TRAVIS_PULL_REQUEST" == "false" ]
then 
curl https://$APPETIZE_API_TOKEN@api.appetize.io/v1/apps/4eqye6ea422e5np0gp2jfpemgm -H 'Content-Type: application/json' -d '{"url":"https://github.com/fossasia/pslab-android/blob/apk/app-debug.apk", "note": "PSLab Android App Update"}' 
fi
