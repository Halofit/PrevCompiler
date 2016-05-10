package compiler.data.ast;

import compiler.common.report.*;

/**
 * @author sliva
 */
public abstract class Expr extends ASTNode {

	public Expr(Position position) {
		super(position);
	}

}
