package compiler.data.ast;

import java.util.*;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class FunDecl extends Decl {

	private final ParDecl[] pars;
	
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
