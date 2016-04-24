package compiler.data.ast;

import compiler.common.report.Position;
import compiler.data.ast.code.Visitor;

/**
 * Denotes an error in sentential form denoting a declaration error.
 * 
 * @author sliva
 */
public class DeclError extends Decl {

	public DeclError() {
		super(new Position("", 0, 0), null, null);
	}

	@Override
	public void accept(Visitor visitor) {
		visitor.visit(this);
	}

}
