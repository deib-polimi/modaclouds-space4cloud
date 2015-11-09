#!/bin/sh

bash stop.sh line 1
bash stop.sh cmpl 1
bash stop.sh db 1

if [ "$(uname)" == "Darwin" ]; then
    # Mac OS X
    docker-machine stop s4c
    docker-machine kill s4c
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    # Windows
    echo "Windows not supported yet."
    exit -1
fi
