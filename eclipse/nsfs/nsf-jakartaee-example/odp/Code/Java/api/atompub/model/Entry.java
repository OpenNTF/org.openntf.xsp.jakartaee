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
package api.atompub.model;

import java.time.Instant;
import java.util.ArrayList;
import java.util.List;

import api.rss20.model.InstantXmlAdapter;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;
import jakarta.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

@XmlRootElement(namespace=AtomPubService.NS_ATOM)
public class Entry {
	private String title;
	private String id;
	private Instant updated;
	private Author author;
	private Summary summary;
	private Content content;
	private List<Link> links = new ArrayList<>();
	private Instant published;
	private List<AtomCategory> categories = new ArrayList<>();
	private Control control;

	@XmlElement(namespace=AtomPubService.NS_ATOM)
	public String getTitle() {
		return title;
	}
	public void setTitle(String title) {
		this.title = title;
	}
	@XmlElement(namespace=AtomPubService.NS_ATOM)
	public String getId() {
		return id;
	}
	public void setId(String id) {
		this.id = id;
	}
	@XmlElement(namespace=AtomPubService.NS_ATOM)
	@XmlJavaTypeAdapter(InstantXmlAdapter.class)
	public Instant getUpdated() {
		return updated;
	}
	public void setUpdated(Instant updated) {
		this.updated = updated;
	}
	@XmlElementRef
	public Author getAuthor() {
		return author;
	}
	public void setAuthor(Author author) {
		this.author = author;
	}
	@XmlElementRef
	public Summary getSummary() {
		return summary;
	}
	public void setSummary(Summary summary) {
		this.summary = summary;
	}
	@XmlElementRef
	public Content getContent() {
		return content;
	}
	public void setContent(Content content) {
		this.content = content;
	}
	@XmlElementRef
	public List<Link> getLinks() {
		return links;
	}
	@XmlElement(namespace=AtomPubService.NS_ATOM)
	@XmlJavaTypeAdapter(InstantXmlAdapter.class)
	public Instant getPublished() {
		return published;
	}
	public void setPublished(Instant published) {
		this.published = published;
	}
	@XmlElementRef
	public List<AtomCategory> getCategories() {
		return categories;
	}
	@XmlElementRef
	public Control getControl() {
		return control;
	}
	public void setControl(Control control) {
		this.control = control;
	}
}
