# GitHub REST Client

This project is from part of the [August 2022 OpenNTF webinar](https://frostillic.us/blog/posts/2022/8/18/xpages-jakarta-ee-in-practice-slides-and-video).

It demonstrates using the MicroProfile Rest Client to access the GitHub Issues API, and then showing the results in an XPage using the enhanced Expression Language support.

The Java code consists of two class files:

- `restclient.GitHubIssues` is an interface that defines a MicroProfile Rest Client to access the GitHub API, which in turn uses JAX-RS annotations to define endpoints and parameters.
- `bean.IssuesBean` is an application-scoped CDI bean that acts as an intermediary between the XPage UI and the Rest Client, accepting a GitHub owner and repo name and pulling in the issues when they're not blank.

The important UI code is in the `gitHubIssues.xsp` XPage, which includes a small `xe:formTable` to request the repo owner and name, followed by an `xp:dataTable` that shows the results from a call to `IssuesBean`. This call uses newer EL support to pass method parameters to the bean's `get` method.

This example requires no dependencies other than the XPages Jakarta EE Support project. It was written to target version 2.8.0.

### API Limits

This project demonstrates using the GitHub API in an unauthenticated way, which is subject to [greater rate limits](https://docs.github.com/en/rest/overview/resources-in-the-rest-api#rate-limiting) than authenticated or registered-app calls. This is fine for demonstration purposes, but a full application with moderate or heavy use should add tokens to API calls.