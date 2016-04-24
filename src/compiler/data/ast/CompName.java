package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

/**
 * @author sliva
 */
public class CompName extends VarName {

	public CompName(Position position, String name) {
		super(position, name);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
