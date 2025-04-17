#!/usr/bin/bash

CWD="$(dirname "$0")"

rm /tmp/result.json
cp "${CWD}/result.json" /tmp/result.json
cp "${CWD}/perf-stat.txt" /tmp/perf-stat.txt

eval "$QDUP_CMD"
