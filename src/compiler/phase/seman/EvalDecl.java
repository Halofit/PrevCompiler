package compiler.phase.seman;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;

/**
 * Declaration resolver.
 * 
 * <p>
 * Declaration resolver maps each AST node denoting a
 * {@link compiler.data.ast.Declarable} name to the declaration where
 * this name is declared. In other words, it links each use of each name to a
 * declaration of that name.
 * </p>
 * 
 * @author sliva
 */
public class EvalDecl extends FullVisitor {

	private final Attributes attrs;
	
	public EvalDecl(Attributes attrs) {
		this.attrs = attrs;
	}

	/** The symbol table. */
	private SymbolTable symbolTable = new SymbolTable();

	// TODO
}
