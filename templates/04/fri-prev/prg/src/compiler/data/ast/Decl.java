package compiler.data.ast;

import compiler.common.report.*;

/**
 * @author sliva
 */
public abstract class Decl extends ASTNode {

	public final String name;

	public final Type type;

	public Decl(Position position, String name, Type type) {
		super(position);
		this.name = name;
		this.type = type;
	}

}
