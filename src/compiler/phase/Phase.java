package compiler.phase;

import compiler.Task;
import compiler.common.logger.Logger;

/**
 * @author sliva
 */
public abstract class Phase implements AutoCloseable {

	protected boolean logging;
	protected Task task;

	/**
	 * The logger object used to produce the log of this phase (or
	 * <code>null</code> if logging has not been requested).
	 */
	protected final Logger logger;

	public Phase(Task task, String phaseName) {
		this.task = task;
		this.logging = this.task.loggedPhases.contains(phaseName);
		if (logging) {
			logger = new Logger(this.task.xmlFName + "." + phaseName + ".xml",
								this.task.xslDName + "/" + phaseName + ".xsl");
		} else {
			logger = null;
		}
	}

	public Phase(Task task, String phaseName, boolean xmlLogging) {
		this.task = task;
		this.logging = this.task.loggedPhases.contains(phaseName);
		if (logging && xmlLogging) {
			logger = new Logger(this.task.xmlFName + "." + phaseName + ".xml",
								this.task.xslDName + "/" + phaseName + ".xsl");
		} else {
			logger = null;
		}
	}

	@Override
	public void close() {
		if (logger != null) {
			logger.close();
		}
	}

}
