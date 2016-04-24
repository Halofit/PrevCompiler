package compiler.data.ast;

import compiler.common.report.Position;

/**
 * @author sliva
 */
public abstract class Type extends ASTNode {

	public Type(Position position) {
		super(position);
	}

}
