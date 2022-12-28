FROM hclcom/domino:latest

RUN mkdir -p /local/runner \
	&& mkdir -p /local/eclipse/eclipse/plugins

COPY --chown=notes:notes staging/domino-config.json /local/runner/
COPY --chown=notes:notes staging/container.link /opt/hcl/domino/notes/latest/linux/osgi/rcp/eclipse/links/container.link
COPY --chown=notes:notes staging/.java.policy /home/notes/.java.policy

COPY --chown=notes:notes staging/ntf/* /local/runner/
COPY --chown=notes:notes staging/plugins/* /local/eclipse/eclipse/plugins/