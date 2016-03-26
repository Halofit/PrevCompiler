package compiler.phase.synan;

import compiler.Task;
import compiler.common.logger.Transformer;
import compiler.common.report.PhaseErrors.SynAnError;
import compiler.common.report.Report;
import compiler.data.ast.*;
import compiler.phase.Phase;
import compiler.phase.lexan.LexAn;
import compiler.phase.lexan.Symbol;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

import java.util.LinkedList;


/**
 * The syntax analyzer.
 *
 * @author sliva
 */
public class SynAn extends Phase {

	/**
	 * The lexical analyzer.
	 */
	private final LexAn lexAn;

	/**
	 * Constructs a new syntax analyzer.
	 *
	 * @ param lexAn
	 * The lexical analyzer.
	 */
	public SynAn(Task task) {
		super(task, "synan");
		this.lexAn = new LexAn(task);
		this.logger.setTransformer(//
				new Transformer() {
					// This transformer produces the
					// left-most derivation.

					private String nodeName(Node node) {
						Element element = (Element) node;
						String nodeName = element.getTagName();
						if (nodeName.equals("nont")) {
							return element.getAttribute("name");
						}
						if (nodeName.equals("symbol")) {
							return element.getAttribute("name");
						}
						return null;
					}

					private void leftMostDer(Node node) {
						if (((Element) node).getTagName().equals("nont")) {
							String nodeName = nodeName(node);
							NodeList children = node.getChildNodes();
							StringBuilder production = new StringBuilder();
							production.append(nodeName).append(" -->");
							for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
								Node child = children.item(childIdx);
								String childName = nodeName(child);
								production.append(" ").append(childName);
							}
							Report.info(production.toString());
							for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
								Node child = children.item(childIdx);
								leftMostDer(child);
							}
						}
					}

					public Document transform(Document doc) {
						leftMostDer(doc.getDocumentElement().getFirstChild());
						return doc;
					}
				});
	}

	/**
	 * Terminates syntax analysis. Lexical analyzer is not closed and, if
	 * logging has been requested, this method produces the report by closing
	 * the logger.
	 */
	@Override
	public void close() {
		lexAn.close();
		super.close();
	}

	/**
	 * The parser's lookahead buffer.
	 */
	private Symbol laSymbol;

	/**
	 * Reads the next lexical symbol from the source file and stores it in the
	 * lookahead buffer (before that it logs the previous lexical symbol, if
	 * requested); returns the previous symbol.
	 *
	 * @return The previous symbol (the one that has just been replaced by the
	 * new symbol).
	 */
	private Symbol nextSymbol() {
		Symbol symbol = laSymbol;
		symbol.log(logger);
		laSymbol = lexAn.lexAn();
		return symbol;
	}

	/**
	 * Starts logging an internal node of the derivation tree.
	 *
	 * @param nontName The name of a nonterminal the internal node represents.
	 */
	private void begLog(String nontName) {
		if (logger == null) return;
		logger.begElement("nont");
		logger.addAttribute("name", nontName);
	}

	/**
	 * Ends logging an internal node of the derivation tree.
	 */
	private void endLog() {
		if (logger == null) return;
		logger.endElement();
	}

	/**
	 * The parser.
	 * <p>
	 * This method performs the syntax analysis of the source file.
	 */
	public Program synAn() {
		laSymbol = lexAn.lexAn();
		Program prg = parseProgram();

		if (laSymbol.token != Symbol.Token.EOF) {
			Report.warning(laSymbol, "Unexpected symbol(s) at the end of file.");
		}

		return prg;
	}


	private Symbol skip(Symbol.Token token) {
		if (laSymbol.token == token) {
			return nextSymbol();
		} else {
			SynAnError error = new SynAnError("[skip] expected " + token
					+ " got " + laSymbol.token + " at " + laSymbol.getPosition());

			throw new SynAnError("[skip] expected " + token + " got "
					+ laSymbol.token + " at " + laSymbol.getPosition()
					+ " " + (error.getStackTrace()[1]));
		}
	}


	private void signalError(String funName) {
		throw new SynAnError("[" + funName + "]" + laSymbol.getPosition() + " | " + laSymbol.token);
	}


	// All these methods are a part of a recursive descent implementation of an
	// LL(1) parser.

	//Program -> Expression .
	private Program parseProgram() {
		begLog("Program");
		Expr expr = parseExpression();

		if (laSymbol.token != Symbol.Token.EOF) {
			throw new SynAnError("Symbol at the end is not EOF, not all input was consumed.");
		}

		endLog();
		return new Program(laSymbol, expr);
	}

	//Expression -> AssignmentExpression ExpressionPrime .
	private Expr parseExpression() {
		begLog("Expression");
		Expr op1 = parseAssignmentExpression();
		Expr expr = parseExpressionPrime(op1);
		endLog();
		return expr;
	}

	//ExpressionPrime -> where Declarations end ExpressionPrime .
	//ExpressionPrime -> .
	private Expr parseExpressionPrime(Expr op1) {
		begLog("ExpressionPrime");
		Expr expr;
		switch (laSymbol.token) {
			case WHERE:
				skip(Symbol.Token.WHERE);
				LinkedList<Decl> decls = parseDeclarations();
				skip(Symbol.Token.END);
				Expr where = new WhereExpr(laSymbol, op1, decls);
				expr = parseExpressionPrime(where);
				break;

			case END:
			case COMMA:
			case CLOSING_BRACKET:
			case CLOSING_PARENTHESIS:
			case THEN:
			case ELSE:
			case COLON:
			case TYP:
			case FUN:
			case VAR:
			case EOF:
				expr = op1;
				break;

			default:
				expr = null;
				signalError("ExpressionPrime");

		}
		endLog();

		return expr;
	}


	//Expressions -> Expression ExpressionsPrime .
	private LinkedList<Expr> parseExpressions() {
		begLog("Expressions");
		Expr expr = parseExpression();
		LinkedList<Expr> exprs = parseExpressionsPrime();
		endLog();
		exprs.add(0, expr);
		return exprs;
	}

	//ExpressionsPrime -> comma Expression ExpressionsPrime .
	//ExpressionsPrime -> .
	private LinkedList<Expr> parseExpressionsPrime() {
		begLog("ExpressionsPrime");
		LinkedList<Expr> exprs;
		switch (laSymbol.token) {
			case COMMA:
				skip(Symbol.Token.COMMA);
				Expr expr = parseExpression();
				exprs = parseExpressionsPrime();
				exprs.add(0, expr);
				break;
			case CLOSING_PARENTHESIS:
				exprs = new LinkedList<>();
				break;
			default:
				exprs = null;
				signalError("ExpressionsPrime");
		}
		endLog();
		return exprs;
	}

	//AssignmentExpression -> DisjunctiveExpression AssignmentExpressionPrime .
	private Expr parseAssignmentExpression() {
		begLog("AssignmentExpression");
		Expr op1 = parseDisjunctiveExpression();
		Expr expr = parseAssignmentExpressionPrime(op1);
		endLog();
		return expr;
	}


	//AssignmentExpressionPrime -> .
	//AssignmentExpressionPrime -> assign DisjunctiveExpression .
	private Expr parseAssignmentExpressionPrime(Expr op1) {
		begLog("AssignmentExpressionPrime");
		Expr expr;

		switch (laSymbol.token) {
			case ASSIGN:
				skip(Symbol.Token.ASSIGN);
				Expr op2 = parseDisjunctiveExpression();
				expr = new BinExpr(laSymbol, BinExpr.Oper.ASSIGN, op1, op2);
				break;

			case WHERE:
			case END:
			case COMMA:
			case CLOSING_BRACKET:
			case CLOSING_PARENTHESIS:
			case THEN:
			case ELSE:
			case COLON:
			case TYP:
			case FUN:
			case VAR:
			case EOF:
				expr = op1;
				break;

			default:
				expr = null;
				signalError("AssignmentExpressionPrime");
		}

		endLog();
		return expr;
	}

	//DisjunctiveExpression -> ConjunctiveExpression DisjunctiveExpressionPrime .
	private Expr parseDisjunctiveExpression() {
		begLog("DisjunctiveExpression");
		Expr op1 = parseConjunctiveExpression();
		Expr expr = parseDisjunctiveExpressionPrime(op1);
		endLog();
		return expr;
	}

	//DisjunctiveExpressionPrime -> or ConjunctiveExpression DisjunctiveExpressionPrime .
	//DisjunctiveExpressionPrime -> .
	private Expr parseDisjunctiveExpressionPrime(Expr op1) {
		begLog("DisjunctiveExpressionPrime");
		Expr expr;
		switch (laSymbol.token) {
			case OR:
				skip(Symbol.Token.OR);
				Expr op2 = parseConjunctiveExpression();
				expr = parseDisjunctiveExpressionPrime(new BinExpr(laSymbol, BinExpr.Oper.OR, op1, op2));
				break;
			case WHERE:
			case END:
			case COMMA:
			case ASSIGN:
			case CLOSING_BRACKET:
			case CLOSING_PARENTHESIS:
			case THEN:
			case ELSE:
			case COLON:
			case TYP:
			case FUN:
			case VAR:
			case EOF:
				expr = op1;
				break;
			default:
				expr = null;
				signalError("DisjunctiveExpressionPrime");
		}
		endLog();
		return expr;
	}

	//ConjunctiveExpression -> RelationalExpression ConjunctiveExpressionPrime .
	private Expr parseConjunctiveExpression() {
		begLog("ConjunctiveExpression");
		Expr op1 = parseRelationalExpression();
		Expr expr = parseConjunctiveExpressionPrime(op1);
		endLog();
		return expr;
	}

	//ConjunctiveExpressionPrime -> and RelationalExpression ConjunctiveExpressionPrime .
	//ConjunctiveExpressionPrime -> .
	private Expr parseConjunctiveExpressionPrime(Expr op1) {
		begLog("ConjunctiveExpressionPrime");
		Expr expr;
		switch (laSymbol.token) {
			case AND:
				skip(Symbol.Token.AND);
				Expr op2 = parseRelationalExpression();
				expr = parseConjunctiveExpressionPrime(new BinExpr(laSymbol, BinExpr.Oper.AND, op1, op2));
				break;
			case WHERE:
			case END:
			case COMMA:
			case ASSIGN:
			case OR:
			case CLOSING_BRACKET:
			case CLOSING_PARENTHESIS:
			case THEN:
			case ELSE:
			case COLON:
			case TYP:
			case FUN:
			case VAR:
			case EOF:
				expr = op1;
				break;
			default:
				expr = null;
				signalError("ConjunctiveExpressionPrime");
		}
		endLog();
		return expr;
	}

	//RelationalExpression -> AdditiveExpression RelationalExpressionPrime .
	private Expr parseRelationalExpression() {
		begLog("RelationalExpression");
		Expr op1 = parseAdditiveExpression();
		Expr expr = parseRelationalExpressionPrime(op1);
		endLog();
		return expr;
	}

	//RelationalExpressionPrime -> .
	//RelationalExpressionPrime -> equ AdditiveExpression .
	//RelationalExpressionPrime -> neq AdditiveExpression .
	//RelationalExpressionPrime -> lth AdditiveExpression .
	//RelationalExpressionPrime -> gth AdditiveExpression .
	//RelationalExpressionPrime -> leq AdditiveExpression .
	//RelationalExpressionPrime -> geq AdditiveExpression .
	private Expr parseRelationalExpressionPrime(Expr op1) {
		begLog("RelationalExpressionPrime");
		Expr expr;
		Expr op2;
		switch (laSymbol.token) {
			case EQU:
				skip(Symbol.Token.EQU);
				op2 = parseAdditiveExpression();
				expr = new BinExpr(laSymbol, BinExpr.Oper.EQU, op1, op2);
				break;
			case NEQ:
				skip(Symbol.Token.NEQ);
				op2 = parseAdditiveExpression();
				expr = new BinExpr(laSymbol, BinExpr.Oper.NEQ, op1, op2);
				break;
			case LTH:
				skip(Symbol.Token.LTH);
				op2 = parseAdditiveExpression();
				expr = new BinExpr(laSymbol, BinExpr.Oper.LTH, op1, op2);
				break;
			case GTH:
				skip(Symbol.Token.GTH);
				op2 = parseAdditiveExpression();
				expr = new BinExpr(laSymbol, BinExpr.Oper.GTH, op1, op2);
				break;
			case LEQ:
				skip(Symbol.Token.LEQ);
				op2 = parseAdditiveExpression();
				expr = new BinExpr(laSymbol, BinExpr.Oper.LEQ, op1, op2);
				break;
			case GEQ:
				skip(Symbol.Token.GEQ);
				op2 = parseAdditiveExpression();
				expr = new BinExpr(laSymbol, BinExpr.Oper.GEQ, op1, op2);
				break;

			case WHERE:
			case END:
			case COMMA:
			case ASSIGN:
			case OR:
			case AND:
			case CLOSING_BRACKET:
			case CLOSING_PARENTHESIS:
			case THEN:
			case ELSE:
			case COLON:
			case TYP:
			case FUN:
			case VAR:
			case EOF:
				expr = op1;
				break;
			default:
				expr = null;
				signalError("RelationalExpressionPrime");
		}
		endLog();
		return expr;
	}

	//AdditiveExpression -> MultiplicativeExpression AdditiveExpressionPrime .
	private Expr parseAdditiveExpression() {
		begLog("AdditiveExpression");
		Expr op1 = parseMultiplicativeExpression();
		Expr expr = parseAdditiveExpressionPrime(op1);
		endLog();
		return expr;
	}

	//AdditiveExpressionPrime -> add MultiplicativeExpression AdditiveExpressionPrime .
	//AdditiveExpressionPrime -> sub MultiplicativeExpression AdditiveExpressionPrime .
	//AdditiveExpressionPrime -> .
	private Expr parseAdditiveExpressionPrime(Expr op1) {
		begLog("AdditiveExpressionPrime");
		Expr op2;
		Expr expr;
		switch (laSymbol.token) {
			case ADD:
				skip(Symbol.Token.ADD);
				op2 = parseMultiplicativeExpression();
				expr = parseAdditiveExpressionPrime(new BinExpr(laSymbol, BinExpr.Oper.ADD, op1, op2));
				break;
			case SUB:
				skip(Symbol.Token.SUB);
				op2 = parseMultiplicativeExpression();
				expr = parseAdditiveExpressionPrime(new BinExpr(laSymbol, BinExpr.Oper.SUB, op1, op2));
				break;

			case WHERE:
			case END:
			case COMMA:
			case ASSIGN:
			case OR:
			case AND:
			case EQU:
			case NEQ:
			case LTH:
			case GTH:
			case LEQ:
			case GEQ:
			case CLOSING_BRACKET:
			case CLOSING_PARENTHESIS:
			case THEN:
			case ELSE:
			case COLON:
			case TYP:
			case FUN:
			case VAR:
			case EOF:
				expr = op1;
				break;

			default:
				expr = null;
				signalError("AdditiveExpressionPrime");

		}
		endLog();
		return expr;
	}

	//MultiplicativeExpression -> PrefixExpression MultiplicativeExpressionPrime .
	private Expr parseMultiplicativeExpression() {
		begLog("MultiplicativeExpression");
		Expr op1 = parsePrefixExpression();
		Expr expr = parseMultiplicativeExpressionPrime(op1);
		endLog();
		return expr;
	}

	//MultiplicativeExpressionPrime -> mul PrefixExpression MultiplicativeExpressionPrime .
	//MultiplicativeExpressionPrime -> div PrefixExpression MultiplicativeExpressionPrime .
	//MultiplicativeExpressionPrime -> mod PrefixExpression MultiplicativeExpressionPrime .
	//MultiplicativeExpressionPrime -> .
	private Expr parseMultiplicativeExpressionPrime(Expr op1) {
		begLog("MultiplicativeExpressionPrime");
		Expr expr;
		Expr op2;
		switch (laSymbol.token) {
			case MUL:
				skip(Symbol.Token.MUL);
				op2 = parsePrefixExpression();
				expr = parseMultiplicativeExpressionPrime(new BinExpr(laSymbol, BinExpr.Oper.MUL, op1, op2));
				break;
			case DIV:
				skip(Symbol.Token.DIV);
				op2 = parsePrefixExpression();
				expr = parseMultiplicativeExpressionPrime(new BinExpr(laSymbol, BinExpr.Oper.DIV, op1, op2));
				break;
			case MOD:
				skip(Symbol.Token.MOD);
				op2 = parsePrefixExpression();
				expr = parseMultiplicativeExpressionPrime(new BinExpr(laSymbol, BinExpr.Oper.MOD, op1, op2));
				break;

			case WHERE:
			case END:
			case COMMA:
			case ASSIGN:
			case OR:
			case AND:
			case EQU:
			case NEQ:
			case LTH:
			case GTH:
			case LEQ:
			case GEQ:
			case ADD:
			case SUB:
			case CLOSING_BRACKET:
			case CLOSING_PARENTHESIS:
			case THEN:
			case ELSE:
			case COLON:
			case TYP:
			case FUN:
			case VAR:
			case EOF:
				expr = op1;
				break;

			default:
				expr = null;
				signalError("MultiplicativeExpressionPrime");
		}
		endLog();
		return expr;
	}

	//PrefixExpression -> PostfixExpression .
	//PrefixExpression -> add PrefixExpression .
	//PrefixExpression -> sub PrefixExpression .
	//PrefixExpression -> not PrefixExpression .
	//PrefixExpression -> mem PrefixExpression .
	//PrefixExpression -> lbracket Type rbracket PrefixExpression .
	private Expr parsePrefixExpression() {
		begLog("PrefixExpression");
		Expr expr;
		Expr op1;

		switch (laSymbol.token) {
			case ADD:
				skip(Symbol.Token.ADD);
				op1 = parsePrefixExpression();
				expr = new UnExpr(laSymbol, UnExpr.Oper.ADD, op1);
				break;
			case SUB:
				skip(Symbol.Token.SUB);
				op1 = parsePrefixExpression();
				expr = new UnExpr(laSymbol, UnExpr.Oper.SUB, op1);
				break;
			case NOT:
				skip(Symbol.Token.NOT);
				op1 = parsePrefixExpression();
				expr = new UnExpr(laSymbol, UnExpr.Oper.NOT, op1);
				break;
			case MEM:
				skip(Symbol.Token.MEM);
				op1 = parsePrefixExpression();
				expr = new UnExpr(laSymbol, UnExpr.Oper.MEM, op1);
				break;
			case OPENING_BRACKET:
				skip(Symbol.Token.OPENING_BRACKET);
				Type type = parseType();
				skip(Symbol.Token.CLOSING_BRACKET);
				op1 = parsePrefixExpression();
				expr = new CastExpr(laSymbol, type, op1);
				break;
			default:
				expr = parsePostfixExpression();
				break;
		}
		endLog();
		return expr;
	}

	//PostfixExpression -> AtomicExpression PostfixExpressionPrime .
	private Expr parsePostfixExpression() {
		begLog("PostfixExpression");
		Expr op1 = parseAtomicExpression();
		Expr expr = parsePostfixExpressionPrime(op1);
		endLog();
		return expr;
	}

	//PostfixExpressionPrime -> lbracket Expression rbracket PostfixExpressionPrime .
	//PostfixExpressionPrime -> dot IDENTIFIER PostfixExpressionPrime .
	//PostfixExpressionPrime -> val PostfixExpressionPrime .
	//PostfixExpressionPrime -> .
	private Expr parsePostfixExpressionPrime(Expr op1) {
		begLog("PostfixExpressionPrime");
		Expr expr;
		Expr op2;
		switch (laSymbol.token) {
			case OPENING_BRACKET: //prefix [expr]
				skip(Symbol.Token.OPENING_BRACKET);
				op2 = parseExpression();
				skip(Symbol.Token.CLOSING_BRACKET);
				expr = parsePostfixExpressionPrime(new BinExpr(laSymbol, BinExpr.Oper.ARR, op1, op2));
				break;

			case DOT:
				skip(Symbol.Token.DOT);
				Symbol id = skip(Symbol.Token.IDENTIFIER);
				op2 = new VarName(laSymbol, id.lexeme);
				expr = parsePostfixExpressionPrime(new BinExpr(laSymbol, BinExpr.Oper.REC, op1, op2));
				break;
			case VAL:
				skip(Symbol.Token.VAL);
				expr = parsePostfixExpressionPrime(new UnExpr(laSymbol, UnExpr.Oper.VAL, op1));
				break;

			case WHERE:
			case END:
			case COMMA:
			case ASSIGN:
			case OR:
			case AND:
			case EQU:
			case NEQ:
			case LTH:
			case GTH:
			case LEQ:
			case GEQ:
			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case MOD:
			case CLOSING_BRACKET:
			case CLOSING_PARENTHESIS:
			case THEN:
			case ELSE:
			case COLON:
			case TYP:
			case FUN:
			case VAR:
			case EOF:
				expr = op1;
				break;

			default:
				expr = null;
				signalError("PostfixExpressionPrime");
		}
		endLog();
		return expr;
	}

	//AtomicExpression -> INTEGER .
	//AtomicExpression -> BOOLEAN .
	//AtomicExpression -> CHAR .
	//AtomicExpression -> STRING .
	//AtomicExpression -> null .
	//AtomicExpression -> none .
	//AtomicExpression -> IDENTIFIER ArgumentsOpt .
	//AtomicExpression -> lparen Expressions rparen .
	//AtomicExpression -> if Expression then Expression else Expression end .
	//AtomicExpression -> for IDENTIFIER assign Expression comma Expression colon Expression end .
	//AtomicExpression -> while Expression colon Expression end .
	private Expr parseAtomicExpression() {
		begLog("AtomicExpression");
		Expr expr;
		Symbol atom;
		switch (laSymbol.token) {
			case CONST_INTEGER:
				atom = skip(Symbol.Token.CONST_INTEGER);
				expr = new AtomExpr(laSymbol, AtomExpr.AtomTypes.INTEGER, atom.lexeme);
				break;
			case CONST_BOOLEAN:
				atom = skip(Symbol.Token.CONST_BOOLEAN);
				expr = new AtomExpr(laSymbol, AtomExpr.AtomTypes.BOOLEAN, atom.lexeme);
				break;
			case CONST_CHAR:
				atom = skip(Symbol.Token.CONST_CHAR);
				expr = new AtomExpr(laSymbol, AtomExpr.AtomTypes.CHAR, atom.lexeme);
				break;
			case CONST_STRING:
				atom = skip(Symbol.Token.CONST_STRING);
				expr = new AtomExpr(laSymbol, AtomExpr.AtomTypes.STRING, atom.lexeme);
				break;
			case CONST_NULL:
				atom = skip(Symbol.Token.CONST_NULL);
				expr = new AtomExpr(laSymbol, AtomExpr.AtomTypes.PTR, atom.lexeme);
				break;
			case CONST_NONE:
				atom = skip(Symbol.Token.CONST_NONE);
				expr = new AtomExpr(laSymbol, AtomExpr.AtomTypes.VOID, atom.lexeme);
				break;
			case IDENTIFIER:
				atom = skip(Symbol.Token.IDENTIFIER);
				LinkedList<Expr> args = parseArgumentsOpt();
				expr = new FunCall(laSymbol, atom.lexeme, args);
				break;
			case OPENING_PARENTHESIS:
				skip(Symbol.Token.OPENING_PARENTHESIS);
				LinkedList<Expr> exprs = parseExpressions();
				expr = new Exprs(laSymbol, exprs);
				skip(Symbol.Token.CLOSING_PARENTHESIS);
				break;
			//AtomicExpression -> if Expression then Expression else Expression end .
			case IF:
				skip(Symbol.Token.IF);
				Expr ifCond = parseExpression();
				skip(Symbol.Token.THEN);
				Expr thenExpr = parseExpression();
				skip(Symbol.Token.ELSE);
				Expr elseExpr = parseExpression();
				skip(Symbol.Token.END);
				expr = new IfExpr(laSymbol, ifCond, thenExpr, elseExpr);
				break;
			//AtomicExpression -> for IDENTIFIER assign Expression comma Expression colon Expression end .
			case FOR:
				skip(Symbol.Token.FOR);
				Symbol id = skip(Symbol.Token.IDENTIFIER);
				VarName iterVar = new VarName(laSymbol, id.lexeme);
				skip(Symbol.Token.ASSIGN);
				Expr lowBound = parseExpression();
				skip(Symbol.Token.COMMA);
				Expr highBound = parseExpression();
				skip(Symbol.Token.COLON);
				Expr forBody = parseExpression();
				skip(Symbol.Token.END);
				expr = new ForExpr(laSymbol, iterVar, lowBound, highBound, forBody);
				break;

			//AtomicExpression -> while Expression colon Expression end .
			case WHILE:
				skip(Symbol.Token.WHILE);
				Expr whileCond = parseExpression();
				skip(Symbol.Token.COLON);
				Expr whileBody = parseExpression();
				skip(Symbol.Token.END);
				expr = new WhileExpr(laSymbol, whileCond, whileBody);
				break;

			default:
				expr = null;
				signalError("AtomicExpression");

		}

		endLog();
		return expr;
	}

	//ArgumentsOpt -> .
	//ArgumentsOpt -> lparen Expressions rparen .
	private LinkedList<Expr> parseArgumentsOpt() {
		begLog("ArgumentsOpt");
		LinkedList<Expr> exprs;

		switch (laSymbol.token) {
			case OPENING_PARENTHESIS:
				skip(Symbol.Token.OPENING_PARENTHESIS);
				exprs = parseExpressions();
				skip(Symbol.Token.CLOSING_PARENTHESIS);
				break;

			case WHERE:
			case END:
			case COMMA:
			case ASSIGN:
			case OR:
			case AND:
			case EQU:
			case NEQ:
			case LTH:
			case GTH:
			case LEQ:
			case GEQ:
			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case MOD:
			case OPENING_BRACKET:
			case CLOSING_BRACKET:
			case DOT:
			case VAL:
			case CLOSING_PARENTHESIS:
			case THEN:
			case ELSE:
			case COLON:
			case TYP:
			case FUN:
			case VAR:
			case EOF:
				exprs = new LinkedList<>();
				break;
			default:
				exprs = null;
				signalError("ArgumentsOpt");
		}

		endLog();
		return exprs;
	}

	//Declarations -> Declaration DeclarationsPrime .
	private LinkedList<Decl> parseDeclarations() {
		begLog("Declarations");
		Decl decl = parseDeclaration();
		LinkedList<Decl> decls = parseDeclarationsPrime();
		decls.add(0, decl);
		endLog();
		return decls;
	}

	//DeclarationsPrime -> Declaration DeclarationsPrime .
	//DeclarationsPrime -> .
	private LinkedList<Decl> parseDeclarationsPrime() {
		begLog("DeclarationsPrime");
		LinkedList<Decl> decls;
		switch (laSymbol.token) {
			case TYP:
			case FUN:
			case VAR:
				Decl decl = parseDeclaration();
				decls = parseDeclarationsPrime();
				decls.add(0, decl);
				break;
			case END:
				decls = new LinkedList<>();
				break;
			default:
				decls = null;
				signalError("DeclarationsPrime");
		}

		endLog();
		return decls;
	}

	//Declaration -> TypeDeclaration .
	//Declaration -> FunctionDeclaration .
	//Declaration -> VariableDeclaration .
	private Decl parseDeclaration() {
		begLog("Declaration");
		Decl decl;
		switch (laSymbol.token) {
			case TYP:
				decl = parseTypeDeclaration();
				break;
			case FUN:
				decl = parseFunctionDeclaration();
				break;
			case VAR:
				decl = parseVariableDeclaration();
				break;
			default:
				decl = null;
				signalError("Declaration");
		}
		endLog();
		return decl;
	}

	//TypeDeclaration -> typ IDENTIFIER colon Type .
	private TypeDecl parseTypeDeclaration() {
		begLog("TypeDeclaration");
		skip(Symbol.Token.TYP);
		Symbol id = skip(Symbol.Token.IDENTIFIER);
		skip(Symbol.Token.COLON);
		Type type = parseType();
		TypeDecl decl = new TypeDecl(laSymbol, id.lexeme, type);

		endLog();
		return decl;
	}

	//FunctionDeclaration -> fun IDENTIFIER lparen ParametersOpt rparen colon Type FunctionBodyOpt .
	private FunDecl parseFunctionDeclaration() {
		begLog("FunctionDeclaration");
		skip(Symbol.Token.FUN);
		Symbol id = skip(Symbol.Token.IDENTIFIER);
		skip(Symbol.Token.OPENING_PARENTHESIS);
		LinkedList<ParDecl> params = parseParametersOpt();
		skip(Symbol.Token.CLOSING_PARENTHESIS);
		skip(Symbol.Token.COLON);
		Type type = parseType();
		Expr body = parseFunctionBodyOpt();

		FunDecl decl;
		if (body == null) {
			decl = new FunDecl(laSymbol, id.lexeme, params, type);
		} else {
			decl = new FunDef(laSymbol, id.lexeme, params, type, body);
		}

		endLog();
		return decl;
	}

	//ParametersOpt -> .
	//ParametersOpt -> Parameters .
	private LinkedList<ParDecl> parseParametersOpt() {
		begLog("ParametersOpt");
		LinkedList<ParDecl> params;
		switch (laSymbol.token) {
			case IDENTIFIER:
				params = parseParameters();
				break;
			case CLOSING_PARENTHESIS:
				params = new LinkedList<>();
				break;
			default:
				params = null;
				signalError("ParametersOpt");
		}
		endLog();

		return params;
	}

	//Parameters -> Parameter ParametersPrime .
	private LinkedList<ParDecl> parseParameters() {
		begLog("Parameters");
		ParDecl param = parseParameter();
		LinkedList<ParDecl> params = parseParametersPrime();
		params.add(0, param);
		endLog();
		return params;
	}

	//ParametersPrime -> comma Parameter ParametersPrime .
	//ParametersPrime -> .
	private LinkedList<ParDecl> parseParametersPrime() {
		begLog("ParametersPrime");
		LinkedList<ParDecl> params;
		switch (laSymbol.token) {
			case COMMA:
				skip(Symbol.Token.COMMA);
				ParDecl param = parseParameter();
				params = parseParametersPrime();
				params.add(0, param);
				break;

			case CLOSING_PARENTHESIS:
				params = new LinkedList<>();
				break;
			default:
				params = null;
				signalError("ParametersPrime");
		}
		endLog();
		return params;
	}

	//Parameter -> IDENTIFIER colon Type .
	private ParDecl parseParameter() {
		begLog("Parameter");
		Symbol id = skip(Symbol.Token.IDENTIFIER);
		skip(Symbol.Token.COLON);
		Type type = parseType();
		endLog();
		return new ParDecl(laSymbol, id.lexeme, type);
	}

	//FunctionBodyOpt -> .
	//FunctionBodyOpt -> assign Expression .
	private Expr parseFunctionBodyOpt() {
		begLog("FunctionBodyOpt");
		Expr expr;
		switch (laSymbol.token) {
			case ASSIGN:
				skip(Symbol.Token.ASSIGN);
				expr = parseExpression();
				break;
			case END:
			case TYP:
			case FUN:
			case VAR:
				expr = null;
				break;
			default:
				expr = null;
				signalError("FunctionBodyOpt");
		}
		endLog();

		return expr;
	}

	//VariableDeclaration -> var IDENTIFIER colon Type .
	private VarDecl parseVariableDeclaration() {
		begLog("VariableDeclaration");
		skip(Symbol.Token.VAR);
		Symbol id = skip(Symbol.Token.IDENTIFIER);
		skip(Symbol.Token.COLON);
		Type type = parseType();
		VarDecl decl = new VarDecl(laSymbol, id.lexeme, type);

		endLog();
		return decl;
	}

	//Type -> integer .
	//Type -> boolean .
	//Type -> char .
	//Type -> string .
	//Type -> void .
	//Type -> arr lbracket Expression rbracket Type .
	//Type -> rec lbrace Components rbrace .
	//Type -> ptr Type .
	//Type -> IDENTIFIER .
	private Type parseType() {
		begLog("Type");
		Type type;
		switch (laSymbol.token) {
			case INTEGER:
				skip(Symbol.Token.INTEGER);
				type = new AtomType(laSymbol, AtomType.AtomTypes.INTEGER);
				break;
			case BOOLEAN:
				skip(Symbol.Token.BOOLEAN);
				type = new AtomType(laSymbol, AtomType.AtomTypes.BOOLEAN);
				break;
			case CHAR:
				skip(Symbol.Token.CHAR);
				type = new AtomType(laSymbol, AtomType.AtomTypes.CHAR);
				break;
			case STRING:
				skip(Symbol.Token.STRING);
				type = new AtomType(laSymbol, AtomType.AtomTypes.STRING);
				break;
			case VOID:
				skip(Symbol.Token.VOID);
				type = new AtomType(laSymbol, AtomType.AtomTypes.VOID);
				break;
			case ARR:
				skip(Symbol.Token.ARR);
				skip(Symbol.Token.OPENING_BRACKET);
				Expr size = parseExpression();
				skip(Symbol.Token.CLOSING_BRACKET);
				Type arrType = parseType();

				type = new ArrType(laSymbol, size, arrType);
				break;
			case REC:
				skip(Symbol.Token.REC);
				skip(Symbol.Token.OPENING_BRACE);
				LinkedList<CompDecl> comps = parseComponents();
				skip(Symbol.Token.CLOSING_BRACE);
				type = new RecType(laSymbol, comps);
				break;
			case PTR:
				skip(Symbol.Token.PTR);
				Type ptrType = parseType();
				type = new PtrType(laSymbol, ptrType);
				break;
			case IDENTIFIER:
				Symbol typeName = skip(Symbol.Token.IDENTIFIER);
				type = new TypeName(laSymbol, typeName.lexeme);
				break;
			default:
				type = null;
				signalError("Type");
		}

		endLog();
		return type;
	}

	//Components -> Component ComponentsPrime .
	private LinkedList<CompDecl> parseComponents() {
		begLog("Components");
		CompDecl comp = parseComponent();
		LinkedList<CompDecl> comps = parseComponentsPrime();
		comps.add(0, comp);

		endLog();
		return comps;
	}

	//ComponentsPrime -> comma Component ComponentsPrime .
	//ComponentsPrime -> .
	private LinkedList<CompDecl> parseComponentsPrime() {
		begLog("ComponentsPrime");
		LinkedList<CompDecl> comps;
		switch (laSymbol.token) {
			case COMMA:
				skip(Symbol.Token.COMMA);
				CompDecl comp = parseComponent();
				comps = parseComponentsPrime();
				comps.add(0, comp);
				break;
			case CLOSING_BRACE:
				comps = new LinkedList<>();
				break;
			default:
				comps = null;
				signalError("ComponentsPrime");
		}
		endLog();

		return comps;
	}

	//Component -> IDENTIFIER colon Type .
	private CompDecl parseComponent() {
		begLog("Component");
		Symbol id = skip(Symbol.Token.IDENTIFIER);
		skip(Symbol.Token.COLON);
		Type type = parseType();
		CompDecl decl = new CompDecl(laSymbol, id.lexeme, type);

		endLog();
		return decl;
	}
}
