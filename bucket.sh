#!/usr/bin/env bash

BUCKET=$(sed '6q;d' $1 | awk '{ print $11 }' | sed -e 's/\[//g' | sed -e 's/\]//g' | awk -F/ '{ r=$6"/"$7 ; print r }')
echo "$BUCKET"
export BUCKET
