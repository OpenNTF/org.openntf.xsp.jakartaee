# Configure the runtime image
FROM --platform=linux/amd64 hclcom/domino:12.0.1IF1

COPY --chown=notes java.policy /home/notes/.java.policy
COPY --chown=notes domino-config.json /tmp/
COPY --chown=notes data.zip /tmp/
COPY --chown=notes notesdata/* /local/notesdatatemp/

ENV LANG "en_US.UTF-8"
ENV CustomNotesdataZip "/tmp/data.zip"
ENV SetupAutoConfigure "1"
ENV SetupAutoConfigureParams "/tmp/domino-config.json"
ENV DOMINO_DOCKER_STDOUT "yes"

EXPOSE 80
