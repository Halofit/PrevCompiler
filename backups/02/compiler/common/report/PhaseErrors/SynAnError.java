package compiler.common.report.PhaseErrors;

import compiler.common.report.CompilerError;

/**
 * Created by gregor on 20. 03. 2016.
 */
public class SynAnError extends CompilerError {
	/**
	 * Compiler error of unspecified kind.
	 *
	 * @param message Error message.
	 */
	public SynAnError(String message) {
		super("[SynAn]" + message);
	}
}
