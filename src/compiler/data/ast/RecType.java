package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

import java.util.LinkedList;

/**
 * @author sliva
 */
public class RecType extends Type {
	
	public final CompDecl[] comps;

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
