package compiler.data.ast;

import compiler.data.ast.code.*;

/**
 * @author sliva
 */
public interface AST {
		
	public abstract void accept(Visitor visitor);

}