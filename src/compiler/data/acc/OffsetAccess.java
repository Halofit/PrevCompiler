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
	 * The static level.
	 */
	public final int level;

	/**
	 * The offset from the FP.
	 */
	public final long offset;

	/**
	 * Constructs a new access using an offset.
	 * 
	 * @param level
	 *            The static level (or -1 if this is a record component).
	 * @param offset
	 *            The offset from the FP.
	 * @param size
	 *            The size of a variable.
	 */
	public OffsetAccess(int level, long offset, long size) {
		super(size);
		this.level = level;
		this.offset = offset;
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("access");
		if (level >= 0) logger.addAttribute("level", Long.toString(level));
		logger.addAttribute("offset", Long.toString(offset));
		logger.addAttribute("size", Long.toString(size));
		logger.endElement();
	}

}
