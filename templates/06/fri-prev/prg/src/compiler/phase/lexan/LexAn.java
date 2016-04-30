package compiler.phase.lexan;

import java.io.*;

import compiler.*;
import compiler.common.report.*;
import compiler.phase.*;

/**
 * The lexical analyzer.
 * 
 * @author sliva
 */
public class LexAn extends Phase {

	/** The source file. */
	private FileReader srcFile;

	/**
	 * The last character read from the source file (or <code>-1</code> if the
	 * end of file has already been reached). If
	 * {@link compiler.phase.lexan.LexAn#bufferCharValid bufferCharValid} equals
	 * <code>false</code>, the character has already been used by the lexical
	 * analyzer.
	 */
	private int bufferChar;

	/**
	 * A flag denoting whether a character stored in
	 * {@link compiler.phase.lexan.LexAn#bufferChar bufferChar} has already been
	 * used by the lexical analyzer.
	 */
	private boolean bufferCharValid;

	/**
	 * The line of the character stored in
	 * {@link compiler.phase.lexan.LexAn#bufferChar bufferChar}.
	 */
	private int bufferCharLine;

	/**
	 * The column of the character stored in
	 * {@link compiler.phase.lexan.LexAn#bufferChar bufferChar}.
	 */
	private int bufferCharColumn;

	/**
	 * The symbol that has already been recognized but not yet returned - used
	 * to hold the unsigned integer constant if a signed integer constant has
	 * been recognized and the sign must be returned.
	 */
	private Symbol symbolBuffer;

	/**
	 * Constructs a new lexical analyzer.
	 * 
	 * Opens the source file and prepares the buffer. If logging is requested,
	 * sets up the logger.
	 * 
	 * @param task
	 *            The parameters and internal data of the compilation process.
	 */
	public LexAn(Task task) {
		super(task, "lexan");

		// Open the source file.
		try {
			srcFile = new FileReader(this.task.srcFName);
		} catch (FileNotFoundException ex) {
			throw new CompilerError("Source file '" + this.task.srcFName + "' not found.");
		}

		// Prepare the buffer.
		bufferChar = 10 /* LF */;
		bufferCharLine = 0;
		bufferCharColumn = 0;
		bufferCharValid = false;
		symbolBuffer = null;
	}

	/**
	 * Terminates lexical analysis. Closes the source file and, if logging has
	 * been requested, this method produces the report by closing the logger.
	 */
	@Override
	public void close() {
		// Close the source file.
		if (srcFile != null) {
			try {
				srcFile.close();
			} catch (IOException ex) {
				Report.warning("Source file '" + task.srcFName + "' cannot be closed.");
			}
		}
		super.close();
	}

	/**
	 * Ensures that the buffer contents is valid. If it is not, it reads the
	 * next character from the source file and updates the position. If the next
	 * character cannot be read it prints a warning message and assumes the end
	 * of file.
	 */
	private void ensureValidBuffer() {
		if (!bufferCharValid) {
			try {
				if (bufferChar == 10 /* LF */) {
					bufferCharLine++;
					bufferCharColumn = 0;
				}
				bufferChar = srcFile.read();
				bufferCharValid = true;
				bufferCharColumn++;
			} catch (IOException ex) {
				Report.warning("Error reading source file '" + task.srcFName + "'; assuming end of file.");
				bufferCharValid = true;
				bufferChar = -1;
			}
		}
	}

