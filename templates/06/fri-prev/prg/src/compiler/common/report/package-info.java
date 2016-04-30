/**
 * Printing out an info, debug, warning and error messages.
 * 
 * <p>
 * All messages (except the banner) that the compiler must print out must be
 * sent through package {@link compiler.common.report}.
 * </p>
 * 
 * <ul>
 * 
 * <li>
 * <p>
 * Info messages are used for reporting certain issues that in the compiler
 * writer's humble opinion should be known to programmers. All these messages
 * are printed out using several methods
 * {@link compiler.common.report.Report#info(String) Report.info}.
 * </p>
 * 
 * <p>
 * These messages are automatically prefixed by <code>:-)</code> and have no
 * other effect rather than annoying the programmer, so their amount should be
 * kept at absolute minimum.
 * </p>
 * 
 * <p>
 * Example:
 * </p>
 * <code>import compiler.common.report;</code><br>
 * <code>Report.info("Every program can be reduced to a one-line bug.");</code>
 * </li>
 * 
 * <li>
 * <p>
 * Debug messages are used for printing out debug information. All these
 * messages are printed out using several methods
 * {@link compiler.common.report.Report#debug(String) Report.debug}.
 * </p>
 * 
 * <p>
 * These messages are automatically prefixed by <code>:-]</code> and have no
 * other effect rather than giving the impression that a compiler writer
 * understands the compiler behavior but no amount of these messages can improve
 * the situation.
 * </p>
 * 
 * <p>
 * Example:
 * </p>
 * <code>import compiler.common.report;</code><br>
 * <code>Report.debug("Testing can show the presence of bugs, but not their absence.");</code>
 * </li>
 * 
 * <li>
 * <p>
 * Warning messages are used for reporting issues that the programmer should
 * think about before running the compiler on the same source program again. All
 * these messages are printed out using several methods
 * {@link compiler.common.report.Report#warning(String) Report.warning}.
 * </p>
 * 
 * <p>
 * These messages are automatically prefixed by <code>:-o</code>. The
 * compilation continues, but at the end the compiler prints out a warning that
 * some warnings have been printed out to further annoy the programmer.
 * </p>
 * 
 * <p>
 * Example:
 * </p>
 * <code>import compiler.common.report;</code><br>
 * <code>Report.warning("There is always one more bug.");</code></li>
 * 
 * <li>
 * <p>
 * Error messages are used when the compiler realizes it cannot possibly produce
 * a meaningful translation of the source program. Error messages are printed
 * out by throwing an exception of class
 * {@link compiler.common.report.CompilerError} or one of its subclasses. These
 * exceptions (errors, actually) are caught only at the end of method
 * {@link compiler.Main#main(String[]) compiler.Main.main}.
 * </p>
 * 
 * <p>
 * These messages are automatically prefixed by <code>:-(</code>. The
 * compilation terminates with exit code 1 and no usefull output can be expected
 * (including the error message).
 * </p>
 * 
 * <p>
 * Example:
 * </p>
 * <code>import compiler.common.report;</code><br>
 * <code>new CompilerError("Shit happens.")</code></li>
 * 
 * </ul>
 * 
 * @author sliva
 */
package compiler.common.report;
