package compiler.phase.lexan;

import java.io.*;
import java.util.Arrays;

import compiler.*;
import compiler.common.report.*;
import compiler.common.report.PhaseErrors.LexAnError;
import compiler.phase.*;

/**
 * The lexical analyzer.
 *
 * @author sliva
 */
public class LexAn extends Phase {

	//Debug Logger
	private static final boolean DEBUG = false;

	private static PrintStream debugOut;
	static {
		if(DEBUG) {
			debugOut = System.out;
		}else{
			//If no debugging than define a null stream (similar to printing to /dev/null
			debugOut = new PrintStream(new OutputStream() {
				@Override
				public void write(int b) throws IOException {}
			});
		}
	}


	/**
	 * The source file.
	 */
	private PushbackInputStream src;

	/**
	 * Constructs a new lexical analyzer.
	 * <p>
	 * Opens the source file and prepares the buffer. If logging is requested,
	 * sets up the logger.
	 * <p>
	 * -@param task.srcFName
	 * The name of the source file name.
	 */
	public LexAn(Task task) {
		super(task, "lexan");

		// Open the source file.
		try {
			//srcFile = new FileReader(this.task.srcFName);
			src = new PushbackInputStream(new FileInputStream(this.task.srcFName));
		} catch (FileNotFoundException ex) {
			throw new LexAnError("Source file '" + this.task.srcFName + "' not found.");
		}

		currLine = 1;
		currColumn = 0;
		prevLine = 1;
		prevColumn = 1;

		errorReport = "";
	}

	/**
	 * Terminates lexical analysis. Closes the source file and, if logging has
	 * been requested, this method produces the report by closing the logger.
	 */
	@Override
	public void close() {
		// Close the source file.
		if (src != null) {
			try {
				src.close();
			} catch (IOException ex) {
				Report.warning("Source file '" + task.srcFName + "' cannot be closed.");
			}
		}
		super.close();
	}

	//Lang definitions

	private static final String[] symbols =
			{"+", "&", "=", ":", ",", "}", "]", ")", ".", "/", "==", ">=", ">",
					"<=", "<", "@", "%", "*", "!=", "!", "{", "[", "(", "|", "-", "^"};

	private static final Character[] symbols_starters =  //also includes symbol enders '='
			{'+', '&', '=', ':', ',', '}', ']', ')', '.', '/', '=', '>', '>',
					'<', '<', '@', '%', '*', '!', '!', '{', '[', '(', '|', '-', '^'};


	private enum State {
		NONE, WS, COMM, WORD, STRCON, CHARCON, INTCON, SYMBOL, EOF
	}

	private enum CharId {
		HASH, ALPHA, DIGIT, USCORE, DQUOTE, SQUOTE, SYMB, BSLASH,
		NL, OTHER_WS,
		EOF, ERROR
	}

	private enum EndFlag {
		NONE, END, END_PB, THROW_AWAY
	}

	/**
	 * FSM variables
	 */

	private int currLine;
	private int currColumn;
	private int prevColumn;
	private int prevLine;
	private State state;
	private EndFlag endFlag;
	private StringBuilder lexeme;
	private String errorReport;