	/**
	 * Returns the next lexical symbol from the source file.
	 * 
	 * @return The next lexical symbol.
	 */
	public Symbol lexAn() {

		if (symbolBuffer != null) {
			Symbol symbol = symbolBuffer;
			symbolBuffer = null;
			return log(symbol);
		}

		while (true) {
			// Skip the white space.
			ensureValidBuffer();
			while (Character.isWhitespace(bufferChar)) {
				bufferCharValid = false;
				ensureValidBuffer();
			}

			// The big switch.
			switch (bufferChar) {
			case -1:
				// The end of the source file.
				return new Symbol(Symbol.Token.EOF, new Position(task.srcFName, bufferCharLine, bufferCharColumn));

			// Comments.
			case '#':
				while ((bufferChar != 10 /* LF */) && (bufferChar != -1 /* EOF */)) {
					bufferCharValid = false;
					ensureValidBuffer();
				}
				bufferCharValid = false;
				break;

			// Symbols.
			case '&':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.AND, new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case '=':
				bufferCharValid = false;
				ensureValidBuffer();
				switch (bufferChar) {
				case '=':
					bufferCharValid = false;
					return log(new Symbol(Symbol.Token.EQU, new Position(task.srcFName, bufferCharLine,
							bufferCharColumn - 1, task.srcFName, bufferCharLine, bufferCharColumn)));
				default:
					return log(new Symbol(Symbol.Token.ASSIGN,
							new Position(task.srcFName, bufferCharLine, bufferCharColumn - 1)));
				}
			case '}':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.CLOSING_BRACE,
						new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case ']':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.CLOSING_BRACKET,
						new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case ')':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.CLOSING_PARENTHESIS,
						new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case ':':
				bufferCharValid = false;
				return log(
						new Symbol(Symbol.Token.COLON, new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case ',':
				bufferCharValid = false;
				return log(
						new Symbol(Symbol.Token.COMMA, new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case '.':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.DOT, new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case '/':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.DIV, new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case '>':
				bufferCharValid = false;
				ensureValidBuffer();
				switch (bufferChar) {
				case '=':
					bufferCharValid = false;
					return log(new Symbol(Symbol.Token.GEQ, new Position(task.srcFName, bufferCharLine,
							bufferCharColumn - 1, task.srcFName, bufferCharLine, bufferCharColumn)));
				default:
					return log(new Symbol(Symbol.Token.GTH,
							new Position(task.srcFName, bufferCharLine, bufferCharColumn - 1)));
				}
			case '<':
				bufferCharValid = false;
				ensureValidBuffer();
				switch (bufferChar) {
				case '=':
					bufferCharValid = false;
					return log(new Symbol(Symbol.Token.LEQ, new Position(task.srcFName, bufferCharLine,
							bufferCharColumn - 1, task.srcFName, bufferCharLine, bufferCharColumn)));
				default:
					return log(new Symbol(Symbol.Token.LTH,
							new Position(task.srcFName, bufferCharLine, bufferCharColumn - 1)));
				}
			case '@':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.MEM, new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case '%':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.MOD, new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case '*':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.MUL, new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case '!':
				bufferCharValid = false;
				ensureValidBuffer();
				switch (bufferChar) {
				case '=':
					bufferCharValid = false;
					return log(new Symbol(Symbol.Token.NEQ, new Position(task.srcFName, bufferCharLine,
							bufferCharColumn - 1, task.srcFName, bufferCharLine, bufferCharColumn)));
				default:
					return log(new Symbol(Symbol.Token.NOT,
							new Position(task.srcFName, bufferCharLine, bufferCharColumn - 1)));
				}
			case '{':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.OPENING_BRACE,
						new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case '[':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.OPENING_BRACKET,
						new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case '(':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.OPENING_PARENTHESIS,
						new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case '|':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.OR, new Position(task.srcFName, bufferCharLine, bufferCharColumn)));
			case '^':
				bufferCharValid = false;
				return log(new Symbol(Symbol.Token.VAL, new Position(task.srcFName, bufferCharLine, bufferCharColumn)));

			// Character constants.
			case '\'':
				Symbol char_constant = null; {
				StringBuffer lexemeBuffer = new StringBuffer();
				int begLine = bufferCharLine;
				int begColumn = bufferCharColumn;
				int endLine = bufferCharLine;
				int endColumn = bufferCharColumn;
				// states:
				// 0 : nothing has been read so far
				// 1 : the opening ' has been read
				// 2 : the character has been read
				// 3 : the closing ' has been read
				// 4 : \ has been read, the escape sequence follows
				int state = 0;
				while (char_constant == null) {
					switch (state) {
					case 0:
						switch (bufferChar) {
						case '\'':
							state = 1;
							break;
						default:
							state = 1;
							Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
									"Missing opening \' inserted.");
							lexemeBuffer.append('\'');
						}
						break;
					case 1:
						switch (bufferChar) {
						case '\'':
							Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
									"Empty character constant.");
							state = 3;
							break;
						case '\\':
							state = 4;
							break;
						case -1:
							Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
									"End-of-file within a character constant.");
							state = 2;
							break;
						default:
							if (!((32 <= bufferChar) && (bufferChar <= 126)))
								Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
										"Illegal character with ASCII code " + bufferChar
												+ " within a character constant.");
							state = 2;
						}
						break;
					case 2:
						switch (bufferChar) {
						case '\'':
							state = 3;
							break;
						default:
							Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
									"Missing closing \' inserted.");
							state = 3;
						}
						break;
					case 3:
						String lexeme = lexemeBuffer.toString();
						Position position = new Position(task.srcFName, begLine, begColumn, task.srcFName, endLine,
								endColumn);
						char_constant = new Symbol(Symbol.Token.CONST_CHAR, lexeme, position);
						break;
					case 4:
						switch (bufferChar) {
						case 'n':
						case 't':
						case '\'':
						case '\"':
						case '\\':
							state = 2;
							break;
						default:
							Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
									"Illegal escape sequence within a character constant.");
							state = 2;
							break;
						}
						break;
					}
					if ((bufferChar != -1) && (char_constant == null)) {
						lexemeBuffer.append((char) bufferChar);
						endLine = bufferCharLine;
						endColumn = bufferCharColumn;
						bufferCharValid = false;
						ensureValidBuffer();
					}
				}
			}
				return log(char_constant);

			// String constants.
			case '\"':
				Symbol string_constant = null; {
				StringBuffer lexemeBuffer = new StringBuffer();
				int begLine = bufferCharLine;
				int begColumn = bufferCharColumn;
				int endLine = bufferCharLine;
				int endColumn = bufferCharColumn;
				// states:
				// 0 : nothing has been read so far
				// 1 : the opening " has been read and some more characters
				// 2 : the closing " has been read
				// 3 : \ has been read, the escape sequence follows
				int state = 0;
				while (string_constant == null) {
					switch (state) {
					case 0:
						switch (bufferChar) {
						case '\"':
							state = 1;
							break;
						default:
							state = 1;
							Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
									"Missing opening \" inserted.");
							lexemeBuffer.append('\"');
						}
						break;
					case 1:
						switch (bufferChar) {
						case '\"':
							state = 2;
							break;
						case '\\':
							state = 3;
							break;
						case -1:
							Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
									"End-of-file within a string constant.");
							Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
									"Missing closing \" inserted.");
							lexemeBuffer.append('\"');
							state = 2;
							break;
						default:
							if (!((32 <= bufferChar) && (bufferChar <= 126)))
								Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
										"Illegal character with ASCII code " + bufferChar
												+ " within a string constant.");
							state = 1;
						}
						break;
					case 2:
						String lexeme = lexemeBuffer.toString();
						Position position = new Position(task.srcFName, begLine, begColumn, task.srcFName, endLine,
								endColumn);
						string_constant = new Symbol(Symbol.Token.CONST_STRING, lexeme, position);
						break;
					case 3:
						switch (bufferChar) {
						case 'n':
						case 't':
						case '\'':
						case '\"':
						case '\\':
							state = 1;
							break;
						default:
							Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
									"Illegal escape sequence within a string constant.");
							state = 1;
						}
						break;
					}
					if ((bufferChar != -1) && (string_constant == null)) {
						lexemeBuffer.append((char) bufferChar);
						endLine = bufferCharLine;
						endColumn = bufferCharColumn;
						bufferCharValid = false;
						ensureValidBuffer();
					}
				}
			}
				return log(string_constant);

			// Constants (but not chars or strings), type names, keywords,
			// identifiers, and unexpected characters.
			default:
				if (Character.isDigit(bufferChar) || (bufferChar == '+') || (bufferChar == '-')) {
					// An integer constant.

					// Read the entire lexeme.
					StringBuffer lexemeBuffer = new StringBuffer();
					int begLine = bufferCharLine;
					int begColumn = bufferCharColumn;
					int endLine = bufferCharLine;
					int endColumn = bufferCharColumn;
					do {
						lexemeBuffer.append((char) bufferChar);
						endLine = bufferCharLine;
						endColumn = bufferCharColumn;
						bufferCharValid = false;
						ensureValidBuffer();
					} while (Character.isDigit(bufferChar));

					if (lexemeBuffer.length() > 1) {
						try {
							new Long(lexemeBuffer.toString());
						} catch (NumberFormatException ex) {
							Report.warning(
									new Position(task.srcFName, begLine, begColumn, task.srcFName, endLine, endColumn),
									"Integer constant " + lexemeBuffer.toString() + " is out of range.");
							for (int i = 0; i < lexemeBuffer.length(); i++) {
								if (Character.isDigit(lexemeBuffer.charAt(i)))
									lexemeBuffer.setCharAt(i, '0');
							}
						}
					}

					if (Character.isDigit(lexemeBuffer.charAt(0))) {
						if (Character.isAlphabetic(bufferChar) || (bufferChar == '_'))
							Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
									"Character '" + (char) bufferChar + "' should not follow integer constant.");

						String lexeme = lexemeBuffer.toString();
						Position position = new Position(task.srcFName, begLine, begColumn, task.srcFName, endLine,
								endColumn);
						return log(new Symbol(Symbol.Token.CONST_INTEGER, lexeme, position));
					} else {
						if (lexemeBuffer.length() > 1) {
							String lexeme = lexemeBuffer.toString().substring(1);
							Position position = new Position(task.srcFName, begLine, begColumn + 1, task.srcFName,
									endLine, endColumn);
							symbolBuffer = new Symbol(Symbol.Token.CONST_INTEGER, lexeme, position);

							if (Character.isAlphabetic(bufferChar) || (bufferChar == '_'))
								Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
										"Character '" + (char) bufferChar + "' should not follow integer constant.");
						}

						switch (lexemeBuffer.charAt(0)) {
						case '+':
							return log(new Symbol(Symbol.Token.ADD, new Position(task.srcFName, begLine, begColumn)));
						case '-':
							return log(new Symbol(Symbol.Token.SUB, new Position(task.srcFName, begLine, begColumn)));
						default:
							throw new InternalCompilerError();
						}
					}
				}

				if (Character.isAlphabetic(bufferChar) || (bufferChar == '_')) {
					// A constant, type name, keyword or an identifier.

					// Read the entire lexeme.
					StringBuffer lexemeBuffer = new StringBuffer();
					int begLine = bufferCharLine;
					int begColumn = bufferCharColumn;
					int endLine = bufferCharLine;
					int endColumn = bufferCharColumn;
					do {
						lexemeBuffer.append((char) bufferChar);
						endLine = bufferCharLine;
						endColumn = bufferCharColumn;
						bufferCharValid = false;
						ensureValidBuffer();
					} while (Character.isAlphabetic(bufferChar) || Character.isDigit(bufferChar)
							|| (bufferChar == '_'));
					String lexeme = lexemeBuffer.toString();
					Position position = new Position(task.srcFName, begLine, begColumn, task.srcFName, endLine,
							endColumn);

					// Constants.
					if (lexeme.equals("true") || lexeme.equals("false"))
						return log(new Symbol(Symbol.Token.CONST_BOOLEAN, lexeme, position));
					if (lexeme.equals("null"))
						return log(new Symbol(Symbol.Token.CONST_NULL, position));
					if (lexeme.equals("none"))
						return log(new Symbol(Symbol.Token.CONST_NONE, position));

					// Type names.
					if (lexeme.equals("integer"))
						return log(new Symbol(Symbol.Token.INTEGER, position));
					if (lexeme.equals("boolean"))
						return log(new Symbol(Symbol.Token.BOOLEAN, position));
					if (lexeme.equals("char"))
						return log(new Symbol(Symbol.Token.CHAR, position));
					if (lexeme.equals("string"))
						return log(new Symbol(Symbol.Token.STRING, position));
					if (lexeme.equals("void"))
						return log(new Symbol(Symbol.Token.VOID, position));

					// Keywords.
					if (lexeme.equals("arr"))
						return log(new Symbol(Symbol.Token.ARR, position));
					if (lexeme.equals("else"))
						return log(new Symbol(Symbol.Token.ELSE, position));
					if (lexeme.equals("end"))
						return log(new Symbol(Symbol.Token.END, position));
					if (lexeme.equals("for"))
						return log(new Symbol(Symbol.Token.FOR, position));
					if (lexeme.equals("fun"))
						return log(new Symbol(Symbol.Token.FUN, position));
					if (lexeme.equals("if"))
						return log(new Symbol(Symbol.Token.IF, position));
					if (lexeme.equals("then"))
						return log(new Symbol(Symbol.Token.THEN, position));
					if (lexeme.equals("ptr"))
						return log(new Symbol(Symbol.Token.PTR, position));
					if (lexeme.equals("rec"))
						return log(new Symbol(Symbol.Token.REC, position));
					if (lexeme.equals("typ"))
						return log(new Symbol(Symbol.Token.TYP, position));
					if (lexeme.equals("var"))
						return log(new Symbol(Symbol.Token.VAR, position));
					if (lexeme.equals("where"))
						return log(new Symbol(Symbol.Token.WHERE, position));
					if (lexeme.equals("while"))
						return log(new Symbol(Symbol.Token.WHILE, position));

					// Otherwise it is an identifier.
					return log(new Symbol(Symbol.Token.IDENTIFIER, lexeme, position));
				}

				// Otherwise it is an illegal character.
				Report.warning(new Position(task.srcFName, bufferCharLine, bufferCharColumn),
						"Illegal character '" + ((char) bufferChar) + "' ignored.");
				bufferCharValid = false;
			}
		}

	}

	/**
	 * Produces of the log of the symbol.
	 * 
	 * @param symbol
	 *            The symbol.
	 * @return The symbol the log should be produced for.
	 */
	private Symbol log(Symbol symbol) {
		symbol.log(logger);
		return symbol;
	}

}
