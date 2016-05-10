package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;

/**
 * BINOP represents a binary operation.
 * 
 * @author sliva
 */
public class BINOP extends IMCExpr {

	public enum Oper {
		OR, AND, EQU, NEQ, LTH, GTH, LEQ, GEQ, ADD, SUB, MUL, DIV, MOD
	};

	/** The operator. */
	public final Oper oper;

	/** The first subexpression. */
	public final IMCExpr expr1;

	/** The second subexpression. */
	public final IMCExpr expr2;

	/**
	 * Constructs a new BINOP.
	 * 
	 * @param oper
	 *            The operator.
	 * @param expr1
	 *            The first subexpression.
	 * @param expr2
	 *            The second subexpression.
	 */
	public BINOP(Oper oper, IMCExpr expr1, IMCExpr expr2) {
		this.oper = oper;
		this.expr1 = expr1;
		this.expr2 = expr2;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "BINOP:" + oper);
		if (expr1 != null) expr1.toXML(logger);
		if (expr2 != null) expr2.toXML(logger);
		logger.endElement();
	}
			
	@Override
	public SEXPR linCode() {
		int result = TEMP.newTempName();
		SEXPR expr1LC = expr1.linCode();
		SEXPR expr2LC = expr2.linCode();
		Vector<IMCStmt> lc = new Vector<IMCStmt>();
		lc.addAll(((STMTS)(expr1LC.stmt)).stmts());
		lc.addAll(((STMTS)(expr2LC.stmt)).stmts());
		lc.add(new MOVE(new TEMP(result), new BINOP(oper, expr1LC.expr, expr2LC.expr)));
		return new SEXPR(new STMTS(lc), new TEMP(result));
	}

}
