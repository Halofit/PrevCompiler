package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;

/**
 * SEXPR represents an expression prefixed by a statement.
 * 
 * @author sliva
 */
public class SEXPR extends IMCExpr {

	/** A statement. */
	public final IMCStmt stmt;

	/** Expression. */
	public final IMCExpr expr;

	/**
	 * Construct a new statement-expression.
	 * 
	 * @param stmt
	 *            A statement.
	 * @param expr
	 *            An expression.
	 */
	public SEXPR(IMCStmt stmt, IMCExpr expr) {
		this.stmt = stmt;
		this.expr = expr;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "SEXPR");
		if (stmt != null) stmt.toXML(logger);
		if (expr != null) expr.toXML(logger);
		logger.endElement();
	}
	
	@Override
	public SEXPR linCode() {
		STMTS stmtLC = stmt.linCode();
		SEXPR exprLC = expr.linCode();
		Vector<IMCStmt> lc = new Vector<IMCStmt>();
		lc.addAll(stmtLC.stmts());
		lc.addAll(((STMTS)(exprLC.stmt)).stmts());
		return new SEXPR(new STMTS(lc), exprLC.expr);
	}

}
