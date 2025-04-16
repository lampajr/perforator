#!/usr/bin/bash

CWD="$(dirname "$0")"

rm /tmp/result.json
cp "${CWD}/result.json" /tmp/result.json

eval "$QDUP_CMD"
