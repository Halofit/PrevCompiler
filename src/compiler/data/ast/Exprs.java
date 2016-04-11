package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

import java.util.LinkedList;

/**
 * @author sliva
 */
public class Exprs extends Expr {

	private final Expr[] exprs;
	
	public Exprs(Position position, LinkedList<Expr> exprs) {
		super(position);
		this.exprs = new Expr[exprs.size()];
		for (int e = 0; e < exprs.size(); e++)
			this.exprs[e] = exprs.get(e);
	}
	
	public int numExprs() {
		return exprs.length;
	}
	
	public Expr expr(int e) {
		return exprs[e];
	}

	public Expr lastExpr(){
		return exprs[exprs.length-1];
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
