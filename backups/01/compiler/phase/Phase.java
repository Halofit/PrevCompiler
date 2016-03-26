package compiler.phase;

import compiler.*;
import compiler.common.logger.*;

/**
 * An abstract class acting as a template for each individual phase.
 * 
 * @author sliva
 */
public abstract class Phase implements AutoCloseable {

	/** The parameters of the compilation process being carried out. */
	protected Task task;

	/**
	 * The logger object used to produce the log of this phase (or
	 * <code>null</code> if logging has not been requested).
	 */
	protected final Logger logger;

	/**
	 * Constructs a new phase: prepares the logger if requested.
	 * 
	 * @param task
	 *            The parameters of the compilation process being carried out.
	 * @param phaseName
	 *            The name of the actual phase.
	 */
	public Phase(Task task, String phaseName) {
		this.task = task;
		if (this.task.loggedPhases.indexOf(phaseName) != -1) {
			logger = new Logger(this.task.xmlFName + "." + phaseName + ".xml",
					this.task.xslDName + "/" + phaseName + ".xsl");
		} else
			logger = null;
	}

	@Override
	public void close() {
		if (logger != null)
			logger.close();
	}

}
