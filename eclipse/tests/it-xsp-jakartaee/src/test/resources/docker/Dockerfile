#
# Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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

ARG BASEIMAGE=domino-container:V1202_11032022prod
FROM ${BASEIMAGE}

RUN mkdir -p /local/runner \
	&& mkdir -p /local/eclipse/eclipse/plugins

USER root
RUN curl -L https://github.com/ibmruntimes/semeru17-binaries/releases/download/jdk-17.0.4.1%2B1_openj9-0.33.1/ibm-semeru-open-jdk_x64_linux_17.0.4.1_1_openj9-0.33.1.tar.gz > /local/jdk.tgz \
    && cd /local \
    && tar xzf jdk.tgz \
    && mkdir back \
    && cp -r /opt/hcl/domino/notes/latest/linux/jvm/bin/classic back/classic \
    && cp /opt/hcl/domino/notes/latest/linux/jvm/conf/security/default_US_export.policy back \
    && cp /opt/hcl/domino/notes/latest/linux/jvm/conf/security/default_local.policy back \
    && cp /opt/hcl/domino/notes/latest/linux/jvm/conf/security/java.policy back \
    && cp /opt/hcl/domino/notes/latest/linux/jvm/conf/security/java.security back \
    && rm -r /opt/hcl/domino/notes/latest/linux/jvm \
    && mv jdk-17.0.4.1+1 /opt/hcl/domino/notes/latest/linux/jvm \
    && mv back/classic /opt/hcl/domino/notes/latest/linux/jvm/bin/classic \
    && mv back/* /opt/hcl/domino/notes/latest/linux/jvm/conf/security
USER notes

COPY --chown=notes:notes staging/domino-config.json /local/runner/
COPY --chown=notes:notes staging/JavaOptionsFile.txt /local/
COPY --chown=notes:notes staging/container.link /opt/hcl/domino/notes/latest/linux/osgi/rcp/eclipse/links/container.link
COPY --chown=notes:notes staging/.java.policy /home/notes/.java.policy
COPY --chown=notes:notes staging/postgresql.jar /opt/hcl/domino/notes/latest/linux/ndext

COPY --chown=notes:notes staging/ntf/* /local/runner/
COPY --chown=notes:notes staging/plugins/* /local/eclipse/eclipse/plugins/