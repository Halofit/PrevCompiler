package compiler.common.report;

/**
 * Throws a compiler error of unspecified kind.
 * 
 * All other kinds of compiler errors are subclasses of this class and its
 * throwables should be caught in method {@link compiler.Main#main(String[]) main}.
 * 
 * @author sliva
 */
@SuppressWarnings("serial")
public class CompilerError extends Error {

	/**
	 * Compiler error of unspecified kind.
	 * 
	 * @param message Error message.
	 */
	public CompilerError(String message) {
		super(message);
	}

}
