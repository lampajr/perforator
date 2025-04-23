#!/usr/bin/bash

CWD="$(dirname "$0")"

echo "Running test $1.."
rm "/tmp/$1/result.json" "/tmp/$1/baseline.json" "/tmp/$1/perf-stat.txt"
mkdir -p "/tmp/$1"

cp "${CWD}/result.json" "/tmp/$1/result.json"
cp "${CWD}/baseline.json" "/tmp/$1/baseline.json"
cp "${CWD}/perf-stat.txt" "/tmp/$1/perf-stat.txt"

eval "$QDUP_CMD"