	/**
	 * Returns the next lexical symbol from the source file.
	 *
	 * @return The next lexical symbol.
	 */
	public Symbol lexAn() {
		Symbol sym;

		//First of all clear the lexer
		clearLexer();

		try {

			// Read character loop
			while (true) {

				int curr = src.read();
				this.currColumn++;

				Character c;
				CharId id;

				// HACK
				if (curr == -1) {
					c = '\0';
					id = CharId.EOF;
				} else {
					c = (char) (0xFF & curr);
					//Check if character is ascii char
					if((curr & 0x80) != 0) {
						throw new LexAnError( getPosition() + " Non-ascii character with code: <"  + curr + "> !");
					}

					id = identify(c);
				}

				lexeme.append(c);

				debugOut.print(" ");
				debugOut.print(state.name());
				debugOut.print(" ");
				debugOut.print(c.toString().replace("\n", "\\n").replace("\t", "\\t").replace(" ", "<space>"));

				switch (state) {
					case NONE:
						switch (id) {
							case NL:
							case OTHER_WS:
								//handle whitespace
								state = State.WS;
								endFlag = EndFlag.THROW_AWAY;
								break;
							case HASH:
								state = State.COMM;
								break;
							case SYMB:
								state = State.SYMBOL;
								break;
							case SQUOTE:
								state = State.CHARCON;
								break;
							case DQUOTE:
								state = State.STRCON;
								break;
							case USCORE:
							case ALPHA:
								state = State.WORD;
								break;
							case DIGIT:
								state = State.INTCON;
								break;
							case EOF:
								state = State.EOF;
								endFlag = EndFlag.END;
								break;
							case ERROR:
							default:
								throw new LexAnError(getPosition() + " Unknown character: " + c);
						}
						break;
					case COMM:
						switch (id) {
							case NL:
							case EOF:
								endFlag = EndFlag.THROW_AWAY;
								break;
							default:
								state = State.COMM;
						}
						break;
					case CHARCON:
						switch (id) {
							case SQUOTE:
								if (lexeme.charAt(lexeme.length() - 2) != '\\') {
									endFlag = EndFlag.END;
								}
								break;
							case EOF:
								endFlag = EndFlag.END_PB;
								break;
							default:
								state = State.CHARCON;
						}
						break;
					case INTCON:
						switch (id) {
							case DIGIT:
								state = State.INTCON;
								break;
							default:
								endFlag = EndFlag.END_PB;
						}
						break;
					case STRCON:
						switch (id) {
							case DQUOTE:
								if (lexeme.charAt(lexeme.length() - 2) != '\\') {
									endFlag = EndFlag.END;
								}
								break;
							case EOF:
								endFlag = EndFlag.END_PB;
								break;
							default:
								state = State.STRCON;
						}
						break;
					case SYMBOL:
						if(Arrays.asList(symbols).contains(lexeme.toString())){
							endFlag = EndFlag.END;
						} else {
							endFlag = EndFlag.END_PB;
						}
						break;
					case WORD:
						switch (id) {
							case USCORE:
							case DIGIT:
							case ALPHA:
								state = State.WORD;
								break;
							default:
								endFlag = EndFlag.END_PB;
						}
						break;
					default:
						throw new LexAnError(getPosition() + " Lexer: Unreachable statement reached");
				}

				debugOut.println(" --> " + state.name() + "(" + endFlag.name() + ")");

				if (endFlag == EndFlag.END) {
					sym = createSymbol();
					clearLexer();
					break;
				} else if (endFlag == EndFlag.END_PB) {
					lexeme.setLength(lexeme.length() - 1);

					if (curr != -1) {
						src.unread(curr);
					}
					this.currColumn--;

					sym = createSymbol();
					clearLexer();
					break;
				} else if (endFlag == EndFlag.THROW_AWAY) {
					if (id == CharId.NL) {
						this.currColumn = 0;
						this.currLine++;
					}
					clearLexer();
				}
			} // the read char loop

			log(sym);
		} catch (IOException e) {
			throw new LexAnError(getPosition() + " Error reading character!");
		}

		if (sym.token == Symbol.Token.CONST_CHAR) {
			if (!isValidCharConst(sym.lexeme)) {
				throw new LexAnError(getPosition() + " Invalid char constant <" + sym.lexeme + ">");
			}
		}

		if (sym.token == Symbol.Token.CONST_STRING) {
			if (!isValidStringConst(sym.lexeme)) {
				throw new LexAnError(getPosition()
						+ " Invalid string constant <"
						+ (sym.lexeme.length() <= 80 ? sym.lexeme : sym.lexeme.substring(0, 80) + "...")
						+ "> " + errorReport);
			}
		}

		return sym;
	}

