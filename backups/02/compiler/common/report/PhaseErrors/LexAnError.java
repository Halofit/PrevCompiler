package compiler.common.report.PhaseErrors;

import compiler.common.report.CompilerError;

/**
 * Created by gregor on 20. 03. 2016.
 */
public class LexAnError extends CompilerError {
	/**
	 * Compiler error of unspecified kind.
	 *
	 * @param message Error message.
	 */
	public LexAnError(String message) {
		super("[LexAn]" + message);
	}
}
