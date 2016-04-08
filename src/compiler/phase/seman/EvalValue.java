package compiler.phase.seman;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;

/**
 * Computes the value of simple integer constant expressions.
 * 
 * <p>
 * Simple integer constant expressions consists of integer constants and five
 * basic arithmetic operators (<code>ADD</code>, <code>SUB</code>,
 * <code>MUL</code>, <code>DIV</code>, and <code>MOD</code>).
 * </p>
 * 
 * <p>
 * This is needed during type resolving and type checking to compute the correct
 * array types.
 * </p>
 * 
 * @author sliva
 */
public class EvalValue extends FullVisitor {

	private final Attributes attrs;
	
	public EvalValue(Attributes attrs) {
		this.attrs = attrs;
	}
	
	// TODO
}
