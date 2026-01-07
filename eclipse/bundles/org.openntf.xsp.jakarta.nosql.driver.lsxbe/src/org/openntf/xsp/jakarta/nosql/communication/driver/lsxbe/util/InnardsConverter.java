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
package org.openntf.xsp.jakarta.nosql.communication.driver.lsxbe.util;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.time.LocalTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.temporal.JulianFields;
import java.time.temporal.Temporal;

/**
 * Utility method for working with Notes date/time innards,
 * based on the version in JNX.
 * 
 * @author Karsten Lehmann
 * @author Jesse Gallagher
 * @since 3.5.0
 */
public enum InnardsConverter {
	;

	private static final int ALLDAY = 0xffffffff;
	private static final int ANYDAY = 0xffffffff;

	@SuppressWarnings("unused")
	public static Temporal decodeInnards(final int[] innards, boolean dstActive) {
		if (innards == null || innards.length < 2 || innards.length >= 2 && innards[0] == 0 && innards[1] == 0) {
			return null;
		}

		// The Domino and Notes TIMEDATE structure consists of two long words that
		// encode the time, the date, the time zone, and the Daylight Savings Time
		// settings that were in effect when the structure was initialized.
		// The TIMEDATE structure is designed to be accessed exclusively through the
		// time and date subroutines defined in misc.h. This structure is subject to
		// change; the description here is provided for debugging purposes.

		final boolean hasTime = innards[0] != ALLDAY;
		final boolean hasDate = innards[1] != ANYDAY;

		if (!hasDate && !hasTime) {
			return null;
		}

		// The first DWORD, Innards[0], contains the number of hundredths of seconds
		// since midnight, Greenwich mean time. If only the date is important, not the
		// time, this field may be set to ALLDAY.

		final long timeInnard = Integer.toUnsignedLong(innards[0]);
		final long hundredSecondsSinceMidnight = timeInnard;
		long milliSecondsSinceMidnight;
		if (hasTime) {
			milliSecondsSinceMidnight = hundredSecondsSinceMidnight * 10;
		} else {
			milliSecondsSinceMidnight = 0;
		}

		LocalTime utcTime;
		try {
			utcTime = LocalTime.ofNanoOfDay(milliSecondsSinceMidnight * 1000 * 1000);
		} catch (final DateTimeException e) {
			// Observed when the stored data is not representable (e.g. from a randomly-set
			// UNID)
			return null;
		}
		if (!hasDate) {
			return utcTime;
		}

		// The date and the time zone and Daylight Savings Time settings are encoded in
		// Innards[1].
		//
		// The 24 low-order bits contain the Julian Day, the number of days since
		// January 1, 4713 BC.
		//
		// Note that this is NOT the same as the Julian calendar! The Julian Day was
		// originally devised as an aid to astronomers. Since only days are counted,
		// weeks, months, and years are ignored in calculations.
		// The Julian Day is defined to begin at noon; for simplicity, Domino and Notes
		// assume that the day begins at midnight. The high-order byte, bits 31-24,
		// encode the time zone and Daylight Savings Time information.
		// The high-order bit, bit 31 (0x80000000), is set if Daylight Savings Time is
		// observed.
		// Bit 30 (0x40000000) is set if the time zone is east of Greenwich mean time.
		// Bits 27-24 contain the number of hours difference between the time zone and
		// Greenwich mean time, and bits 29-28 contain the number of 15-minute intervals
		// in the difference.

		final int dateInnard = innards[1];

		final long julianDay = dateInnard & 0x7FFFFF;

		final LocalDate utcDate = LocalDate.MIN.with(JulianFields.JULIAN_DAY, julianDay);
		if (hasTime) {
			final OffsetDateTime utc = OffsetDateTime.of(utcDate, utcTime, ZoneOffset.UTC);

			// Determines whether the zone does DST at all
			final boolean dst = (dateInnard & 0x80000000) != 0; // bit 31

			// Figure out the time zone
			final boolean eastOfGmt = (dateInnard & 0x40000000) != 0; // bit 30
			// Non-daylight offset from GMT (e.g. -5h in US Eastern regardless of day of
			// year)
			final int hourOffset = (dateInnard & 0xF000000) >> 24; // bits 27-24
			final int intervalCount = (dateInnard & 0x30000000) >> 28; // bits 29-28

			int offsetSeconds = (eastOfGmt ? 1 : -1) * (hourOffset * 60 * 60 + intervalCount * 15 * 60);
			if(dstActive) {
				offsetSeconds += 60 * 60;
			}

			// Since time zone information is stored only as "normal offset" + "do they do
			// daylight savings at all?", we it's unsafe to try to map to a real time zone.
			// Instead, just return an OffsetDateTime that matches how it was stored
			if (offsetSeconds != 0) {
				// Then just make a generic offset
				final ZoneOffset offset = ZoneOffset.ofTotalSeconds(offsetSeconds);
				return OffsetDateTime.ofInstant(utc.toInstant(), offset);
			} else {
				return utc;
			}
		} else {
			return utcDate;
		}
	}
}
