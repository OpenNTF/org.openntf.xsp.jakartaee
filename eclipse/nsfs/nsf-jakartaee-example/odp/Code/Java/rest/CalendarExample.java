package rest;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.temporal.TemporalAccessor;

import jakarta.inject.Inject;
import jakarta.data.repository.Pageable;
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
		
		Pageable pagination = null;
		if(pageSize > 0) {
			pagination = Pageable.ofPage(Math.max(page, 0)).size(pageSize);
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
