/**
 * Copyright (c) 2018-2023 Contributors to the XPages Jakarta EE Support Project
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
package it.org.openntf.xsp.jakartaee.nsf.nosql;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.io.IOException;
import java.io.StringReader;
import java.net.URLEncoder;
import java.time.Instant;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.ChronoUnit;
import java.util.List;

import org.junit.jupiter.api.Test;

import com.ibm.commons.util.StringUtil;

import it.org.openntf.xsp.jakartaee.AbstractWebClientTest;
import it.org.openntf.xsp.jakartaee.TestDatabase;
import jakarta.ws.rs.client.Client;
import jakarta.ws.rs.client.Entity;
import jakarta.ws.rs.client.WebTarget;
import jakarta.ws.rs.core.Response;
import net.fortuna.ical4j.data.CalendarBuilder;
import net.fortuna.ical4j.data.ParserException;
import net.fortuna.ical4j.model.Calendar;
import net.fortuna.ical4j.model.DateTime;
import net.fortuna.ical4j.model.component.VEvent;
import net.fortuna.ical4j.model.property.Uid;

@SuppressWarnings("nls")
public class TestNoSQLCalendar extends AbstractWebClientTest {
	@Test
	public void testCalendarLifecycle() throws IOException, ParserException {
		Client client = getAdminClient();
		
		OffsetDateTime start1 = OffsetDateTime.now();
		OffsetDateTime start2 = start1.plus(3, ChronoUnit.HOURS).plus(1, ChronoUnit.DAYS);
		String uid1;
		String uid2;
		// Create some known entries
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar"); //$NON-NLS-1$
			
			{
				VEvent event = new VEvent(new DateTime(java.util.Date.from(start1.toInstant())), new DateTime(java.util.Date.from(start1.plus(1, ChronoUnit.HOURS).toInstant())), "Test Event 1")
					.getFluentTarget();
				Calendar icsCalendar = new Calendar()
					.withProdId("-//Test Suite//OpenNTF 1.0//EN")
					.withComponent(event)
					.getFluentTarget();
				uid1 = readString(target.request().post(Entity.entity(icsCalendar.toString(), "text/calendar")));
				assertFalse(StringUtil.isEmpty(uid1), "UID should not be empty");
			}
			{
				VEvent event = new VEvent(new DateTime(java.util.Date.from(start2.toInstant())), new DateTime(java.util.Date.from(start2.plus(1, ChronoUnit.HOURS).toInstant())), "Test Event 2")
					.getFluentTarget();
				Calendar icsCalendar = new Calendar()
					.withProdId("-//Test Suite//OpenNTF 1.0//EN")
					.withComponent(event)
					.getFluentTarget();
				uid2 = readString(target.request().post(Entity.entity(icsCalendar.toString(), "text/calendar")));
				assertFalse(StringUtil.isEmpty(uid2), "UID should not be empty");
			}
		}
		
		// Try reading both entries
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/readEntries") //$NON-NLS-1$
				.queryParam("start", start1.toLocalDate().minus(1, ChronoUnit.DAYS))
				.queryParam("end", start1.toLocalDate().plus(2, ChronoUnit.DAYS));
			String icsData = readString(target.request().get());
			
			Calendar cal = new CalendarBuilder().build(new StringReader(icsData));
			List<VEvent> events = cal.getComponents("VEVENT");
			assertEquals(2, events.size());
			assertTrue(events.stream().anyMatch(e -> uid1.equals(e.getUid().getValue())), "Should contain UID 1");
			assertTrue(events.stream().anyMatch(e -> uid2.equals(e.getUid().getValue())), "Should contain UID 2");
		}
		
		// Read one entry
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/readEntries") //$NON-NLS-1$
				.queryParam("start", start1.toLocalDate().minus(1, ChronoUnit.DAYS))
				.queryParam("end", start1.toLocalDate().plus(2, ChronoUnit.DAYS))
				.queryParam("page","1")
				.queryParam("pageSize", "1");
			String icsData = readString(target.request().get());
			
			Calendar cal = new CalendarBuilder().build(new StringReader(icsData));
			List<VEvent> events = cal.getComponents("VEVENT");
			assertEquals(1, events.size());
			assertTrue(events.stream().anyMatch(e -> uid1.equals(e.getUid().getValue())), "Should contain UID 1");
			assertFalse(events.stream().anyMatch(e -> uid2.equals(e.getUid().getValue())), "Should not contain UID 2");
		}
		// Read second entry
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/readEntries") //$NON-NLS-1$
				.queryParam("start", start1.toLocalDate().minus(1, ChronoUnit.DAYS))
				.queryParam("end", start1.toLocalDate().plus(2, ChronoUnit.DAYS))
				.queryParam("page","2")
				.queryParam("pageSize", "1");
			String icsData = readString(target.request().get());
			Calendar cal = new CalendarBuilder().build(new StringReader(icsData));
			List<VEvent> events = cal.getComponents("VEVENT");
			assertEquals(1, events.size());
			assertFalse(events.stream().anyMatch(e -> uid1.equals(e.getUid().getValue())), "Should contain UID 1");
			assertTrue(events.stream().anyMatch(e -> uid2.equals(e.getUid().getValue())), "Should not contain UID 2");
		}
		
		// Delete the first entry
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/" + URLEncoder.encode(uid1, "UTF-8"));
			readString(target.request().delete());
		}
		
		// Read all and expect only the second
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/readEntries") //$NON-NLS-1$
					.queryParam("start", start1.toLocalDate().minus(1, ChronoUnit.DAYS))
					.queryParam("end", start1.toLocalDate().plus(2, ChronoUnit.DAYS));
			String icsData = readString(target.request().get());
			
			Calendar cal = new CalendarBuilder().build(new StringReader(icsData));
			List<VEvent> events = cal.getComponents("VEVENT");
			assertEquals(1, events.size());
			assertFalse(events.stream().anyMatch(e -> uid1.equals(e.getUid().getValue())), "Should contain UID 1");
			assertTrue(events.stream().anyMatch(e -> uid2.equals(e.getUid().getValue())), "Should not contain UID 2");
		}
		
		// Delete the second entry
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/" + URLEncoder.encode(uid2, "UTF-8"));
			readString(target.request().delete());
		}
		
		// Ensure that the result is empty
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/readEntries") //$NON-NLS-1$
					.queryParam("start", start1.toLocalDate().minus(1, ChronoUnit.DAYS))
					.queryParam("end", start1.toLocalDate().plus(2, ChronoUnit.DAYS));
			String icsData = readString(target.request().get());
			assertTrue(StringUtil.isEmpty(icsData), () -> "Received unexpected content: " + icsData);
		}
	}
	
	@Test
	public void testGetFakeEntry() throws IOException, ParserException {
		Client client = getAdminClient();
		WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/idonotexist"); //$NON-NLS-1$
		Response resp = target.request().get();
		assertEquals(404, resp.getStatus(), () -> "Unexpected status: " + resp.getStatus() + ", content: " + resp.readEntity(String.class));
	}
	
	@Test
	public void testGetEntry() throws IOException, ParserException {
		Client client = getAdminClient();
		
		OffsetDateTime start1 = OffsetDateTime.now().plus(7, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
		
		String uid1;
		// Create the entry
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar"); //$NON-NLS-1$
			
			{
				VEvent event = new VEvent(new DateTime(java.util.Date.from(start1.toInstant())), new DateTime(java.util.Date.from(start1.plus(1, ChronoUnit.HOURS).toInstant())), "Test Event 1")
					.getFluentTarget();
				Calendar icsCalendar = new Calendar()
					.withProdId("-//Test Suite//OpenNTF 1.0//EN")
					.withComponent(event)
					.getFluentTarget();
				uid1 = readString(target.request().post(Entity.entity(icsCalendar.toString(), "text/calendar")));
				assertFalse(StringUtil.isEmpty(uid1), "UID should not be empty");
			}
		}
		
		// Try to read it with GET
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/" + URLEncoder.encode(uid1, "UTF-8")); //$NON-NLS-1$
			String icsData = readString(target.request().get());
			
			Calendar cal = new CalendarBuilder().build(new StringReader(icsData));
			List<VEvent> events = cal.getComponents("VEVENT");
			assertEquals(1, events.size());
			assertTrue(events.stream().anyMatch(e -> uid1.equals(e.getUid().getValue())), "Should contain UID 1");
		}
		
		// Try to clean up the entry
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/" + URLEncoder.encode(uid1, "UTF-8"));
			readString(target.request().delete());
		}
	}
	
	@Test
	public void testQueryTimeOnly() throws IOException, ParserException {
		Client client = getAdminClient();
		
		OffsetDateTime start1 = OffsetDateTime.now().plus(3, ChronoUnit.DAYS).truncatedTo(ChronoUnit.SECONDS);
		
		String uid1;
		// Create the entry with the initial time
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar"); //$NON-NLS-1$
			
			{
				VEvent event = new VEvent(toDt(start1.toInstant()), toDt(start1.plus(1, ChronoUnit.HOURS).toInstant()), "Test Event 1")
					.getFluentTarget();
				Calendar icsCalendar = new Calendar()
					.withProdId("-//Test Suite//OpenNTF 1.0//EN")
					.withComponent(event)
					.getFluentTarget();
				uid1 = readString(target.request().post(Entity.entity(icsCalendar.toString(), "text/calendar")));
				assertFalse(StringUtil.isEmpty(uid1), "UID should not be empty");
			}
		}
		
		// Try to read it with a time-only range
		{
			LocalTime startTime = start1.atZoneSameInstant(ZoneOffset.UTC).toLocalTime();
			LocalTime endTime = startTime.plus(2, ChronoUnit.HOURS);
			if(startTime.isAfter(endTime)) {
				// This will happen at midnight, so swap
				LocalTime temp = startTime;
				startTime = endTime;
				endTime = temp;
			}
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/readEntries") //$NON-NLS-1$
				.queryParam("start", startTime)
				.queryParam("end", endTime);
			String icsData = readString(target.request().get());
			assertFalse(StringUtil.isEmpty(icsData));
			
			Calendar cal = new CalendarBuilder().build(new StringReader(icsData));
			List<VEvent> events = cal.getComponents("VEVENT");
			assertEquals(1, events.size());
			assertTrue(events.stream().anyMatch(e -> uid1.equals(e.getUid().getValue())), "Should contain UID 1");
		}
		
		// Try to clean up the entry
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/" + URLEncoder.encode(uid1, "UTF-8"));
			readString(target.request().delete());
		}
	}
	
	@Test
	public void testUpdateEntry() throws IOException, ParserException {
		Client client = getAdminClient();
		
		OffsetDateTime start1 = OffsetDateTime.now().plus(10, ChronoUnit.DAYS);
		OffsetDateTime start2 = start1.plus(3, ChronoUnit.HOURS).plus(1, ChronoUnit.DAYS);
		
		String uid1;
		// Create the entry with the initial time
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar"); //$NON-NLS-1$
			
			{
				VEvent event = new VEvent(toDt(start1.toInstant()), toDt(start1.plus(1, ChronoUnit.HOURS).toInstant()), "Test Event 1")
					.getFluentTarget();
				Calendar icsCalendar = new Calendar()
					.withProdId("-//Test Suite//OpenNTF 1.0//EN")
					.withComponent(event)
					.getFluentTarget();
				uid1 = readString(target.request().post(Entity.entity(icsCalendar.toString(), "text/calendar")));
				assertFalse(StringUtil.isEmpty(uid1), "UID should not be empty");
			}
		}
		
		// Ensure it was written properly
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/readEntries") //$NON-NLS-1$
				.queryParam("start", start1.minus(1, ChronoUnit.DAYS))
				.queryParam("end", start1.plus(2, ChronoUnit.DAYS));
			String icsData = readString(target.request().get());
			
			Calendar cal = new CalendarBuilder().build(new StringReader(icsData));
			List<VEvent> events = cal.getComponents("VEVENT");
			assertEquals(1, events.size());
			assertTrue(events.stream().anyMatch(e -> uid1.equals(e.getUid().getValue())), "Should contain UID 1");
		}
		
		// Update it
		{
			VEvent event = new VEvent(toDt(start2.toInstant()), toDt(start2.plus(1, ChronoUnit.HOURS).toInstant()), "Test Event 1 Updated")
				.withProperty(new Uid(uid1))
				.getFluentTarget();
			Calendar icsCalendar = new Calendar()
				.withProdId("-//Test Suite//OpenNTF 1.0//EN")
				.withComponent(event)
				.getFluentTarget();
			System.out.println("sending ics data " + icsCalendar.toString());
				
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/" + URLEncoder.encode(uid1, "UTF-8"));
			readString(target.request().put(Entity.entity(icsCalendar.toString(), "text/calendar")));
		}
		
		// Make sure it was updated
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/readEntries") //$NON-NLS-1$
				.queryParam("start", start1.minus(1, ChronoUnit.DAYS))
				.queryParam("end", start1.plus(2, ChronoUnit.DAYS));
			String icsData = readString(target.request().get());
			
			Calendar cal = new CalendarBuilder().build(new StringReader(icsData));
			List<VEvent> events = cal.getComponents("VEVENT");
			assertEquals(1, events.size());
			assertTrue(events.stream().anyMatch(e -> uid1.equals(e.getUid().getValue())), "Should contain UID 1");
			
			VEvent event = events.get(0);
			assertEquals("Test Event 1 Updated", event.getSummary().getValue());
			assertEquals(toDt(start2.toInstant()), event.getStartDate().getDate());
		}
		
		// Try to clean up the entry
		{
			WebTarget target = client.target(getRestUrl(null, TestDatabase.MAIN) + "/calendar/" + URLEncoder.encode(uid1, "UTF-8"));
			readString(target.request().delete());
		}
	}
	
	private DateTime toDt(Instant instant) {
		DateTime dt = new DateTime(java.util.Date.from(instant));
		dt.setUtc(true);
		return dt;
	}
	
	private String readString(Response response) {
		assertTrue(response.getStatus() >= 200 && response.getStatus() < 300, () -> "Unexpected status: " + response.getStatus() + "; content: " + response.readEntity(String.class));
		return response.readEntity(String.class);
	}
}
