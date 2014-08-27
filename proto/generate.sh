#!/bin/bash
DIR="$( cd "$( dirname "${BASH_SOURCE[0]}" )" && pwd )"
/opt/local/bin/protoc -I=$DIR --cpp_out=$DIR/generated/cpp --python_out=$DIR/generated/python --java_out=$DIR/../submission-server/src/generated $DIR/shenanigans.proto
