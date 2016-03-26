package compiler.phase.abstr;

import compiler.*;
import compiler.phase.*;

/**
 * The construction of the abstract syntax tree.
 * 
 * @author sliva
 */
public class Abstr extends Phase {

	/**
	 * Constructs the abstract syntax tree constructor.
	 */
	public Abstr(Task task) {
		super(task, "abstr");
	}

	/**
	 * Terminates the abstract syntax tree construction. If logging has been
	 * requested, this method produces the report by closing the logger.
	 */
	@Override
	public void close() {
		if (logger != null)
			(new AbstrToXML(logger, true)).visit(task.prgAST);
		super.close();
	}

}
