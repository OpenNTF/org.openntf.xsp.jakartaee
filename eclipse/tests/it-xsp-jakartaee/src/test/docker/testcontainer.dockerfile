# Configure the runtime image
FROM --platform=linux/amd64 hclcom/domino:12.0.1

COPY --chown=notes java.policy /local/notes/.java.policy
COPY --chown=notes domino-config.json /tmp/
COPY --chown=notes data.zip /tmp/

ENV LANG "en_US.UTF-8"
ENV CustomNotesdataZip "/tmp/data.zip"
ENV SetupAutoConfigure "1"
ENV SetupAutoConfigureParams "/tmp/domino-config.json"

EXPOSE 80