	private boolean isValidStringConst(String lexeme) {
		if (lexeme.charAt(0) == '"' && lexeme.charAt(lexeme.length() - 1) == '"') {
			//Dont look at forst and last character
			for (int i = 1; i < lexeme.length() - 1; i++) {
				if (lexeme.charAt(i) >= 32 && lexeme.charAt(i) <= 126) {
					switch (lexeme.charAt(i)) {
						case '\n':
							errorReport = "String constant contains new line";
							return false;
						case '\\':
							//guaranteed not to overflow
							switch (lexeme.charAt(i + 1)) {
								case '\\':
								case '\'':
								case '\"':
								case 't':
								case 'n':
									i++; //Skip the next character as well
									break;
								default:
									errorReport =
											"Unknown escape sequence in string literal <"
													+ lexeme.charAt(i) + lexeme.charAt(i + 1) + ">";
									return false;
							}
							break;

					}
				} else {
					errorReport = "Invalid character (not in range [32,126]) in string literal <" + lexeme.charAt(i) + ">";
					return false;
				}
			}
		} else {
			errorReport = "String literal must start and end with \"";
			return false;
		}

		return true;
	}

	private boolean isValidCharConst(String lexeme) {
		if (lexeme.length() == 3) {
			// of form 'a'
			if (lexeme.charAt(0) == '\'' && lexeme.charAt(2) == '\'') {
				switch (lexeme.charAt(1)) {
					case '\\':
					case '\'':
					case '\"':
						errorReport = "Characters \\, \',\" ust be escaped";
						return false;
					default:
						if (lexeme.charAt(2) >= 32 && lexeme.charAt(2) <= 126) {
							return true;
						} else {
							errorReport = "Character not in range [32,126] in char literal";
							return false;
						}
				}
			} else {
				errorReport = "String literal must be of form '_' or '\\_'";
				return false;
			}
		} else if (lexeme.length() == 4) {
			// of form '\_'
			if (lexeme.charAt(0) == '\'' && lexeme.charAt(3) == '\'' && lexeme.charAt(1) == '\\') {
				switch (lexeme.charAt(2)) {
					case '\\':
					case '\'':
					case '\"':
					case 't':
					case 'n':
						return true;
					default:
						errorReport = "Invalid escape sequence";
						return false;
				}
			} else {
				errorReport = "String literal must be of form '_' or '\\_'";
				return false;
			}
		} else {
			//Chars cannot have any other form
			errorReport = "String literal must be of form '_' or '\\_'";
			return false;
		}
	}


	private void clearLexer() {
		lexeme = new StringBuilder("");
		state = State.NONE;
		endFlag = EndFlag.NONE;

		prevColumn = currColumn + 1;
		prevLine = currLine;
	}

	private Position getPosition() {
		return new Position(
				this.task.srcFName, this.prevLine, this.prevColumn,
				this.task.srcFName, this.currLine, this.currColumn
		);
	}

	private Symbol createSymbol() {
		String lexemeString = this.lexeme.toString();
		Symbol.Token token = idToken(this.state, lexemeString);
		Position pos = getPosition();

		boolean needLexeme;

		switch (token) {
			case ERROR:
			case CONST_INTEGER:
			case CONST_BOOLEAN:
			case CONST_CHAR:
			case CONST_STRING:
			case IDENTIFIER:
				needLexeme = true;
				break;
			default:
				needLexeme = false;
		}

		if (needLexeme) {
			return new Symbol(token, lexemeString, pos);
		} else {
			return new Symbol(token, pos);
		}

	}

	private CharId identify(char c) {
		if (c == '\'') return CharId.SQUOTE;
		else if (c == '\\') return CharId.BSLASH;
		else if (c == '"') return CharId.DQUOTE;
		else if (c == '#') return CharId.HASH;
		else if (c == '_') return CharId.USCORE;
		else if (c == '\n') return CharId.NL;
		else if (Character.isWhitespace(c)) return CharId.OTHER_WS;
		else if (Arrays.asList(symbols_starters).contains(c)) return CharId.SYMB;
		else if (Character.isLetter(c)) return CharId.ALPHA;
		else if (Character.isDigit(c)) return CharId.DIGIT;
		else return CharId.ERROR;
	}

