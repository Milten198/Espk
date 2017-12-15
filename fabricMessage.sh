#!/usr/bin/env bash

rm -f fabricMessage.txt
GIT_RESULT=$(git log -1 --pretty=%B)
echo -e "$GIT_RESULT"
echo -e "$GIT_RESULT" > fabricMessage.txt
