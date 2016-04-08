package compiler.data.ast;

import java.util.*;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class WhereExpr extends Expr {

	public final Expr expr;

	private final Decl[] decls;

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
