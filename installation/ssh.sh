#!/bin/sh

IMAGE="$1"
VERSION="$2"

IP="localhost"

if [ "$(uname)" == "Darwin" ]; then
    # Mac OS X
    eval "$(docker-machine env s4c)"
    IP=`docker-machine ip s4c`
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    # Windows
    echo "Windows not supported yet."
    exit -1
fi

PORT=`docker port $IMAGE$VERSION 22 | awk '{ split($1, s, ":"); print s[2]; }'`

ssh -x root@$IP -o StrictHostKeyChecking=no -p $PORT -i $IMAGE/id_rsa
