package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

/**
 * @author sliva
 */
public class TypeName extends Type implements Declarable {

	private final String name;

	public TypeName(Position position, String name) {
		super(position);
		this.name = name;
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
