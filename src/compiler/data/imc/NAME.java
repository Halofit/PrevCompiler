package compiler.data.imc;

import compiler.common.logger.Logger;
import compiler.phase.codegen.CodeGen;

import java.util.Vector;

/**
 * NAME represents a symbolic constant.
 * 
 * @author sliva
 */
public class NAME extends IMCExpr {

	/** The name of the symbolic constant. */
	public final String name;

	/**
	 * Constructs a new NAME.
	 * 
	 * @param name
	 *            The name of the symbolic constant.
	 */
	public NAME(String name) {
		this.name = name;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "NAME:" + name);
		logger.endElement();
	}

	@Override
	public SEXPR linCode() {
		return new SEXPR(new STMTS(new Vector<IMCStmt>()), new NAME(name));
	}


	@Override
	public void visit(CodeGen phase) {
		phase.tile(this);
	}
}
