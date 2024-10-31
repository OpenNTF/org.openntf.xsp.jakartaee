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
package org.openntf.xsp.jakarta.transaction;

import java.io.Serializable;
import java.security.SecureRandom;
import java.util.Arrays;
import java.util.Random;

import javax.transaction.xa.Xid;

/**
 *
 * @author Jesse Gallagher
 * @since 2.7.0
 */
public class DominoXid implements Xid, Serializable {
	private static final long serialVersionUID = 1L;

	private int formatId = 83925;
	private byte[] globalTransactionId;
	private byte[] branchQualifier;

	public DominoXid() {
		Random rnd = new SecureRandom();

		this.globalTransactionId = new byte[Long.BYTES];
		rnd.nextBytes(this.globalTransactionId);

		this.branchQualifier = new byte[Long.BYTES];
		rnd.nextBytes(this.branchQualifier);

	}

	@Override
	public int getFormatId() {
		return formatId;
	}

	@Override
	public byte[] getGlobalTransactionId() {
		return Arrays.copyOf(this.globalTransactionId, this.globalTransactionId.length);
	}

	@Override
	public byte[] getBranchQualifier() {
		return Arrays.copyOf(this.branchQualifier, this.branchQualifier.length);
	}

}
