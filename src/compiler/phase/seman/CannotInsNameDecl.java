package compiler.phase.seman;

import compiler.common.report.CompilerError;

/**
 * An exception thrown when the declaration of a name cannot be inserted into
 * the symbol table.
 * 
 * @author sliva
 */
@SuppressWarnings("serial")
public class CannotInsNameDecl extends CompilerError {

	public CannotInsNameDecl(String message) {
		super(message);
	}

}
