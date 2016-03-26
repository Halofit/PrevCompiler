package compiler.data.ast;

import java.util.*;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class RecType extends Type {
	
	private final CompDecl[] comps;

	public RecType(Position position, LinkedList<CompDecl> comps) {
		super(position);
		this.comps = new CompDecl[comps.size()];
		for (int c = 0; c < comps.size(); c++)
			this.comps[c] = comps.get(c);
	}

	public int numComps() {
		return comps.length;
	}
	
	public CompDecl comp(int c) {
		return comps[c];
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
