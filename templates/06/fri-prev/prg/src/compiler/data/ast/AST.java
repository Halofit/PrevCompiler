package compiler.data.ast;

import compiler.data.ast.code.*;

public interface AST {
		
	public abstract void accept(Visitor visitor);

}