package compiler.common.report;

import compiler.common.logger.*;

/**
 * Denotes the position of a text within a file.
 * 
 * @author sliva
 */
public class Position implements Loggable {

	/** The name of the file containing the first character of text. */
	private final String begFileName;

	/** The line of the first character of the text. */
	private final int begLine;

	/** The column of the first character of the text. */
	private final int begColumn;

	/** The name of the file containing the last character of the text. */
	private final String endFileName;

	/** The line of the last character of the text. */
	private final int endLine;

	/** The column of the last character of the text. */
	private final int endColumn;

	/**
	 * Constructs a new position denoting a single character.
	 * 
	 * @param fileName
	 *            The name of the file containing the text.
	 * @param line
	 *            The line of the character.
	 * @param column
	 *            The column of the character.
	 */
	public Position(String fileName, int line, int column) {
		this(fileName, line, column, fileName, line, column);
	}

	/**
	 * Constructs a new position denoting a sequence of characters.
	 * 
	 * @param begFileName
	 *            The name of the file containing the first character of the
	 *            text.
	 * @param begLine
	 *            The line of the first character of the text.
	 * @param begColumn
	 *            The column of the first character of the text.
	 * @param endFileName
	 *            The name of the file containing the last character of the
	 *            text.
	 * @param endLine
	 *            The line of the last character of the text.
	 * @param endColumn
	 *            The column of the last character of the text.
	 */
	public Position(String begFileName, int begLine, int begColumn, String endFileName, int endLine, int endColumn) {
		this.begFileName = begFileName;
		this.begLine = begLine;
		this.begColumn = begColumn;
		this.endFileName = endFileName;
		this.endLine = endLine;
		this.endColumn = endColumn;
	}

	/**
	 * Constructs a new position from a specified position.
	 * 
	 * @param position
	 *            The specified position.
	 */
	public Position(Position position) {
		this.begFileName = position.begFileName;
		this.begLine = position.begLine;
		this.begColumn = position.begColumn;
		this.endFileName = position.endFileName;
		this.endLine = position.endLine;
		this.endColumn = position.endColumn;
	}

	/**
	 * Constructs a new position spanning from the first to the last position.
	 * 
	 * @param begPosition
	 *            The first position.
	 * @param endPosition
	 *            The last position.
	 */
	public Position(Position begPosition, Position endPosition) {
		this.begFileName = begPosition.begFileName;
		this.begLine = begPosition.begLine;
		this.begColumn = begPosition.begColumn;
		this.endFileName = begPosition.endFileName;
		this.endLine = endPosition.endLine;
		this.endColumn = endPosition.endColumn;
	}

	@Override
	public String toString() {
		return "[" + begLine + "." + begColumn + "--" + endLine + "." + endColumn + "]";
	}

	@Override
	public void log(Logger logger) {
		if (logger == null)
			return;
		logger.begElement("position");
		logger.addAttribute("begsource", begFileName);
		logger.addAttribute("begLine", Integer.toString(begLine));
		logger.addAttribute("begColumn", Integer.toString(begColumn));
		logger.addAttribute("endsource", endFileName);
		logger.addAttribute("endLine", Integer.toString(endLine));
		logger.addAttribute("endColumn", Integer.toString(endColumn));
		logger.endElement();
	}

}
