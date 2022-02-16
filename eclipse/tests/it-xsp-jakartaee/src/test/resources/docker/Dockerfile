#
# Copyright © 2018-2022 Jesse Gallagher
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

FROM --platform=linux/amd64 hclcom/domino:latest

ENV LANG "en_US.UTF-8"
ENV SetupAutoConfigure "1"
ENV SetupAutoConfigureParams "/local/runner/domino-config.json"
ENV DOMINO_DOCKER_STDOUT "yes"

EXPOSE 80