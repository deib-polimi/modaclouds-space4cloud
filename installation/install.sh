#!/bin/sh

CORES=2
MEMORY=2048
DISK=10000

if [ "$(uname)" == "Darwin" ]; then
    # Mac OS X
    docker-machine kill s4c
    docker-machine rm s4c
    docker-machine create --driver virtualbox --virtualbox-cpu-count $CORES --virtualbox-memory $MEMORY --virtualbox-disk-size $DISK s4c
    eval "$(docker-machine env s4c)"
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    # Windows
    echo "Windows not supported yet."
    exit -1
fi

bash build.sh line 1
bash build.sh cmpl 1
bash build.sh db 1
