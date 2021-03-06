package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

import java.util.LinkedList;

/**
 * @author sliva
 */
public class FunDecl extends Decl {

	public final ParDecl[] pars;
	
	public FunDecl(Position position, String name, LinkedList<ParDecl> pars, Type type) {
		super(position, name, type);
		this.pars = new ParDecl[pars.size()];
		for (int p = 0; p < pars.size(); p++)
			this.pars[p] = pars.get(p);
	}
	
	public int numPars() {
		return pars.length;
	}
	
	public ParDecl par(int p) {
		return pars[p];
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
