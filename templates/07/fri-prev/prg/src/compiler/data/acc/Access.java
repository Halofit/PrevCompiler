package compiler.data.acc;

import compiler.common.logger.*;

/**
 * The access to a variable.
 * 
 * @author sliva
 */
public abstract class Access implements Loggable {

	/**
	 * The size of a variable.
	 */
	public final long size;
	
	/**
	 * Constructs a new access.
	 * 
	 * @param size
	 *            The size of a variable.
	 */
	public Access(long size) {
		this.size = size;
	}
}
