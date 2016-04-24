package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

import java.util.LinkedList;

/**
 * @author sliva
 */
public class FunDef extends FunDecl {

	public final Expr body;

	public FunDef(Position position, String name, LinkedList<ParDecl> pars, Type type, Expr body) {
		super(position, name, pars, type);
		this.body = body;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
