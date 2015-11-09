# CMPL

Docker container for Coliop/Coin Mathematical Programming Language (CMPL).

## Disclaimer of warranty

Licensor provides the Work (and each Contributor provides its Contributions) on an "**AS IS**" *BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND*, either express or implied, including, without limitation, any warranties or conditions of *TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE*. You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.

More details on the disclaimer at [Apache 2.0]. Following more details on the components and licenses.

### Components and licenses

The components in this docker container are:
* CMPL, license: [GPL 3.0]

Other components used in the container:
* OpenSSH

[BSD New]: https://opensource.org/licenses/BSD-3-Clause
[Apache 2.0]: https://www.apache.org/licenses/LICENSE-2.0
[EPL 1.0]: https://www.eclipse.org/org/documents/epl-v10.php
[GPL 3.0]: https://www.gnu.org/licenses/gpl-3.0.en.html

## How to run it

To start it:

```sh
docker run --name <name> -p <port>:22 deibpolimi/cmpl
```

then just open a bash session with:

```sh
docker exec -it <name> bash
```

or connect via ssh, either using the password `modaclouds` for the root use, or else using the key:

```sh
ssh root@<ip> -o StrictHostKeyChecking=no -p <port> -i id_rsa
```

where the id_rsa key can be downloaded from https://github.com/deib-polimi/modaclouds-space4cloud/tree/master/installation/cmpl.
