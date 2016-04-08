package compiler.phase.seman;

import compiler.*;
import compiler.phase.*;

/**
 * Semantic analyzer.
 * 
 * <p>
 * Semantic analyzer performs all computations using specialized
 * {@link compiler.data.ast.code.Visitor}s. Visitors are implemented as separate
 * classes, but all attributes of the abstract syntax tree are statically
 * declared here.
 *
 * @author sliva
 */
public class SemAn extends Phase {

	/**
	 * Constructs semantic analyzer.
	 */
	public SemAn(Task task) {
		super(task, "seman");
	}

	/**
	 * Terminates semantic analysis. If logging has been requested, this method
	 * produces the report by closing the logger.
	 */
	@Override
	public void close() {
		if (logger != null)
			(new SemAnToXML(logger, true, task.prgAttrs)).visit(task.prgAST);
		super.close();
	}

}
