package org.openntf.xsp.jakarta.transaction;

import java.io.Serializable;
import java.nio.ByteBuffer;
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
		ByteBuffer buf = ByteBuffer.allocate(Long.BYTES);
		Random rnd = new SecureRandom();
		
		buf.putLong(rnd.nextLong());
		this.globalTransactionId = new byte[Long.BYTES];
		buf.position(0);
		buf.get(this.globalTransactionId);
		
		buf.position(0);
		buf.putLong(rnd.nextLong());
		this.branchQualifier = new byte[Long.BYTES];
		buf.position(0);
		buf.get(this.branchQualifier);
		
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
