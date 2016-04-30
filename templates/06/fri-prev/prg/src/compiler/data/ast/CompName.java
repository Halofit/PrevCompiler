package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

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
