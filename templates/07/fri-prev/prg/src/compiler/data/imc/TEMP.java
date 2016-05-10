package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;

/**
 * TEMP represents a temporary register.
 * 
 * @author sliva
 */
public class TEMP extends IMCExpr {

	/** The name of a temporary register. */
	public final int name;

	/**
	 * Constructs a new temporary register with a given name.
	 * 
	 * @param name
	 *            The name of a temporary register.
	 */
	public TEMP(int name) {
		this.name = name;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "TEMP:" + name);
		logger.endElement();
	}

	/** The number of all temporary register names. */
	public static int tempNameCount = 0;

	/**
	 * Returns a new temporary register name.
	 * 
	 * @return A new temporary register name.
	 */
	public static int newTempName() {
		tempNameCount++;
		return tempNameCount;
	}
	
	@Override
	public SEXPR linCode() {
		return new SEXPR(new STMTS(new Vector<IMCStmt>()), new TEMP(name));
	}

}
