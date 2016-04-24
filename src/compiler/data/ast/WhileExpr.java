package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

/**
 * @author sliva
 */
public class WhileExpr extends Expr {

	public final Expr cond;
	
	public final Expr body;
	
	public WhileExpr(Position position, Expr cond, Expr body) {
		super(position);
		this.cond = cond;
		this.body = body;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
