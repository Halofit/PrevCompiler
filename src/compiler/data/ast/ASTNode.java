package compiler.data.ast;

import compiler.common.report.*;

/**
 * @author sliva
 */
public abstract class ASTNode extends Position implements AST, Typeable {

	public ASTNode(Position position) {
		super(position);
	}

}
