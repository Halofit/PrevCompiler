package compiler.phase;

import compiler.*;
import compiler.common.logger.*;

/**
 * @author sliva
 */
public abstract class Phase implements AutoCloseable {

	protected Task task;

	/**
	 * The logger object used to produce the log of this phase (or
	 * <code>null</code> if logging has not been requested).
	 */
	protected final Logger logger;

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
