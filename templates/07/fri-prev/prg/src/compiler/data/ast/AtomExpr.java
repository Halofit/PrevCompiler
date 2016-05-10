package compiler.data.ast;

import compiler.common.report.*;
import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public class AtomExpr extends Expr {

	public enum AtomTypes {
		INTEGER, BOOLEAN, CHAR, STRING, PTR, VOID,
	}

	public final AtomTypes type;
	
	public final String value;
	
	public AtomExpr(Position position, AtomTypes type, String value) {
		super(position);
		this.type = type;
		this.value = value;
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
