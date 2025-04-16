#!/usr/bin/bash

CWD="$(dirname "$0")"

if [ "$#" -eq 1 ]; then
  ADDITIONAL_ARGS="$1"
else
  ADDITIONAL_ARGS=""
fi

QDUP_CMD="jbang qDup@hyperfoil $ADDITIONAL_ARGS ${CWD}/sut.yaml ${CWD}/hyperfoil.yaml ${CWD}/profiling.yaml ${CWD}/util.yaml ${CWD}/qdup.yaml"

echo Executing: "$QDUP_CMD"

eval "$QDUP_CMD"
