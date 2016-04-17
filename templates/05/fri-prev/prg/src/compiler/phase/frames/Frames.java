package compiler.phase.frames;

import compiler.*;
import compiler.phase.*;

/**
 * Evaluation of stack frames and variable accesses.
 * 
 * @author sliva
 */
public class Frames extends Phase {

	// public static Attribute<Typ, Integer> typSizeAttr = new Attribute<Typ,
	// Integer>();

	/**
	 * Constructs frame construction.
	 * 
	 * @param task
	 *            The parameters and internal data of the compilation process.
	 */
	public Frames(Task task) {
		super(task, "frames");
	}

	/**
	 * Terminates frame construction. If logging has been requested, this method
	 * produces the report by closing the logger.
	 */
	@Override
	public void close() {
		if (logger != null)
			(new FramesToXML(logger, true, task.prgAttrs)).visit(task.prgAST);
		super.close();
	}

}
