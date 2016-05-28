package compiler;

import compiler.common.report.CompilerError;
import compiler.common.report.Report;
import compiler.data.ast.Program;
import compiler.data.ast.attr.Attributes;
import compiler.data.codegen.InstructionSet;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.Fragment;
import compiler.data.liveness.InterferenceGraph;

import java.util.HashMap;

/**
 * The parameters and internal data of the compilation process.
 * 
 * @author sliva
 */
public class Task {

	/** The name of the source file. */
	public final String srcFName;

	/** The stem of the name of the log files. */
	public final String xmlFName;

	/** The name of the directory containing .xsl files. */
	public final String xslDName;

	/** A regular expression describing all phases of the compiler. */
	private static final String allPhases = "(lexan|synan|abstr|seman|frames|imcode|lincode|codegen|liveness|regalloc)";

	/** A list of phases logging should be performed for. */
	public final String loggedPhases;

	/** The last phase of the compiler to be performed. */
	public final String phase;

	/**
	 * Construct a new compilation task based on the command-line agruments.
	 * 
	 * @param args
	 *            Command-line arguments.
	 */
	public Task(String[] args) {

		String srcFName = "";
		String xmlFName = "";
		String xslDName = "";
		String loggedPhases = "";
		String phase = "";

		for (String arg : args) {
			if (arg.startsWith("-")) {
				// This is an option.

				if (arg.startsWith("--phase=")) {
					if (phase.equals("")) {
						phase = arg.replaceFirst("--phase=", "");
						if (!phase.matches(allPhases)) {
							Report.warning("Illegal compilation phase specified by '" + arg + "' ignored.");
							phase = "";
						}
					} else {
						Report.warning("Phase already specified, option '" + arg + "' ignored.");
					}
					continue;
				}

				if (arg.startsWith("--loggedphases=")) {
					if (loggedPhases.equals("")) {
						loggedPhases = arg.replaceFirst("--loggedphases=", "");
						if (!loggedPhases.matches(allPhases + "(," + allPhases + ")*")) {
							Report.warning("Illegal compilation phases specified by '" + arg + "' ignored.");
							loggedPhases = "";
						}
					} else {
						Report.warning("Logged phases already specified, option '" + arg + "' ignored.");
					}
					continue;
				}

				if (arg.startsWith("--xsldir=")) {
					if (xslDName.equals("")) {
						xslDName = arg.replaceFirst("--xsldir=", "");
						if (xslDName.equals("")) {
							Report.warning("No XSL directory specified by '" + arg + "'; option ignored.");
							loggedPhases = "";
						}
					} else {
						Report.warning("XSL directory already specified, option '" + arg + "' ignored.");
					}
					continue;
				}

				Report.warning("Unknown command line option '" + arg + "'.");
			} else {
				// This is a file name.
				if (srcFName.equals("")) {
					srcFName = arg;
					xmlFName = arg.replaceFirst(".prev$", "");
				} else {
					Report.warning("Filename '" + arg + "' ignored.");
				}
			}
		}

		this.srcFName = srcFName;
		this.xmlFName = xmlFName;
		this.xslDName = xslDName;
		this.loggedPhases = loggedPhases;
		this.phase = phase;

		// Check the source file name.
		if (this.srcFName.equals(""))
			throw new CompilerError("Source file name not specified.");
	}

	/**
	 * The abstract syntax tree representing the program that is being compiled.
	 */
	public Program prgAST = null;

	/**
	 * The attributes of the AST nodes.
	 */
	public Attributes prgAttrs = new Attributes();
	
	/**
	 * Fragments of the program (indexed by entry labels).
	 */
    public HashMap<String, Fragment> fragments = new HashMap<>();

	public HashMap<CodeFragment, InstructionSet> fragInstrs = new HashMap<>();
	public HashMap<CodeFragment, InterferenceGraph> intfGraph = new HashMap<>();
    
}
