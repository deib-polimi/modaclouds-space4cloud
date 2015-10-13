# Space 4Clouds Dependencies

Docker container for all the dependencies for Space 4Clouds.

## Disclaimer of warranty

Licensor provides the Work (and each Contributor provides its Contributions) on an "**AS IS**" *BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND*, either express or implied, including, without limitation, any warranties or conditions of *TITLE, NON-INFRINGEMENT, MERCHANTABILITY, or FITNESS FOR A PARTICULAR PURPOSE*. You are solely responsible for determining the appropriateness of using or redistributing the Work and assume any risks associated with Your exercise of permissions under this License.

More details on the disclaimer at [Apache 2.0]. Following more details on the components and licenses.

### Components and licenses

The components developed directly by the [MODAClouds group](http://www.modaclouds.eu) are the following:
* Line Solver, license: [BSD New]

Main components the tools above depends on, not developed by us and released as is:
* Matlab Compiler Runtime R2015a
* MySQL Server, license: [GPL 3.0]
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
docker run --name <name> \
-p <port1>:3306 \
-p <port2>:5463 \
-p <port3>:22 \
deibpolimi/space4clouds-dependencies
```

you'll have a SSH server that you can use for CMPL. All the passwords are `modaclouds`.
