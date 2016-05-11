package compiler.data.imc;

import compiler.common.logger.Logger;

import java.util.Vector;

/**
 * ESTMT represents an expression when the result is thrown away.
 * 
 * @author sliva
 */
public class ESTMT extends IMCStmt {

	/** The expression. */
	public final IMCExpr expr;

	/**
	 * Constructs a new expression statement.
	 * 
	 * @param expr
	 *            The expression.
	 */
	public ESTMT(IMCExpr expr) {
		this.expr = expr;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "ESTMT");
		if (expr != null) expr.toXML(logger);
		logger.endElement();
	}

	@Override
	public STMTS linCode() {
		SEXPR exprLC = expr.linCode();
		Vector<IMCStmt> lc = new Vector<IMCStmt>();
		lc.addAll(((STMTS)(exprLC.stmt)).stmts());
		// expression part of exprLC is thrown away
		return new STMTS(lc);
	}

}
