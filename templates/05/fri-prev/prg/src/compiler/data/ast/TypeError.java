package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * Denotes an error in sentential form denoting a type.
 * 
 * @author sliva
 */
public class TypeError extends Type {

	public TypeError() {
		super(new Position("", 0, 0));
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
