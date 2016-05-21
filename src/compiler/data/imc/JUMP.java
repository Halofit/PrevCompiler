package compiler.data.imc;

import compiler.common.logger.Logger;
import compiler.phase.codegen.CodeGen;

import java.util.Vector;

/**
 * JUMP represents an unconditional jump.
 * 
 * @author sliva
 */
public class JUMP extends IMCStmt {

	/** The label. */
	public /*final*/ String label;

	/**
	 * Constructs a new unconditional jump.
	 * 
	 * @param label
	 *            The label.
	 */
	public JUMP(String label) {
		this.label = label;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "JUMP:" + label);
		logger.endElement();
	}
	
	@Override
	public STMTS linCode() {
		Vector<IMCStmt> lc = new Vector<IMCStmt>();
		lc.add(new JUMP(label));
		return new STMTS(lc);
	}

	@Override
	public String toString() {
		return "JUMP(" + label + ')';
	}


	@Override
	public void visit(CodeGen phase) {
		phase.tile(this);
	}
}
