package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class IfExpr extends Expr {

	public final Expr cond;
	
	public final Expr thenExpr;
	
	public final Expr elseExpr;
	
	public IfExpr(Position position, Expr cond, Expr thenExpr, Expr elseExpr) {
		super(position);
		this.cond = cond;
		this.thenExpr = thenExpr;
		this.elseExpr = elseExpr;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
