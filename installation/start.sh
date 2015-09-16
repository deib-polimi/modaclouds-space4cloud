#!/bin/sh

bash stop.sh $@

IMAGE="$1"
VERSION="$2"
shift
shift
PARAMS="$@"

if [ "$(uname)" == "Darwin" ]; then
    # Mac OS X
    eval "$(docker-machine env s4c)"
    docker-machine start s4c
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    # Windows
    echo "Windows not supported yet."
    exit -1
fi

docker run -d $PARAMS --name=$IMAGE$VERSION $IMAGE$VERSION
