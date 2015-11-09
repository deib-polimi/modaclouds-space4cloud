#!/bin/sh

if [ "$(uname)" == "Darwin" ]; then
    # Mac OS X
    BREW_EXISTS=`which brew | grep "/"`
    if [ -z "$BREW_EXISTS" ]; then
        echo "You need to do it by yourself, considering that homebrew isn't installed."
        exit -1
    else
        DOCKER_EXISTS=`which docker | grep "/"`
        DOCKER_MACHINE_EXISTS=`which docker-machine | grep "/"`
        VIRTUALBOX_EXISTS=`which virtualbox | grep "/"`
        brew update
        brew upgrade
        if [ -z "$DOCKER_EXISTS" ]; then
            brew install docker
        fi
        if [ -z "$DOCKER_MACHINE_EXISTS" ]; then
            brew install docker-machine
        fi
        if [ -z "$VIRTUALBOX_EXISTS" ]; then
            brew tap Caskroom/cask
            brew cask install virtualbox
        fi
    fi
elif [ "$(expr substr $(uname -s) 1 10)" == "MINGW32_NT" ]; then
    # Windows
    echo "Windows not supported yet."
    exit -1
elif [ "$(uname)" == "Linux" && "$(which docker)" == "docker not found" ]; then
    # Linux
    sudo apt-get update
    sudo apt-get install -y curl
    curl -sSL https://get.docker.com/ | sh
    sudo usermod -aG docker `whoami`
    echo "Please logout and login again to use docker without sudo."
fi
