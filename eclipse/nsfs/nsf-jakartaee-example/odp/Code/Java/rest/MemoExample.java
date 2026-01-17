/**
 * Copyright (c) 2018-2026 Contributors to the XPages Jakarta EE Support Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package rest;

import java.util.List;

import jakarta.inject.Inject;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import model.Memo;

@Path("memo")
public class MemoExample {
	@Inject
	private Memo.Repository memoRepository;
	
	@Inject
	private Memo.MailfileRepository mailfileMemoRepository;
	
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Memo> findBySubject(@QueryParam("subject") @NotEmpty String subject) {
		return memoRepository.findBySubject(subject).toList();
	}
	
	@Path("mailfile")
	@GET
	@Produces(MediaType.APPLICATION_JSON)
	public List<Memo> findBySubjectInMailfile(@QueryParam("subject") @NotEmpty String subject) {
		return mailfileMemoRepository.findBySubject(subject).toList();
	}

	@POST
	@Consumes(MediaType.APPLICATION_JSON)
	@Produces(MediaType.APPLICATION_JSON)
	public Memo send(Memo memo, @QueryParam("save") boolean save) {
		return memoRepository.send(memo, false, false, save);
	}
}
