package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class BinExpr extends Expr {

	public enum Oper {
		ASSIGN, OR, AND, EQU, NEQ, LTH, GTH, LEQ, GEQ, ADD, SUB, MUL, DIV, MOD, ARR, REC
	};

	public final Oper oper;

	public final Expr fstExpr;

	public final Expr sndExpr;

	public BinExpr(Position position, Oper oper, Expr fstExpr, Expr sndExpr) {
		super(position);
		this.oper = oper;
		this.fstExpr = fstExpr;
		this.sndExpr = sndExpr;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
