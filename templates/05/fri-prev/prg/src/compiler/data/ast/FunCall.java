package compiler.data.ast;

import java.util.*;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class FunCall extends Expr implements Declarable {

	private String name;
	
	private final Expr[] args;

	public FunCall(Position position, String name, LinkedList<Expr> args) {
		super(position);
		this.name = name;
		this.args = new Expr[args.size()];
		for (int a = 0; a < args.size(); a++)
			this.args[a] = args.get(a);
	}

	public int numArgs() {
		return args.length;
	}
	
	public Expr arg(int a) {
		return args[a];
	}

	@Override
	public String name() {
		return name;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
