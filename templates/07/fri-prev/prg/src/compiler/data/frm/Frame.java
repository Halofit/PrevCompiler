package compiler.data.frm;

import compiler.common.logger.*;

/**
 * A stack frame.
 * 
 * @author sliva
 */
public class Frame implements Loggable {

	/**
	 * The static level.
	 */
	public final int level;

	/**
	 * The entry label.
	 */
	public final String label;

	/**
	 * The size of the stack frame.
	 */
	public final long size;

	/**
	 * The size of block containing input arguments (and a static link) when
	 * function is called and the result when function returns.
	 */
	public final long inpCallSize;

	/**
	 * The size of block containing local variables.
	 */
	public final long locVarsSize;

	/**
	 * The size of block containing temporary variables.
	 */
	public final long tmpVarsSize;

	/**
	 * The size of block containing hidden registers.
	 */
	public final long hidRegsSize;

	/**
	 * The size of block containing output arguments (and a static link) when
	 * function calls another function and the result when the called function
	 * returns.
	 */
	public final long outCallSize;

	/**
	 * Constructs a new empty stack frame.
	 * 
	 * @param level
	 *            The static level.
	 * @param label
	 *            The entry label.
	 */
	public Frame(int level, String label, long inpCallSize, long locVarsSize, long tmpVarsSize, long hidRegsSize,
			long outCallSize) {
		this.level = level;
		this.label = label;

		this.inpCallSize = inpCallSize;
		this.locVarsSize = locVarsSize;
		this.tmpVarsSize = tmpVarsSize;
		this.hidRegsSize = hidRegsSize;
		this.outCallSize = outCallSize;

		this.size = this.locVarsSize + 16 + this.tmpVarsSize + this.hidRegsSize + this.outCallSize;
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("frame");
		logger.addAttribute("level", Integer.toString(level));
		logger.addAttribute("label", label);
		logger.addAttribute("size", Long.toString(size));
		logger.addAttribute("inpCallSize", Long.toString(inpCallSize));
		logger.addAttribute("locVarsSize", Long.toString(locVarsSize));
		logger.addAttribute("tmpVarsSize", Long.toString(tmpVarsSize));
		logger.addAttribute("hidRegsSize", Long.toString(hidRegsSize));
		logger.addAttribute("outCallSize", Long.toString(outCallSize));
		logger.endElement();
	}

}
