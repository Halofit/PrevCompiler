package compiler.phase.lincode;

import compiler.*;
import compiler.phase.*;

/**
 * Linearization of the intermediate code.
 * 
 * @author sliva
 */
public class LinCode extends Phase {

	/**
	 * Constructs the phase that performs linearization of the intermediate code.
	 * 
	 * @param task
	 *            The parameters and internal data of the compilation process.
	 */
	public LinCode(Task task) {
		super(task, "lincode");
	}

	/**
	 * Terminates linearization of the intermediate code. If logging has been
	 * requested, this method produces the report by closing the logger.
	 */
	@Override
	public void close() {
		super.close();
	}

}
