package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;

/**
 * NOP represents no operation.
 * 
 * @author sliva
 */
public class NOP extends IMCExpr {

	public NOP() {
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "NOP");
		logger.endElement();
	}
	
	@Override
	public SEXPR linCode() {
		return new SEXPR(new STMTS(new Vector<IMCStmt>()), new NOP());
	}

	
}
