#!/bin/sh

bash stop.sh $@

IMAGE="$1"
VERSION="$2"
shift
shift
PARAMS="$@"

cd $IMAGE

if [ "$(uname)" == "Darwin" ]; then
    # Mac OS X
    docker-machine start s4c
    eval "$(docker-machine env s4c)"
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    # Windows
    echo "Windows not supported yet."
    exit -1
fi

docker run -d $PARAMS --name=$IMAGE$VERSION $IMAGE$VERSION
