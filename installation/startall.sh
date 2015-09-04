#!/bin/sh

if [ "$(uname)" == "Darwin" ]; then
    # Mac OS X
    docker-machine start s4c
    echo "Please use this IP for LINE, CMPL and the MySQL DB: $(docker-machine ip s4c)"
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    # Windows
    echo "Windows not supported yet."
    exit -1
fi

bash start.sh line 1 -p 22022:22
bash start.sh cmpl 1 -p 22122:22
bash start-db.sh
