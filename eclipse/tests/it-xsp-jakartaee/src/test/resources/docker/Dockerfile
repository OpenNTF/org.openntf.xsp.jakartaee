#
# Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
#
# Licensed under the Apache License, Version 2.0 (the "License");
# you may not use this file except in compliance with the License.
# You may obtain a copy of the License at
#
#     http://www.apache.org/licenses/LICENSE-2.0
#
# Unless required by applicable law or agreed to in writing, software
# distributed under the License is distributed on an "AS IS" BASIS,
# WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
# See the License for the specific language governing permissions and
# limitations under the License.
#

ARG BASEIMAGE=hclcom/domino:14.0
FROM ${BASEIMAGE}

COPY --from=ibm-semeru-runtimes:open-21-jdk /opt/java/openjdk/bin/jcmd /opt/hcl/domino/notes/latest/linux/jvm/bin

RUN mkdir -p /local/runner \
	&& mkdir -p /local/eclipse/eclipse/plugins

COPY --chown=notes:notes staging/domino-config.json /local/runner/
COPY --chown=notes:notes staging/jacoco.jar /local/
COPY --chown=notes:notes staging/JavaOptionsFile.txt /local/
COPY --chown=notes:notes staging/container.link /opt/hcl/domino/notes/latest/linux/osgi/rcp/eclipse/links/container.link
COPY --chown=notes:notes staging/.java.policy /home/notes/.java.policy
COPY --chown=notes:notes staging/postgresql.jar /opt/hcl/domino/notes/latest/linux/ndext

COPY --chown=notes:notes staging/ntf/* /local/runner/
COPY --chown=notes:notes staging/plugins/* /local/eclipse/eclipse/plugins/