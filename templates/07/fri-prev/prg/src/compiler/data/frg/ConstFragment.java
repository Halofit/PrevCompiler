package compiler.data.frg;

import compiler.common.logger.Logger;

/**
 * A fragment containing a string constant.
 * 
 * @author sliva
 */
public class ConstFragment extends Fragment {

	/** The string value. */
	public final String string;

	/**
	 * Constructs a new fragment containing a string constant.
	 * 
	 * @param label
	 *            The label of this fragment.
	 * @param string
	 *            The constant.
	 */
	public ConstFragment(String label, String string) {
		super(label);
		this.string = string;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("frg");
		logger.addAttribute("kind", "CONST " + "(" + label + "," + string  + ")");
		logger.endElement();
	}

}
