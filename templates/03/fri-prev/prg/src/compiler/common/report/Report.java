package compiler.common.report;

/**
 * Static methods for printing out info, debug, and warning messages.
 * 
 * @author sliva
 *
 */
public class Report {

	/**
	 * Prints out an info message.
	 * 
	 * @param message
	 *            The info message.
	 */
	public static void info(String message) {
		System.out.print(":-) ");
		System.out.println(message);
	}

	/**
	 * Prints out an info message.
	 * 
	 * @param position
	 *            The position in the source file the info message relates to.
	 * @param message
	 *            The info message.
	 */
	public static void info(Position position, String message) {
		System.out.print(":-) " + position.toString() + " ");
		System.out.println(message);
	}

	/**
	 * Prints out a debug message.
	 * 
	 * @param message
	 *            The debug message.
	 */
	public static void debug(String message) {
		System.out.print(":-[ ");
		System.out.println(message);
	}

	/**
	 * Prints out a debug message.
	 * 
	 * @param position
	 *            The position in the source file the debug message relates to.
	 * @param message
	 *            The debug message.
	 */
	public static void debug(Position position, String message) {
		System.out.print(":-[ " + position.toString() + " ");
		System.out.println(message);
	}

	/** Number of warnings printed out. */
	private static int numWarnings = 0;

	/**
	 * Returns the number of warnings printed out so far.
	 * 
	 * @return The number of warnings printed out so far.
	 */
	public static int getNumWarnings() {
		return numWarnings;
	}

	/**
	 * Prints out a warning message.
	 * 
	 * @param message
	 *            The warning message.
	 */
	public static void warning(String message) {
		System.err.print(":-o ");
		System.err.println(message);
		numWarnings++;
	}

	/**
	 * Prints out a warning message relating to a specific position in the
	 * source file.
	 * 
	 * @param position
	 *            The position in the source file the warning message relates
	 *            to.
	 * @param message
	 *            The warning message.
	 */
	public static void warning(Position position, String message) {
		System.err.print(":-o " + position.toString() + " ");
		System.err.println(message);
		numWarnings++;
	}

}
