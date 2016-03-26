package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class CastExpr extends Expr {

	public final Type type;

	public final Expr expr;

	public CastExpr(Position position, Type type, Expr expr) {
		super(position);
		this.type = type;
		this.expr = expr;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
