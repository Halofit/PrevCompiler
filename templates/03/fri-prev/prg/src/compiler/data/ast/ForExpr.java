package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class ForExpr extends Expr {

	public final VarName var;
	
	public final Expr loBound;
	
	public final Expr hiBound;
	
	public final Expr body;
	
	public ForExpr(Position position, VarName var, Expr loBound, Expr hiBound, Expr body) {
		super(position);
		this.var = var;
		this.loBound = loBound;
		this.hiBound = hiBound;
		this.body = body;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
