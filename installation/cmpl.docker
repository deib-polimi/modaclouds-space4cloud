FROM ubuntu:14.04
MAINTAINER Riccardo B. Desantis <riccardobenito.desantis@polimi.it>

# Setting the environment
ENV USERHOME  /root
ENV DEBIAN_FRONTEND noninteractive

# Update the repos and install all the used packages
RUN apt-get update && apt-get install -y \
    libglib2.0-0 \
    openssh-server \
    curl

# Installing and setting the SSH server
RUN mkdir $USERHOME/.ssh
COPY id_rsa $USERHOME/.ssh/id_rsa
COPY id_rsa.pub $USERHOME/.ssh/id_rsa.pub
RUN cat $USERHOME/.ssh/id_rsa.pub > $USERHOME/.ssh/authorized_keys && \
    chmod go-rwx $USERHOME/.ssh/id_rsa $USERHOME/.ssh/id_rsa.pub && \
    mkdir /var/run/sshd

# Installing CMPL
WORKDIR /tmp
RUN curl -OL http://www.coliop.org/_download/Cmpl-1-10-0-linux64.tar.gz && \
    tar zxf Cmpl-1-10-0-linux64.tar.gz
WORKDIR Cmpl
RUN bash install
WORKDIR ..
RUN rm -rf Cmpl*
WORKDIR $USERHOME

# Starts the SSH server
EXPOSE 22
CMD /usr/sbin/sshd -D
