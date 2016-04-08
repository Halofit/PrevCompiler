package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class ParDecl extends VarDecl {

	public ParDecl(Position position, String name, Type type) {
		super(position, name, type);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
