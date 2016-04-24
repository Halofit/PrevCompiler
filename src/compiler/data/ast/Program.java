package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

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
