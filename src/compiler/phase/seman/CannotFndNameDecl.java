package compiler.phase.seman;

/**
 * An exception thrown when the declaration of a name cannot be found in the
 * symbol table.
 * 
 * @author sliva
 */
@SuppressWarnings("serial")
public class CannotFndNameDecl extends Exception {

	public CannotFndNameDecl(String message) {
		super(message);
	}

}
