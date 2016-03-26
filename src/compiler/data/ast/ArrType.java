package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class ArrType extends Type {

	public final Expr size;

	public final Type elemType;

	public ArrType(Position position, Expr size, Type elemType) {
		super(position);
		this.size = size;
		this.elemType = elemType;
	}
	
	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
