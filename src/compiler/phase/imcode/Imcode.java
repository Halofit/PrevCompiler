package compiler.phase.imcode;

import compiler.*;
import compiler.phase.*;

/**
 * Intermediate code generation.
 * 
 * @author sliva
 */
public class Imcode extends Phase {

	/**
	 * Constructs intermediate code generation phase.
	 * 
	 * @param task
	 *            The parameters and internal data of the compilation process.
	 */
	public Imcode(Task task) {
		super(task, "imcode");
	}

	/**
	 * Terminates intermediate code generation. If logging has been requested,
	 * this method produces the report by closing the logger.
	 */
	@Override
	public void close() {
		if (logger != null)
			(new ImcodeToXML(logger, true, task.prgAttrs)).visit(task.prgAST);
		super.close();
	}

}
