/**
 * Copyright (c) 2018-2025 Contributors to the XPages Jakarta EE Support Project
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

import jakarta.xml.bind.annotation.XmlElementRef;
import jakarta.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name="service", namespace=AtomPubService.NS_ATOMPUB)
public class AtomPubService {
	public static final String NS_ATOMPUB = "http://purl.org/atom/app#"; //$NON-NLS-1$
	public static final String NS_ATOM = "http://www.w3.org/2005/Atom"; //$NON-NLS-1$
	public static final String NS_APP = "http://www.w3.org/2007/app"; //$NON-NLS-1$
	
	private Workspace workspace = new Workspace();
	
	@XmlElementRef
	public Workspace getWorkspace() {
		return workspace;
	}
	public void setWorkspace(Workspace workspace) {
		this.workspace = workspace;
	}
}
