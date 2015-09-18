#!/bin/sh

if [ "$(uname)" == "Darwin" ]; then
    # Mac OS X
    if [ "$(which brew)" == "brew not found" ]; then
        echo "You need to do it by yourself, considering that homebrew isn't installed."
        exit -1
    else
        brew update
        brew install docker
        brew install docker-machine
        brew cask install virtualbox
    fi
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    # Windows
    echo "Windows not supported yet."
    exit -1
elif [ "$(uname)" == "Linux" && "$(which docker)" == "docker not found" ]; then
    # Linux
    sudo apt-get update
    sudo apt-get install curl
    curl -sSL https://get.docker.com/ | sh
    sudo usermod -aG docker `whoami`
    sudo newgrp docker
fi
