package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;

/**
 * CONST represents a constant.
 * 
 * @author sliva
 */
public class CONST extends IMCExpr {

	public final long value;

	/**
	 * Constructs a new CONST.
	 * 
	 * @param value
	 *            The value of this constant.
	 */
	public CONST(long value) {
		this.value = value;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "CONST:" + value);
		logger.endElement();
	}

	@Override
	public SEXPR linCode() {
		return new SEXPR(new STMTS(new Vector<IMCStmt>()), new CONST(value));
	}
	
}
