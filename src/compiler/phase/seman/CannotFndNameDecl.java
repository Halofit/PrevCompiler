package compiler.phase.seman;

import compiler.common.report.PhaseErrors.SemAnError;

/**
 * An exception thrown when the declaration of a name cannot be found in the
 * symbol table.
 * 
 * @author sliva
 */
@SuppressWarnings("serial")
public class CannotFndNameDecl extends SemAnError {

	public CannotFndNameDecl(String message) {
		super(message);
	}

}
