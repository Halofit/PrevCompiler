package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class Program extends Expr {

	public final Expr expr;
	
	public Program(Position position, Expr expr) {
		super(position);
		this.expr = expr;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
