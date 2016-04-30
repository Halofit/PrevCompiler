package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;

/**
 * LABEL represents a label.
 * 
 * @author sliva
 */
public class LABEL extends IMCStmt {

	/* The label. */
	public final String label;

	/**
	 * Constructs a new label with a given name.
	 * 
	 * @param label
	 *            The label.
	 */
	public LABEL(String label) {
		this.label = label;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "LABEL:" + label);
		logger.endElement();
	}

	/** The number of all anonymous label names. */
	private static int labelNameCount = 0;

	/**
	 * Returns a new anonymous label name.
	 * 
	 * @return A new anonymous label name.
	 */
	public static String newLabelName() {
		labelNameCount++;
		return "L" + labelNameCount;
	}
	
	@Override
	public STMTS linCode() {
		Vector<IMCStmt> lc = new Vector<IMCStmt>();
		lc.add(new LABEL(label));
		return new STMTS(lc);
	}

}
