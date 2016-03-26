package compiler.common.logger;

/**
 * A loggable object. 
 * 
 * @author sliva
 */
public interface Loggable {

	/**
	 * Called by an object to produce a log of itself.
	 * 
	 * @param logger
	 *            The logger the object must log itself to.
	 */
	public void log(Logger logger);

}
