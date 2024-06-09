/**
 * Copyright (c) 2018-2024 Contributors to the XPages Jakarta EE Support Project
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

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

import jakarta.inject.Inject;
import jakarta.data.page.PageRequest;
import jakarta.validation.constraints.NotEmpty;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.NotFoundException;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import model.CalendarEntry;

@Path("calendar")
public class CalendarExample {
	
	@Inject
	private CalendarEntry.Repository repository;
	
	@Path("readEntries")
	@GET
	@Produces("text/calendar")
	public String get(
		@QueryParam("start") @NotEmpty String startParam,
		@QueryParam("end") @NotEmpty String endParam,
		@QueryParam("page") int page,
		@QueryParam("pageSize") int pageSize
	) {
		TemporalAccessor start = parseTime(startParam);
		TemporalAccessor end = parseTime(endParam);
		
		PageRequest pagination = null;
		if(pageSize > 0) {
			pagination = PageRequest.ofPage(Math.max(page, 0), pageSize, false);
		}
		return repository.readCalendarRange(start, end, pagination);
	}
	
	@POST
	@Consumes("text/calendar")
	public String create(String icalData) {
		return repository.createCalendarEntry(icalData, false);
	}
	
	@Path("{uid}")
	@GET
	@Produces("text/calendar")
	public String get(@PathParam("uid") String uid) {
		return repository.readCalendarEntry(uid)
			.orElseThrow(() -> new NotFoundException("No calendar entry found for " + uid));
	}
	
	@Path("{uid}")
	@PUT
	@Consumes("text/calendar")
	public void update(@PathParam("uid") String uid, String icalData) {
		repository.updateCalendarEntry(uid, icalData, null, false, false, null);
	}
	
	@Path("{uid}")
	@DELETE
	public void delete(@PathParam("uid") String uid) {
		repository.removeCalendarEntry(uid, null, null);
	}
	
	private TemporalAccessor parseTime(String time) {
		try {
			return DateTimeFormatter.ISO_LOCAL_DATE.parse(time);
		} catch(DateTimeParseException e) {
		}
		try {
			return DateTimeFormatter.ISO_LOCAL_TIME.parse(time);
		} catch(DateTimeParseException e) {
		}
		try {
			return DateTimeFormatter.ISO_OFFSET_DATE_TIME.parse(time);
		} catch(DateTimeParseException e) {
		}
		try {
			return DateTimeFormatter.ISO_INSTANT.parse(time);
		} catch(DateTimeParseException e) {
		}
		try {
			return DateTimeFormatter.ISO_ZONED_DATE_TIME.parse(time);
		} catch(DateTimeParseException e) {
		}
		throw new IllegalArgumentException(MessageFormat.format("Unable to parse date/time value: {0}", time));
	}
}
