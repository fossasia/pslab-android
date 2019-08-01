#!/bin/sh
set -e

export DEPLOY_BRANCH=${DEPLOY_BRANCH:-master}

if [ "$TRAVIS_PULL_REQUEST" != "false" -o "$TRAVIS_REPO_SLUG" != "fossasia/pslab-android" -o "$TRAVIS_BRANCH" != "$DEPLOY_BRANCH" ]; then
    echo "We decrypt key only for pushes to the master branch and not PRs. So, skip."
    exit 0
fi

openssl aes-256-cbc -K $encrypted_d4de000c59f7_key -iv $encrypted_d4de000c59f7_iv -in ./scripts/secrets.tar.enc -out ./scripts/secrets.tar -d
tar xvf ./scripts/secrets.tar -C scripts/
