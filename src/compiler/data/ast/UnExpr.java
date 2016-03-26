package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class UnExpr extends Expr {

	public enum Oper {
		ADD, SUB, NOT, VAL, MEM
	};

	public final Oper oper;

	public final Expr subExpr;

	public UnExpr(Position position, Oper oper, Expr subExpr) {
		super(position);
		this.oper = oper;
		this.subExpr = subExpr;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
