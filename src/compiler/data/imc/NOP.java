package compiler.data.imc;

import compiler.common.logger.Logger;
import compiler.phase.codegen.CodeGen;

import java.util.Vector;

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


	@Override
	public void visit(CodeGen phase) {
		phase.tile(this);
	}
}
