package compiler.data.acc;

import compiler.common.logger.*;

/**
 * Access to a static variable.
 * 
 * @author sliva
 */
public class StaticAccess extends Access {

	/**
	 * The label the variable is stored at.
	 */
	public final String label;

	/**
	 * Constructs a new access to a static variable.
	 * 
	 * @param label
	 *            The label the variable is stored at
	 * @param size
	 *            The size of a variable.
	 */
	public StaticAccess(String label, long size) {
		super(size);
		this.label = label;
	}

	@Override
	public void log(Logger logger) {
		logger.begElement("access");
		logger.addAttribute("label", label);
		logger.addAttribute("size", Long.toString(size));
		logger.endElement();
	}

}
