package compiler.data.acc;

import compiler.common.logger.*;

/**
 * Access to a variable using an offset, i.e., relative to either FP
 * (stack-based variables) or record base address (record components).
 * 
 * @author sliva
 */
public class OffsetAccess extends Access {

	/**
	 * The offset from the FP.
	 */
	public final long offset;

	/**
	 * Constructs a new access using an offset.
	 * 
	 * @param offset
	 *            The offset from the FP.
	 * @param size
	 *            The size of a variable.
	 */
	public OffsetAccess(long offset, long size) {
		super(size);
		this.offset = offset;
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("access");
		logger.addAttribute("offset", Long.toString(offset));
		logger.addAttribute("size", Long.toString(size));
		logger.endElement();
	}

}
