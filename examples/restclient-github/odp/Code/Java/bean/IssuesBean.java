package bean;

import java.util.Collections;
import java.util.List;

import com.ibm.commons.util.StringUtil;

import jakarta.enterprise.context.ApplicationScoped;
import jakarta.inject.Inject;
import jakarta.inject.Named;
import restclient.GitHubIssues;

@ApplicationScoped
@Named
public class IssuesBean {
	
	@Inject
	private GitHubIssues client;
	
	public List<GitHubIssues.Issue> get(String owner, String repo) {
		if(StringUtil.isEmpty(owner) || StringUtil.isEmpty(repo)) {
			return Collections.emptyList();
		}
		return client.get(owner, repo);
	}
}
