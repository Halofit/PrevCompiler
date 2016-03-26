package compiler;

import compiler.common.report.*;
import compiler.phase.lexan.*;

/**
 * The compiler's entry point.
 * 
 * @author sliva
 */
public class Main {

	/**
	 * The compiler's entry point: it parses the command line and triggers the
	 * compilation.
	 * 
	 * @param args
	 *            Command line arguments.
	 */
	public static void main(String args[]) {
		// OK, start at the very beginning.
		System.out.println("This is PREV compiler (2016):");

		try {
			// Parse the command line.
			Task task = new Task(args);

			// Carry out the compilation up to the specified phase.
			while (true) {

				// ***** Lexical analysis. *****
				if (task.phase.equals("lexan")) {
					LexAn lexAn = new LexAn(task);
					while (lexAn.lexAn().token != Symbol.Token.EOF) {
					}
					lexAn.close();
					break;
				}

				break;
			}
		} catch (CompilerError errorReport) {
			// As dead as a dodo. Print error message and signal error.
			System.err.println(":-( " + errorReport.getMessage());
			System.exit(1);
		}

		if (Report.getNumWarnings() > 0) {
			// There is still room for improvement.
			Report.warning("Have you seen all warning messages?");
			System.exit(0);
		} else {
			// Let's hope it ever comes this far.
			Report.info("Done.");
			System.exit(0);
		}
	}

}
