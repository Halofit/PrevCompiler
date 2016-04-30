package compiler.data.ast;

import java.util.*;

import compiler.common.report.*;
import compiler.data.ast.code.*;

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
