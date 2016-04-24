package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

/**
 * @author sliva
 */
public class AtomType extends Type {

	public enum AtomTypes {
		INTEGER, BOOLEAN, CHAR, STRING, VOID
	}
	
	public final AtomTypes type;
	
	public AtomType(Position position, AtomTypes type) {
		super(position);
		this.type = type;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
