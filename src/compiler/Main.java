package compiler;

import compiler.common.report.CompilerError;
import compiler.common.report.Report;
import compiler.phase.abstr.Abstr;
import compiler.phase.codegen.CodeGen;
import compiler.phase.frames.EvalFrameOut;
import compiler.phase.frames.EvalFrames;
import compiler.phase.frames.Frames;
import compiler.phase.imcode.EvalImcode;
import compiler.phase.imcode.Imcode;
import compiler.phase.lexan.LexAn;
import compiler.phase.lexan.Symbol;
import compiler.phase.lincode.BasicBlocks;
import compiler.phase.lincode.LinCode;
import compiler.phase.seman.*;
import compiler.phase.synan.SynAn;

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
		System.out.println();
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

				// ***** Syntax analysis. *****
				SynAn synAn = new SynAn(task);
				task.prgAST = synAn.synAn();
				synAn.close();
				if (task.phase.equals("synan"))
					break;

				// ***** Abstract syntax tree. *****
				Abstr abstr = new Abstr(task);
				abstr.close();
				if (task.phase.equals("abstr"))
					break;

				// ***** Semantic analysis. *****
				SemAn seman = new SemAn(task);
				(new EvalValue(task.prgAttrs)).visit(task.prgAST);
				(new EvalDecl(task.prgAttrs)).visit(task.prgAST);
				(new EvalTyp(task.prgAttrs)).visit(task.prgAST);
				(new EvalMem(task.prgAttrs)).visit(task.prgAST);
				seman.close();
				if (task.phase.equals("seman"))
					break;

				if (Report.getNumWarnings() > 0)
					break;

				// Frames and accesses.
				Frames frames = new Frames(task);
				(new EvalFrames(task.prgAttrs)).visit(task.prgAST);
				(new EvalFrameOut(task.prgAttrs)).visit(task.prgAST);
				frames.close();
				if (task.phase.equals("frames"))
					break;

				
				// Intermediate code generation.
				Imcode imcode = new Imcode(task);
				(new EvalImcode(task.prgAttrs, task.fragments)).visit(task.prgAST);

				//Do basic blocks -> linearisation should have been done here
				BasicBlocks bblocs = new BasicBlocks(task);
				bblocs.transform();


				imcode.close();
				if (task.phase.equals("imcode"))
					break;


				// Linearization of the intermediate code.
				LinCode linCode = new LinCode(task);
				//(new EvalLinCode(task.fragments)).visit(task.prgAST);
				linCode.close();
				if (task.phase.equals("lincode"))
					break;

				CodeGen codeGen = new CodeGen(task);

				if (task.phase.equals("codegen"))
					break;


				break;
			}
		} catch (CompilerError errorReport) {
			System.err.println(errorReport.getMessage());
			System.out.println();
			System.exit(1);
		}

		if (Report.getNumWarnings() > 0) {
			Report.warning("Have you seen all warning messages?");
			System.exit(0);
		} else {
			Report.info("Done.");
			System.exit(0);
		}
	}

}
