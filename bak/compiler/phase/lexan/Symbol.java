package compiler.phase.lexan;

import compiler.common.logger.*;
import compiler.common.report.*;

/**
 * A lexical symbol.
 * 
 * @author sliva
 */
public class Symbol extends Position implements Loggable {

	/**
	 * Tokens.
	 * 
	 * @author sliva
	 */
	public enum Token {

		// Symbols.

		/** Symbol <code>+</code>. */
		ADD,
		/** Symbol <code>&#x26;</code>. */
		AND,
		/** Symbol <code>=</code>. */
		ASSIGN,
		/** Symbol <code>:</code>. */
		COLON,
		/** Symbol <code>,</code>. */
		COMMA,
		/** Symbol <code>}</code>. */
		CLOSING_BRACE,
		/** Symbol <code>]</code>. */
		CLOSING_BRACKET,
		/** Symbol <code>)</code>. */
		CLOSING_PARENTHESIS,
		/** Symbol <code>.</code>. */
		DOT,
		/** Symbol <code>/</code>. */
		DIV,
		/** Symbol <code>==</code>. */
		EQU,
		/** Symbol <code>&#x3E;=</code>. */
		GEQ,
		/** Symbol <code>&#x3E;</code>. */
		GTH,
		/** Symbol <code>&#x3C;</code>. */
		LEQ,
		/** Symbol <code>&#x3C;=</code>. */
		LTH,
		/** Symbol <code>&#x40;</code>. */
		MEM,
		/** Symbol <code>%</code>. */
		MOD,
		/** Symbol <code>*</code>. */
		MUL,
		/** Symbol <code>!=</code>. */
		NEQ,
		/** Symbol <code>!</code>. */
		NOT,
		/** Symbol <code>{</code>. */
		OPENING_BRACE,
		/** Symbol <code>[</code>. */
		OPENING_BRACKET,
		/** Symbol <code>(</code>. */
		OPENING_PARENTHESIS,
		/** Symbol <code>|</code>. */
		OR,
		/** Symbol <code>-</code>. */
		SUB,
		/** Symbol <code>^</code>. */
		VAL,

		// Constants of atomic types.

		/** Integer constant. */
		CONST_INTEGER,
		/** Logical constant. */
		CONST_BOOLEAN,
		/** Character constant. */
		CONST_CHAR,
		/** String constant. */
		CONST_STRING,
		/** Null pointer. */
		CONST_NULL,
		/** Void constnant. */
		CONST_NONE,

		// Type names.

		/** Type <code>integer</code>. */
		INTEGER,
		/** Type <code>boolean</code>. */
		BOOLEAN,
		/** Type <code>char</code>. */
		CHAR,
		/** Type <code>string</code>. */
		STRING,
		/** Type <code>void</code>. */
		VOID,

		// Keywords.

		/** Keyword <code>arr</code>. */
		ARR,
		/** Keyword <code>else</code>. */
		ELSE,
		/** Keyword <code>end</code>. */
		END,
		/** Keyword <code>for</code>. */
		FOR,
		/** Keyword <code>fun</code>. */
		FUN,
		/** Keyword <code>if</code>. */
		IF,
		/** Keyword <code>then</code>. */
		THEN,
		/** Keyword <code>ptr</code>. */
		PTR,
		/** Keyword <code>rec</code>. */
		REC,
		/** Keyword <code>typ</code>. */
		TYP,
		/** Keyword <code>var</code>. */
		VAR,
		/** Keyword <code>where</code>. */
		WHERE,
		/** Keyword <code>while</code>. */
		WHILE,

		// Identifiers.

		/** Identifier. */
		IDENTIFIER,

		// Non-tokens.

		/** End of file signal. */
		EOF,
		/** Error token. */
		ERROR,

	}

	/** The token of this symbol. */
	public final Token token;

	/** The lexeme of this symbol. */
	public final String lexeme;

	/**
	 * Constructs a new symbol.
	 * 
	 * @param token
	 *            The symbol's token.
	 * @param lexeme
	 *            The symbols's lexeme.
	 * @param position
	 *            The symbol's position.
	 */
	public Symbol(Token token, String lexeme, Position position) {
		super(position);
		this.token = token;
		this.lexeme = lexeme;
	}

	/**
	 * Constructs a new symbol.
	 * 
	 * @param token
	 *            The symbol's token.
	 * @param position
	 *            The symbol's position.
	 */
	public Symbol(Token token, Position position) {
		super(position);
		this.token = token;
		this.lexeme = null;
	}

	@Override
	public void log(Logger logger) {
		if (logger == null)
			return;
		logger.begElement("term");
		logger.addAttribute("name", token.toString());
		logger.addAttribute("lexeme", lexeme);
		super.log(logger);
		logger.endElement();
	}

}
