package compiler.phase.seman;

import java.util.LinkedList;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.typ.*;

/**
 * Type checker.
 * 
 * <p>
 * Type checker checks type of all sentential forms of the program and resolves
 * the component names as this cannot be done earlier, i.e., in
 * {@link compiler.phase.seman.EvalDecl}.
 * </p>
 * 
 * @author sliva
 */
public class EvalTyp extends FullVisitor {

	private final Attributes attrs;
	
	public EvalTyp(Attributes attrs) {
		this.attrs = attrs;
	}
	
	/** The symbol table. */
	private SymbolTable symbolTable = new SymbolTable();

	// TODO
}