	private Symbol.Token idToken(State s, String lexeme) {

		switch (s) {
			case CHARCON:
				return Symbol.Token.CONST_CHAR;
			case INTCON:
				return Symbol.Token.CONST_INTEGER;
			case STRCON:
				return Symbol.Token.CONST_STRING;
			case WORD:
				switch (lexeme) {
					// Bool constants
					case "false":
					case "true":
						return Symbol.Token.CONST_BOOLEAN;
					// Ptr constant
					case "null":
						return Symbol.Token.CONST_NULL;
					// Void constant
					case "none":
						return Symbol.Token.CONST_NONE;

					// Types
					case "integer":
						return Symbol.Token.INTEGER;
					case "boolean":
						return Symbol.Token.BOOLEAN;
					case "char":
						return Symbol.Token.CHAR;
					case "string":
						return Symbol.Token.STRING;
					case "void":
						return Symbol.Token.VOID;

					// Keywords
					case "arr":
						return Symbol.Token.ARR;
					case "else":
						return Symbol.Token.ELSE;
					case "end":
						return Symbol.Token.END;
					case "for":
						return Symbol.Token.FOR;
					case "fun":
						return Symbol.Token.FUN;
					case "if":
						return Symbol.Token.IF;
					case "then":
						return Symbol.Token.THEN;
					case "ptr":
						return Symbol.Token.PTR;
					case "rec":
						return Symbol.Token.REC;
					case "typ":
						return Symbol.Token.TYP;
					case "var":
						return Symbol.Token.VAR;
					case "where":
						return Symbol.Token.WHERE;
					case "while":
						return Symbol.Token.WHILE;

					default:
						return Symbol.Token.IDENTIFIER;
				}

			case SYMBOL:
				switch (lexeme) {
					case "+":
						return Symbol.Token.ADD;
					case "&":
						return Symbol.Token.AND;
					case "=":
						return Symbol.Token.ASSIGN;
					case ":":
						return Symbol.Token.COLON;
					case ",":
						return Symbol.Token.COMMA;
					case "}":
						return Symbol.Token.CLOSING_BRACE;
					case "]":
						return Symbol.Token.CLOSING_BRACKET;
					case ")":
						return Symbol.Token.CLOSING_PARENTHESIS;
					case ".":
						return Symbol.Token.DOT;
					case "/":
						return Symbol.Token.DIV;
					case "==":
						return Symbol.Token.EQU;
					case ">=":
						return Symbol.Token.GEQ;
					case ">":
						return Symbol.Token.GTH;
					case "<=":
						return Symbol.Token.LEQ;
					case "<":
						return Symbol.Token.LTH;
					case "@":
						return Symbol.Token.MEM;
					case "%":
						return Symbol.Token.MOD;
					case "*":
						return Symbol.Token.MUL;
					case "!=":
						return Symbol.Token.NEQ;
					case "!":
						return Symbol.Token.NOT;
					case "{":
						return Symbol.Token.OPENING_BRACE;
					case "[":
						return Symbol.Token.OPENING_BRACKET;
					case "(":
						return Symbol.Token.OPENING_PARENTHESIS;
					case "|":
						return Symbol.Token.OR;
					case "-":
						return Symbol.Token.SUB;
					case "^":
						return Symbol.Token.VAL;

					default:
						throw new LexAnError(getPosition() + " Unknown symbol || " + lexeme + " ||");
				}
			case EOF:
				return Symbol.Token.EOF;

			case WS:
			case COMM:
				throw new LexAnError(getPosition() + " idToken: Whitspace should never reach idToken!");
		}
		throw new LexAnError(getPosition() + " Lexer: idToken: unknown token to id");
	}

	/**
	 * Prints out the symbol and returns it.
	 * <p>
	 * This method should be called by the lexical analyzer before it returns a
	 * symbol so that the symbol can be logged (even if logging of lexical
	 * analysis has not been requested).
	 *
	 * @param symbol The symbol to be printed out.
	 * @return The symbol received as an argument.
	 */
	private Symbol log(Symbol symbol) {
		symbol.log(logger);
		return symbol;
	}

}

