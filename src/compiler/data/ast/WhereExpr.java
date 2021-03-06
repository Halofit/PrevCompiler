package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

import java.util.LinkedList;

/**
 * @author sliva
 */
public class WhereExpr extends Expr {

	public final Expr expr;

	public final Decl[] decls;

	public WhereExpr(Position position, Expr expr, LinkedList<Decl> decls) {
		super(position);
		this.expr = expr;
		this.decls = new Decl[decls.size()];
		for (int d = 0; d < decls.size(); d++)
			this.decls[d] = decls.get(d);
	}

	public int numDecls() {
		return decls.length;
	}

	public Decl decl(int d) {
		return decls[d];
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
