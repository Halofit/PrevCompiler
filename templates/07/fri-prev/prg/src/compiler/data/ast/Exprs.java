package compiler.data.ast;

import java.util.*;

import compiler.common.report.*;
import compiler.data.ast.code.*;

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

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
