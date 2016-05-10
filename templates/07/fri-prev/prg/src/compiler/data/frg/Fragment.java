package compiler.data.frg;

import compiler.common.logger.Logger;

/**
 * A fragment of code or data.
 * 
 * @author sliva
 */
public abstract class Fragment {

	/** The label of this fragment. */
	public final String label;
	
	/**
	 * Constructs a new code fragment.
	 * 
	 * @param label
	 *            The label of this fragment.
	 */
	public Fragment(String label) {
		this.label = label;
	}

	public abstract void toXML(Logger logger);

}
