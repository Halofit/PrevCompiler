package compiler;

import java.util.*;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.frg.*;

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
	private static final String allPhases = "(lexan|synan|abstr|seman|frames|imcode)";

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

		for (int argc = 0; argc < args.length; argc++) {
			if (args[argc].startsWith("-")) {
				// This is an option.

				if (args[argc].startsWith("--phase=")) {
					if (phase == "") {
						phase = args[argc].replaceFirst("--phase=", "");
						if (!phase.matches(allPhases)) {
							Report.warning("Illegal compilation phase specified by '" + args[argc] + "' ignored.");
							phase = "";
						}
					} else
						Report.warning("Phase already specified, option '" + args[argc] + "' ignored.");
					continue;
				}

				if (args[argc].startsWith("--loggedphases=")) {
					if (loggedPhases == "") {
						loggedPhases = args[argc].replaceFirst("--loggedphases=", "");
						if (!loggedPhases.matches(allPhases + "(," + allPhases + ")*")) {
							Report.warning("Illegal compilation phases specified by '" + args[argc] + "' ignored.");
							loggedPhases = "";
						}
					} else
						Report.warning("Logged phases already specified, option '" + args[argc] + "' ignored.");
					continue;
				}

				if (args[argc].startsWith("--xsldir=")) {
					if (xslDName == "") {
						xslDName = args[argc].replaceFirst("--xsldir=", "");
						if (xslDName.equals("")) {
							Report.warning("No XSL directory specified by '" + args[argc] + "'; option ignored.");
							loggedPhases = "";
						}
					} else
						Report.warning("XSL directory already specified, option '" + args[argc] + "' ignored.");
					continue;
				}

				Report.warning("Unknown command line option '" + args[argc] + "'.");
			} else {
				// This is a file name.
				if (srcFName == "") {
					srcFName = args[argc];
					xmlFName = args[argc].replaceFirst(".prev$", "");
				} else
					Report.warning("Filename '" + args[argc] + "' ignored.");
			}
		}

		this.srcFName = srcFName;
		this.xmlFName = xmlFName;
		this.xslDName = xslDName;
		this.loggedPhases = loggedPhases;
		this.phase = phase;

		// Check the source file name.
		if (this.srcFName == "")
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
    public HashMap<String, Fragment> fragments = new HashMap<String, Fragment>();
    
}
