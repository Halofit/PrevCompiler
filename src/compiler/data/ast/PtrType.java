package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

/**
 * @author sliva
 */
public class PtrType extends Type {

	public final Type baseType;

	public PtrType(Position position, Type baseType) {
		super(position);
		this.baseType = baseType;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
