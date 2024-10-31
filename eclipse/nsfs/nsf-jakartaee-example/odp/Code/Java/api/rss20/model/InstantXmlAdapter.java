/**
 * Copyright (c) 2022-2024 Contributors to the OpenNTF Home App Project
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
package api.rss20.model;

import java.time.Instant;
import java.time.format.DateTimeFormatter;

import jakarta.xml.bind.annotation.adapters.XmlAdapter;

public class InstantXmlAdapter extends XmlAdapter<String, Instant> {

	@Override
	public Instant unmarshal(String v) throws Exception {
		return Instant.from(DateTimeFormatter.ISO_INSTANT.parse(v));
	}

	@Override
	public String marshal(Instant v) throws Exception {
		return DateTimeFormatter.ISO_INSTANT.format(v);
	}

}
