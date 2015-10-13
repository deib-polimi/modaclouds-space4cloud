FROM ubuntu:14.04
MAINTAINER Riccardo B. Desantis <riccardobenito.desantis@polimi.it>

# Setting the environment
ENV USERHOME  /root
ENV DEBIAN_FRONTEND noninteractive
ENV NOTVISIBLE "in users profile"

# Update the repos and install all the used packages
RUN apt-get update && apt-get install -y --no-install-recommends \
    libglib2.0-0 \
    openssh-server \
    curl && \
    apt-get autoclean && \
    apt-get autoremove && \
    rm -rf /var/lib/apt/lists/*

# Installing and setting the SSH server
WORKDIR $USERHOME
RUN mkdir .ssh
COPY id_rsa .ssh/id_rsa
COPY id_rsa.pub .ssh/id_rsa.pub
RUN cat .ssh/id_rsa.pub > .ssh/authorized_keys && \
    chmod go-rwx .ssh/id_rsa .ssh/id_rsa.pub && \
    mkdir /var/run/sshd && \
    echo 'root:modaclouds' | chpasswd && \
    sed -i 's/PermitRootLogin without-password/PermitRootLogin yes/' /etc/ssh/sshd_config && \
    sed 's@session\s*required\s*pam_loginuid.so@session optional pam_loginuid.so@g' -i /etc/pam.d/sshd && \
    echo "export VISIBLE=now" >> /etc/profile

# Installing CMPL
WORKDIR /tmp
RUN curl -OL http://www.coliop.org/_download/Cmpl-1-10-0-linux64.tar.gz
RUN tar zxf Cmpl-1-10-0-linux64.tar.gz
WORKDIR Cmpl
RUN bash install
WORKDIR ..
RUN rm -rf Cmpl*
WORKDIR $USERHOME

# Starts the SSH server
EXPOSE 22
CMD /usr/sbin/sshd -D
