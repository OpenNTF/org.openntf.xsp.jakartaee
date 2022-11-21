package restclient;

import java.util.Date;
import java.util.List;

import org.eclipse.microprofile.rest.client.inject.RegisterRestClient;

import jakarta.json.bind.annotation.JsonbProperty;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.core.MediaType;

@RegisterRestClient(baseUri="https://api.github.com")
@Path("repos/{owner}/{repo}/issues")
public interface GitHubIssues {
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	List<Issue> get(@PathParam("owner") String owner, @PathParam("repo") String repo);

	class Issue {
		private int id;
		private String url;
		private String title;
		private String state;
		@JsonbProperty("created_at")
		private Date created;
		private Milestone milestone;

		public int getId() {
			return id;
		}
		public void setId(int id) {
			this.id = id;
		}
		public String getUrl() {
			return url;
		}
		public void setUrl(String url) {
			this.url = url;
		}
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
		public String getState() {
			return state;
		}
		public void setState(String state) {
			this.state = state;
		}
		public Date getCreated() {
			return created;
		}
		public void setCreated(Date created) {
			this.created = created;
		}
		public Milestone getMilestone() {
			return milestone;
		}
		public void setMilestone(Milestone milestone) {
			this.milestone = milestone;
		}
	}
	
	class Milestone {
		private String title;
		
		public String getTitle() {
			return title;
		}
		public void setTitle(String title) {
			this.title = title;
		}
	}
}
