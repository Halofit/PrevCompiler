package compiler.phase.seman;

import compiler.common.report.PhaseErrors.SemAnError;

/**
 * An exception thrown when the declaration of a name cannot be inserted into
 * the symbol table.
 * 
 * @author sliva
 */
@SuppressWarnings("serial")
public class CannotInsNameDecl extends SemAnError {

	public CannotInsNameDecl(String message) {
		super(message);
	}

}
