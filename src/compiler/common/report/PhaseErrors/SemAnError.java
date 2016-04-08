package compiler.common.report.PhaseErrors;

import compiler.common.report.CompilerError;

/**
 * Created by gregor on 8. 04. 2016.
 */
public class SemAnError extends CompilerError {

	public SemAnError(String message) {
		super("[SemAn]" + message);
	}
}
