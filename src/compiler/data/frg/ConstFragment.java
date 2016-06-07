package compiler.data.frg;

import compiler.common.logger.Logger;

/**
 * A fragment containing a string constant.
 * 
 * @author sliva
 */
public class ConstFragment extends Fragment {

	/** The string value. */
	public String string;

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

	public String getStringAsValues(){
		StringBuilder sb = new StringBuilder();

		for (int i = 1; i < this.string.length() - 1; i++) {
			char c = this.string.charAt(i);
			if (c == '\\') {
				switch (this.string.charAt(i + 1)) {
					case '\'':
						c = '\'';
						break;
					case '\"':
						c = '\"';
						break;
					case 'n':
						c = '\n';
						break;
					case 't':
						c = '\t';
						break;
				}
				i++;
			}
			sb.append((int)c);
			sb.append(","); //THERE MUST BE NO SPACE
		}

		sb.append(0);
		return sb.toString();
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("frg");
		logger.addAttribute("kind", "CONST " + "(" + label + "," + string  + ")");
		logger.endElement();
	}

}
