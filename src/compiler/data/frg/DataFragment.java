package compiler.data.frg;

import compiler.common.logger.Logger;

/**
 * A fragment containing a variable.
 * 
 * @author sliva
 */
public class DataFragment extends Fragment {

	/** The size of a variable. */
	public final long width;

	/**
	 * Constructs a new fragment containing a variable.
	 * 
	 * @param label
	 *            The label of this fragment.
	 * @param width
	 *            The size of a variable.
	 */
	public DataFragment(String label, long width) {
		super(label);
		this.width = width;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("frg");
		logger.addAttribute("kind", "DATA " + "(" + label + "," + width + ")");
		logger.endElement();
	}

}
