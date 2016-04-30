package compiler.phase.seman;

/**
 * An exception thrown when the declaration of a name cannot be inserted into
 * the symbol table.
 * 
 * @author sliva
 */
@SuppressWarnings("serial")
public class CannotInsNameDecl extends Exception {

	public CannotInsNameDecl(String message) {
		super(message);
	}

}
