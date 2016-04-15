package compiler.common.report;

/**
 * Internal compiler error.
 * 
 * @author sliva
 */
@SuppressWarnings("serial")
public class InternalCompilerError extends CompilerError {

	/**
	 * Internal compiler error.
	 */
	public InternalCompilerError() {
		super("Internal compiler error.");
		this.printStackTrace(System.err);
	}

	public InternalCompilerError(String message, Position pos) {
		super("[Internal compiler error.]" + message + " " + pos);
		this.printStackTrace(System.err);
	}
}
