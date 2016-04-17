package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * Denotes an error in sentential form denoting an expression.
 * 
 * @author sliva
 */
public class ExprError extends Expr {

	public ExprError() {
		super(new Position("", 0, 0));
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
