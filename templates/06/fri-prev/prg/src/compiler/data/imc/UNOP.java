package compiler.data.imc;

import java.util.*;

import compiler.common.logger.*;

/**
 * UNOP represents a unary operation.
 * 
 * @author sliva
 */
public class UNOP extends IMCExpr {

	public enum Oper {
		ADD, SUB, NOT
	};

	/** The operator. */
	public final Oper oper;

	/** The subexpression. */
	public final IMCExpr expr;

	/**
	 * Constructs a new UNOP.
	 * 
	 * @param oper
	 *            The operator.
	 * @param expr
	 *            The subexpression.
	 */
	public UNOP(Oper oper, IMCExpr expr) {
		this.oper = oper;
		this.expr = expr;
	}

	@Override
	public void toXML(Logger logger) {
		logger.begElement("imc");
		logger.addAttribute("kind", "UNOP:" + oper);
		if (expr != null) expr.toXML(logger);
		logger.endElement();
	}
	
	@Override
	public SEXPR linCode() {
		int result = TEMP.newTempName();
		SEXPR exprLC = expr.linCode();
		Vector<IMCStmt> lc = new Vector<IMCStmt>();
		lc.addAll(((STMTS)(exprLC.stmt)).stmts());
		lc.add(new MOVE(new TEMP(result), new UNOP( oper, exprLC.expr)));
		return new SEXPR(new STMTS(lc), new TEMP(result));
	}

}
