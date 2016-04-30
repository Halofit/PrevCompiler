package compiler.phase.synan;

import java.util.*;

import org.w3c.dom.*;

import compiler.*;
import compiler.common.logger.*;
import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.phase.*;
import compiler.phase.lexan.*;

/**
 * The syntax analyzer.
 * 
 * @author sliva
 */
public class SynAn extends Phase {

	/** The lexical analyzer. */
	private final LexAn lexAn;

	/**
	 * Constructs a new syntax analyzer.
	 * 
	 * @param task
	 *            The parameters and internal data of the compilation process.
	 */
	public SynAn(Task task) {
		super(task, "synan");
		this.lexAn = new LexAn(task);
		if (logger != null)
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
							StringBuffer production = new StringBuffer();
							production.append(nodeName + " -->");
							for (int childIdx = 0; childIdx < children.getLength(); childIdx++) {
								Node child = children.item(childIdx);
								String childName = nodeName(child);
								production.append(" " + childName);
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

	/** The parser's lookahead buffer. */
	private Symbol laSymbol;

	/**
	 * Reads the next lexical symbol from the source file and stores it in the
	 * lookahead buffer (before that it logs the previous lexical symbol, if
	 * requested); returns the previous symbol.
	 * 
	 * @return The previous symbol (the one that has just been replaced by the
	 *         new symbol).
	 */
	private Symbol nextSymbol() {
		Symbol symbol = laSymbol;
		symbol.log(logger);
		laSymbol = lexAn.lexAn();
		return symbol;
	}

	/**
	 * Logs the error token inserted when a missing lexical symbol has been
	 * reported.
	 * 
	 * @return The error token (the symbol in the lookahead buffer is to be used
	 *         later).
	 */
	private Symbol nextSymbolIsError() {
		Symbol error = new Symbol(Symbol.Token.ERROR, "", new Position("", 0, 0));
		error.log(logger);
		return error;
	}

	/**
	 * Starts logging an internal node of the derivation tree.
	 * 
	 * @param nontName
	 *            The name of a nonterminal the internal node represents.
	 */
	private void begLog(String nontName) {
		if (logger == null)
			return;
		logger.begElement("nont");
		logger.addAttribute("name", nontName);
	}

	/**
	 * Ends logging an internal node of the derivation tree.
	 */
	private void endLog() {
		if (logger == null)
			return;
		logger.endElement();
	}

	/**
	 * The parser.
	 * 
	 * This method performs the syntax analysis of the source file.
	 * 
	 * @return The root of the abstract syntax tree.
	 */
	public Program synAn() {
		laSymbol = lexAn.lexAn();
		Program program = parseProgram();
		if (laSymbol.token != Symbol.Token.EOF)
			Report.warning(laSymbol, "Unexpected symbol(s) at the end of file.");
		return program;
	}

	// All these methods are a part of a recursive descent implementation of an
	// LL(1) parser.

	private Program parseProgram() {
		Program result;
		begLog("Program");
		Expr astExpr = parseExpression();
		result = new Program((Position) astExpr, astExpr);
		endLog();
		return result;
	}

	private Expr parseExpression() {
		Expr result;
		begLog("Expression");
		Expr astExpr = parseAssignmentExpression();
		result = parseExpressionRest(astExpr);
		endLog();
		return result;
	}

	private Expr parseExpressionRest(Expr astExpr) {
		Expr result;
		begLog("ExpressionRest");
		switch (laSymbol.token) {
		case WHERE: {
			nextSymbol();
			LinkedList<Decl> astDecls = parseDeclarations();
			Symbol symEND;
			if (laSymbol.token == Symbol.Token.END) {
				symEND = nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing keyword 'end' inserted.");
				symEND = nextSymbolIsError();
			}
			result = new WhereExpr(new Position(astExpr, symEND), astExpr, astDecls);
			result = parseExpressionRest(result);
			break;
		}
		default:
			result = astExpr;
			break;
		}
		endLog();
		return result;
	}

	private LinkedList<Expr> parseExpressions() {
		LinkedList<Expr> result;
		begLog("Expressions");
		Expr astExpr = parseExpression();
		result = parseExpressionsRest();
		result.addFirst(astExpr);
		endLog();
		return result;
	}

	private LinkedList<Expr> parseExpressionsRest() {
		LinkedList<Expr> result;
		begLog("ExpressionsRest");
		switch (laSymbol.token) {
		case COMMA:
			nextSymbol();
			Expr astExpr = parseExpression();
			result = parseExpressionsRest();
			result.addFirst(astExpr);
			break;
		default:
			result = new LinkedList<Expr>();
			break;
		}
		endLog();
		return result;
	}

	private Expr parseAssignmentExpression() {
		Expr result;
		begLog("AssignmentExpression");
		Expr astExpr = parseDisjunctiveExpression();
		result = parseAssignmentExpressionRest(astExpr);
		endLog();
		return result;
	}

	private Expr parseAssignmentExpressionRest(Expr astFstExpr) {
		Expr result;
		begLog("AssignmentExpressionRest");
		switch (laSymbol.token) {
		case ASSIGN:
			nextSymbol();
			Expr astSndExpr = parseDisjunctiveExpression();
			result = new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.ASSIGN, astFstExpr, astSndExpr);
			break;
		default:
			result = astFstExpr;
			break;
		}
		endLog();
		return result;
	}

	private Expr parseDisjunctiveExpression() {
		Expr result;
		begLog("DisjunctiveExpression");
		Expr astExpr = parseConjunctiveExpression();
		result = parseDisjunctiveExpressionRest(astExpr);
		endLog();
		return result;
	}

	private Expr parseDisjunctiveExpressionRest(Expr astFstExpr) {
		Expr result;
		begLog("DisjunctiveExpressionRest");
		switch (laSymbol.token) {
		case OR:
			nextSymbol();
			Expr astSndExpr = parseConjunctiveExpression();
			result = parseDisjunctiveExpressionRest(
					new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.OR, astFstExpr, astSndExpr));
			break;
		default:
			result = astFstExpr;
			break;
		}
		endLog();
		return result;
	}

	private Expr parseConjunctiveExpression() {
		Expr result;
		begLog("ConjunctiveExpression");
		Expr astExpr = parseRelationalExpression();
		result = parseConjunctiveExpressionRest(astExpr);
		endLog();
		return result;
	}

	private Expr parseConjunctiveExpressionRest(Expr astFstExpr) {
		Expr result;
		begLog("ConjunctiveExpressionRest");
		switch (laSymbol.token) {
		case AND:
			nextSymbol();
			Expr astSndExpr = parseRelationalExpression();
			result = parseConjunctiveExpressionRest(
					new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.AND, astFstExpr, astSndExpr));
			break;
		default:
			result = astFstExpr;
			break;
		}
		endLog();
		return result;
	}

	private Expr parseRelationalExpression() {
		Expr result;
		begLog("RelationalExpression");
		Expr astExpr = parseAdditiveExpression();
		result = parseRelationalExpressionRest(astExpr);
		endLog();
		return result;
	}

	private Expr parseRelationalExpressionRest(Expr astFstExpr) {
		Expr result;
		begLog("RelationalExpressionRest");
		switch (laSymbol.token) {
		case EQU: {
			nextSymbol();
			Expr astSndExpr = parseAdditiveExpression();
			result = new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.EQU, astFstExpr, astSndExpr);
			break;
		}
		case NEQ: {
			nextSymbol();
			Expr astSndExpr = parseAdditiveExpression();
			result = new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.NEQ, astFstExpr, astSndExpr);
			break;
		}
		case LTH: {
			nextSymbol();
			Expr astSndExpr = parseAdditiveExpression();
			result = new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.LTH, astFstExpr, astSndExpr);
			break;
		}
		case GTH: {
			nextSymbol();
			Expr astSndExpr = parseAdditiveExpression();
			result = new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.GTH, astFstExpr, astSndExpr);
			break;
		}
		case LEQ: {
			nextSymbol();
			Expr astSndExpr = parseAdditiveExpression();
			result = new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.LEQ, astFstExpr, astSndExpr);
			break;
		}
		case GEQ: {
			nextSymbol();
			Expr astSndExpr = parseAdditiveExpression();
			result = new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.GEQ, astFstExpr, astSndExpr);
			break;
		}
		default:
			result = astFstExpr;
			break;
		}
		endLog();
		return result;
	}

	private Expr parseAdditiveExpression() {
		Expr result;
		begLog("AdditiveExpression");
		Expr astExpr = parseMultiplicativeExpression();
		result = parseAdditiveExpressionRest(astExpr);
		endLog();
		return result;
	}

	private Expr parseAdditiveExpressionRest(Expr astFstExpr) {
		Expr result;
		begLog("AdditiveExpressionRest");
		switch (laSymbol.token) {
		case ADD: {
			nextSymbol();
			Expr astSndExpr = parseMultiplicativeExpression();
			result = parseAdditiveExpressionRest(
					new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.ADD, astFstExpr, astSndExpr));
			break;
		}
		case SUB: {
			nextSymbol();
			Expr astSndExpr = parseMultiplicativeExpression();
			result = parseAdditiveExpressionRest(
					new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.SUB, astFstExpr, astSndExpr));
			break;
		}
		default:
			result = astFstExpr;
			break;
		}
		endLog();
		return result;
	}

	private Expr parseMultiplicativeExpression() {
		Expr result;
		begLog("MultiplicativeExpression");
		Expr astExpr = parsePrefixExpression();
		result = parseMultiplicativeExpressionRest(astExpr);
		endLog();
		return result;
	}

	private Expr parseMultiplicativeExpressionRest(Expr astFstExpr) {
		Expr result;
		begLog("MultiplicativeExpressionRest");
		switch (laSymbol.token) {
		case MUL: {
			nextSymbol();
			Expr astSndExpr = parsePrefixExpression();
			result = parseMultiplicativeExpressionRest(
					new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.MUL, astFstExpr, astSndExpr));
			break;
		}
		case DIV: {
			nextSymbol();
			Expr astSndExpr = parsePrefixExpression();
			result = parseMultiplicativeExpressionRest(
					new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.DIV, astFstExpr, astSndExpr));
			break;
		}
		case MOD: {
			nextSymbol();
			Expr astSndExpr = parsePrefixExpression();
			result = parseMultiplicativeExpressionRest(
					new BinExpr(new Position(astFstExpr, astSndExpr), BinExpr.Oper.MOD, astFstExpr, astSndExpr));
			break;
		}
		default:
			result = astFstExpr;
			break;
		}
		endLog();
		return result;
	}

	private Expr parsePrefixExpression() {
		Expr result;
		begLog("PrefixExpression");
		switch (laSymbol.token) {
		case ADD: {
			Symbol symADD = nextSymbol();
			Expr astExpr = parsePrefixExpression();
			result = new UnExpr(new Position(symADD, astExpr), UnExpr.Oper.ADD, astExpr);
			break;
		}
		case SUB: {
			Symbol symSUB = nextSymbol();
			Expr astExpr = parsePrefixExpression();
			result = new UnExpr(new Position(symSUB, astExpr), UnExpr.Oper.SUB, astExpr);
			break;
		}
		case NOT: {
			Symbol symNOT = nextSymbol();
			Expr astExpr = parsePrefixExpression();
			result = new UnExpr(new Position(symNOT, astExpr), UnExpr.Oper.NOT, astExpr);
			break;
		}
		case MEM: {
			Symbol symMEM = nextSymbol();
			Expr astExpr = parsePrefixExpression();
			result = new UnExpr(new Position(symMEM, astExpr), UnExpr.Oper.MEM, astExpr);
			break;
		}
		case OPENING_BRACKET: {
			Symbol symOPENING_BRACKET = nextSymbol();
			Type astType = parseType();
			if (laSymbol.token == Symbol.Token.CLOSING_BRACKET) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol ']' inserted.");
				nextSymbolIsError();
			}
			Expr astExpr = parsePrefixExpression();
			result = new CastExpr(new Position(symOPENING_BRACKET, astExpr), astType, astExpr);
			break;
		}
		default:
			result = parsePostfixExpression();
			break;
		}
		endLog();
		return result;
	}

	private Expr parsePostfixExpression() {
		Expr result;
		begLog("PostfixExpression");
		Expr astExpr = parseAtomicExpression();
		result = parsePostfixExpressionRest(astExpr);
		endLog();
		return result;
	}

	private Expr parsePostfixExpressionRest(Expr astFstExpr) {
		Expr result;
		begLog("PostfixExpressionRest");
		switch (laSymbol.token) {
		case OPENING_BRACKET: {
			nextSymbol();
			Expr astSndExpr = parseExpression();
			Symbol symCLOSING_BRACKET;
			if (laSymbol.token == Symbol.Token.CLOSING_BRACKET) {
				symCLOSING_BRACKET = nextSymbol();
			} else {
				Report.warning(laSymbol, "Symbol ']' inserted.");
				symCLOSING_BRACKET = nextSymbolIsError();
			}
			result = parsePostfixExpressionRest(new BinExpr(new Position(astFstExpr, symCLOSING_BRACKET),
					BinExpr.Oper.ARR, astFstExpr, astSndExpr));
			break;
		}
		case DOT: {
			nextSymbol();
			Symbol symIDENTIFIER;
			if (laSymbol.token == Symbol.Token.IDENTIFIER) {
				symIDENTIFIER = nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing identifier inserted.");
				symIDENTIFIER = nextSymbolIsError();
			}
			CompName astCompName = new CompName(new Position(symIDENTIFIER), symIDENTIFIER.lexeme);
			result = parsePostfixExpressionRest(
					new BinExpr(new Position(astFstExpr, astCompName), BinExpr.Oper.REC, astFstExpr, astCompName));
			break;
		}
		case VAL: {
			Symbol symVAL = nextSymbol();
			result = parsePostfixExpressionRest(
					new UnExpr(new Position(astFstExpr, symVAL), UnExpr.Oper.VAL, astFstExpr));
			break;
		}
		default:
			result = astFstExpr;
			break;
		}
		endLog();
		return result;
	}

	private Expr parseAtomicExpression() {
		Expr result;
		begLog("AtomicExpression");
		switch (laSymbol.token) {
		case CONST_INTEGER: {
			Symbol symCONST_INTEGER = nextSymbol();
			result = new AtomExpr((Position) symCONST_INTEGER, AtomExpr.AtomTypes.INTEGER, symCONST_INTEGER.lexeme);
			break;
		}
		case CONST_BOOLEAN: {
			Symbol symCONST_BOOLEAN = nextSymbol();
			result = new AtomExpr((Position) symCONST_BOOLEAN, AtomExpr.AtomTypes.BOOLEAN, symCONST_BOOLEAN.lexeme);
			break;
		}
		case CONST_CHAR: {
			Symbol symCONST_CHAR = nextSymbol();
			result = new AtomExpr((Position) symCONST_CHAR, AtomExpr.AtomTypes.CHAR, symCONST_CHAR.lexeme);
			break;
		}
		case CONST_STRING: {
			Symbol symCONST_STRING = nextSymbol();
			result = new AtomExpr((Position) symCONST_STRING, AtomExpr.AtomTypes.STRING, symCONST_STRING.lexeme);
			break;
		}
		case CONST_NULL: {
			Symbol symCONST_NULL = nextSymbol();
			result = new AtomExpr((Position) symCONST_NULL, AtomExpr.AtomTypes.PTR, null);
			break;
		}
		case CONST_NONE: {
			Symbol symCONST_NONE = nextSymbol();
			result = new AtomExpr((Position) symCONST_NONE, AtomExpr.AtomTypes.VOID, null);
			break;
		}
		case IDENTIFIER: {
			Symbol symIDENTIFIER = nextSymbol();
			result = parseArgumentsOpt(symIDENTIFIER);
			break;
		}
		case OPENING_PARENTHESIS: {
			Symbol symOPENING_PARENTHESIS = nextSymbol();
			LinkedList<Expr> astExprs = parseExpressions();
			Symbol symCLOSING_PARENTHESIS;
			if (laSymbol.token == Symbol.Token.CLOSING_PARENTHESIS) {
				symCLOSING_PARENTHESIS = nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol ')' inserted.");
				symCLOSING_PARENTHESIS = nextSymbolIsError();
			}
			if (astExprs.size() == 1)
				result = astExprs.get(0);
			else
				result = new Exprs(new Position(symOPENING_PARENTHESIS, symCLOSING_PARENTHESIS), astExprs);
			break;
		}
		case IF: {
			Symbol symIF = nextSymbol();
			Expr astCond = parseExpression();
			if (laSymbol.token == Symbol.Token.THEN) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing keyword 'then' inserted.");
				nextSymbolIsError();
			}
			Expr astThenExpr = parseExpression();
			if (laSymbol.token == Symbol.Token.ELSE) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing keyword 'else' inserted.");
				nextSymbolIsError();
			}
			Expr astElseExpr = parseExpression();
			Symbol symEND;
			if (laSymbol.token == Symbol.Token.END) {
				symEND = nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing keyword 'end' inserted.");
				symEND = nextSymbolIsError();
			}
			result = new IfExpr(new Position(symIF, symEND), astCond, astThenExpr, astElseExpr);
			break;
		}
		case FOR: {
			Symbol symFOR = nextSymbol();
			Symbol symIDENTIFIER;
			if (laSymbol.token == Symbol.Token.IDENTIFIER) {
				symIDENTIFIER = nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing identifier inserted.");
				symIDENTIFIER = nextSymbolIsError();
			}
			if (laSymbol.token == Symbol.Token.ASSIGN) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol '=' inserted.");
				nextSymbolIsError();
			}
			Expr astLoBound = parseExpression();
			if (laSymbol.token == Symbol.Token.COMMA) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol ',' inserted.");
				nextSymbolIsError();
			}
			Expr astHiBound = parseExpression();
			if (laSymbol.token == Symbol.Token.COLON) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing keyword ':' inserted.");
				nextSymbolIsError();
			}
			Expr astBody = parseExpression();
			Symbol symEND;
			if (laSymbol.token == Symbol.Token.END) {
				symEND = nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing keyword 'end' inserted.");
				symEND = nextSymbolIsError();
			}
			result = new ForExpr(new Position(symFOR, symEND),
					new VarName((Position) symIDENTIFIER, symIDENTIFIER.lexeme), astLoBound, astHiBound, astBody);
			break;
		}
		case WHILE: {
			Symbol symWHILE = nextSymbol();
			Expr astCond = parseExpression();
			if (laSymbol.token == Symbol.Token.COLON) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol ':' inserted.");
				nextSymbolIsError();
			}
			Expr astBody = parseExpression();
			Symbol symEND;
			if (laSymbol.token == Symbol.Token.END) {
				symEND = nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing keyword 'end' inserted.");
				symEND = nextSymbolIsError();
			}
			result = new WhileExpr(new Position(symWHILE, symEND), astCond, astBody);
			break;
		}
		default:
			Report.warning(laSymbol, "Missing expression inserted.");
			nextSymbolIsError();
			result = new ExprError();
		}
		endLog();
		return result;
	}

	private Expr parseArgumentsOpt(Symbol symIDENTIFIER) {
		Expr result;
		begLog("ArgumentsOpt");
		switch (laSymbol.token) {
		case OPENING_PARENTHESIS:
			nextSymbol();
			result = parseArgumentsOptRest(symIDENTIFIER);
			break;
		default:
			result = new VarName((Position) symIDENTIFIER, symIDENTIFIER.lexeme);
			break;
		}
		endLog();
		return result;
	}
	
	private Expr parseArgumentsOptRest(Symbol symIDENTIFIER) {
		Expr result;
		begLog("ArgumentsOpt");
		Symbol symCLOSING_PARENTHESIS;
		switch (laSymbol.token) {
		case CLOSING_PARENTHESIS:
			symCLOSING_PARENTHESIS = nextSymbol();
			result = new FunCall(new Position(symIDENTIFIER, symCLOSING_PARENTHESIS), symIDENTIFIER.lexeme, new LinkedList<Expr>());
			break;
		default:
			LinkedList<Expr> astExprs = parseExpressions();
			if (laSymbol.token == Symbol.Token.CLOSING_PARENTHESIS) {
				symCLOSING_PARENTHESIS = nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol ')' inserted.");
				symCLOSING_PARENTHESIS = nextSymbolIsError();
			}
			result = new FunCall(new Position(symIDENTIFIER, symCLOSING_PARENTHESIS), symIDENTIFIER.lexeme, astExprs);
		}
		endLog();
		return result;
	}

	private LinkedList<Decl> parseDeclarations() {
		LinkedList<Decl> result;
		begLog("Declarations");
		Decl astDecl = parseDeclaration();
		result = parseDeclarationsRest();
		result.addFirst(astDecl);
		endLog();
		return result;
	}

	private LinkedList<Decl> parseDeclarationsRest() {
		LinkedList<Decl> result;
		begLog("DeclarationsRest");
		switch (laSymbol.token) {
		case TYP:
		case FUN:
		case VAR:
			Decl astDecl = parseDeclaration();
			result = parseDeclarationsRest();
			result.addFirst(astDecl);
			break;
		default:
			result = new LinkedList<Decl>();
		}
		endLog();
		return result;
	}

	private Decl parseDeclaration() {
		Decl result;
		begLog("Declaration");
		switch (laSymbol.token) {
		case TYP:
			result = parseTypeDeclaration();
			break;
		case FUN:
			result = parseFunctionDeclaration();
			break;
		case VAR:
			result = parseVariableDeclaration();
			break;
		default:
			Report.warning(laSymbol, "Missing declaration inserted.");
			nextSymbolIsError();
			result = new DeclError();
		}
		endLog();
		return result;
	}

	private TypeDecl parseTypeDeclaration() {
		TypeDecl result;
		begLog("TypeDeclaration");
		switch (laSymbol.token) {
		case TYP: {
			Symbol symTYP = nextSymbol();
			Symbol symIDENTIFIER;
			if (laSymbol.token == Symbol.Token.IDENTIFIER) {
				symIDENTIFIER = nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing identifier inserted.");
				symIDENTIFIER = nextSymbolIsError();
			}
			if (laSymbol.token == Symbol.Token.COLON) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol ':' inserted.");
				nextSymbolIsError();
			}
			Type astType = parseType();
			result = new TypeDecl(new Position(symTYP, astType), symIDENTIFIER.lexeme, astType);
			break;
		}
		default:
			throw new InternalCompilerError();
		}
		endLog();
		return result;
	}

	private FunDecl parseFunctionDeclaration() {
		FunDecl result;
		begLog("FunctionDeclaration");
		switch (laSymbol.token) {
		case FUN: {
			Symbol symFUN = nextSymbol();
			Symbol symIDENTIFIER;
			if (laSymbol.token == Symbol.Token.IDENTIFIER) {
				symIDENTIFIER = nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing identifier inserted.");
				symIDENTIFIER = nextSymbolIsError();
			}
			if (laSymbol.token == Symbol.Token.OPENING_PARENTHESIS) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol '(' inserted.");
				nextSymbolIsError();
			}
			LinkedList<ParDecl> astPars = parseParametersOpt();
			if (laSymbol.token == Symbol.Token.CLOSING_PARENTHESIS) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol ')' inserted.");
				nextSymbolIsError();
			}
			if (laSymbol.token == Symbol.Token.COLON) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol ':' inserted.");
				nextSymbolIsError();
			}
			Type astType = parseType();
			switch (laSymbol.token) {
			case ASSIGN:
				Expr astBody = parseFunctionBodyOpt();
				result = new FunDef(new Position(symFUN, astBody), symIDENTIFIER.lexeme, astPars, astType, astBody);
				break;
			default:
				result = new FunDecl(new Position(symFUN, astType), symIDENTIFIER.lexeme, astPars, astType);
			}
			break;
		}
		default:
			throw new InternalCompilerError();
		}
		endLog();
		return result;
	}

	private Expr parseFunctionBodyOpt() {
		Expr result;
		begLog("FunctionBodyOpt");
		switch (laSymbol.token) {
		case ASSIGN:
			nextSymbol();
			result = parseExpression();
			break;
		default:
			throw new InternalCompilerError();
		}
		endLog();
		return result;
	}

	private LinkedList<ParDecl> parseParametersOpt() {
		LinkedList<ParDecl> result;
		begLog("ParametersOpt");
		switch (laSymbol.token) {
		case IDENTIFIER:
			result = parseParameters();
			break;
		default:
			result = new LinkedList<ParDecl>();
		}
		endLog();
		return result;
	}

	private LinkedList<ParDecl> parseParameters() {
		LinkedList<ParDecl> result;
		begLog("Parameters");
		ParDecl astPar = parseParameter();
		result = parseParametersRest();
		result.addFirst(astPar);
		endLog();
		return result;
	}

	private LinkedList<ParDecl> parseParametersRest() {
		LinkedList<ParDecl> result;
		begLog("ParametersRest");
		switch (laSymbol.token) {
		case COMMA:
			nextSymbol();
			ParDecl astPar = parseParameter();
			result = parseParametersRest();
			result.addFirst(astPar);
			break;
		default:
			result = new LinkedList<ParDecl>();
		}
		endLog();
		return result;
	}

	private ParDecl parseParameter() {
		ParDecl result;
		begLog("Parameter");
		Symbol symIDENTIFIER;
		if (laSymbol.token == Symbol.Token.IDENTIFIER) {
			symIDENTIFIER = nextSymbol();
		} else {
			Report.warning(laSymbol, "Missing identifier inserted.");
			symIDENTIFIER = nextSymbolIsError();
		}
		if (laSymbol.token == Symbol.Token.COLON) {
			nextSymbol();
		} else {
			Report.warning(laSymbol, "Missing symbol ':' inserted.");
			nextSymbolIsError();
		}
		Type astType = parseType();
		result = new ParDecl(new Position(symIDENTIFIER, astType), symIDENTIFIER.lexeme, astType);
		endLog();
		return result;
	}

	private VarDecl parseVariableDeclaration() {
		VarDecl result;
		begLog("VariableDeclaration");
		switch (laSymbol.token) {
		case VAR: {
			Symbol symVar = nextSymbol();
			Symbol symId;
			if (laSymbol.token == Symbol.Token.IDENTIFIER) {
				symId = nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing identifier inserted.");
				symId = nextSymbolIsError();
			}
			if (laSymbol.token == Symbol.Token.COLON) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol ':' inserted.");
				nextSymbolIsError();
			}
			Type astType = parseType();
			result = new VarDecl(new Position(symVar, astType), symId.lexeme, astType);
			break;
		}
		default:
			throw new InternalCompilerError();
		}
		endLog();
		return result;
	}

	private Type parseType() {
		Type result;
		begLog("Type");
		switch (laSymbol.token) {
		case INTEGER: {
			Symbol symINTEGER = nextSymbol();
			result = new AtomType((Position) symINTEGER, AtomType.AtomTypes.INTEGER);
			break;
		}
		case BOOLEAN: {
			Symbol symBOOLEAN = nextSymbol();
			result = new AtomType((Position) symBOOLEAN, AtomType.AtomTypes.BOOLEAN);
			break;
		}
		case CHAR: {
			Symbol symCHAR = nextSymbol();
			result = new AtomType((Position) symCHAR, AtomType.AtomTypes.CHAR);
			break;
		}
		case STRING: {
			Symbol symSTRING = nextSymbol();
			result = new AtomType((Position) symSTRING, AtomType.AtomTypes.STRING);
			break;
		}
		case VOID: {
			Symbol symVOID = nextSymbol();
			result = new AtomType((Position) symVOID, AtomType.AtomTypes.VOID);
			break;
		}
		case ARR: {
			Symbol symARR = nextSymbol();
			if (laSymbol.token == Symbol.Token.OPENING_BRACKET) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol '[' inserted.");
				nextSymbolIsError();
			}
			Expr astSize = parseExpression();
			if (laSymbol.token == Symbol.Token.CLOSING_BRACKET) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol ']' inserted.");
				nextSymbolIsError();
			}
			Type astType = parseType();
			result = new ArrType(new Position(symARR, astType), astSize, astType);
			break;
		}
		case REC: {
			Symbol symREC = nextSymbol();
			if (laSymbol.token == Symbol.Token.OPENING_BRACE) {
				nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol '{' inserted.");
				nextSymbolIsError();
			}
			LinkedList<CompDecl> astComps = parseComponents();
			Symbol symCLOSING_BRACE;
			if (laSymbol.token == Symbol.Token.CLOSING_BRACE) {
				symCLOSING_BRACE = nextSymbol();
			} else {
				Report.warning(laSymbol, "Missing symbol '}' inserted.");
				symCLOSING_BRACE = nextSymbolIsError();
			}
			result = new RecType(new Position(symREC, symCLOSING_BRACE), astComps);
			break;
		}
		case PTR: {
			Symbol symPTR = nextSymbol();
			Type astType = parseType();
			result = new PtrType(new Position(symPTR, astType), astType);
			break;
		}
		case IDENTIFIER: {
			Symbol symIDENTIFIER = nextSymbol();
			result = new TypeName((Position) symIDENTIFIER, symIDENTIFIER.lexeme);
			break;
		}
		default:
			Report.warning(laSymbol, "Missing type expression inserted.");
			nextSymbolIsError();
			result = new TypeError();
		}
		endLog();
		return result;
	}

	private LinkedList<CompDecl> parseComponents() {
		LinkedList<CompDecl> result;
		begLog("Components");
		CompDecl astComp = parseComponent();
		result = parseComponentsRest();
		result.addFirst(astComp);
		endLog();
		return result;
	}

	private LinkedList<CompDecl> parseComponentsRest() {
		LinkedList<CompDecl> result;
		begLog("ComponentsRest");
		switch (laSymbol.token) {
		case COMMA:
			nextSymbol();
			CompDecl astComp = parseComponent();
			result = parseComponentsRest();
			result.addFirst(astComp);
			break;
		default:
			result = new LinkedList<CompDecl>();
		}
		endLog();
		return result;
	}

	private CompDecl parseComponent() {
		CompDecl result;
		begLog("Component");
		Symbol symIDENTIFIER;
		if (laSymbol.token == Symbol.Token.IDENTIFIER) {
			symIDENTIFIER = nextSymbol();
		} else {
			Report.warning(laSymbol, "Missing identifier inserted.");
			symIDENTIFIER = nextSymbolIsError();
		}
		if (laSymbol.token == Symbol.Token.COLON) {
			nextSymbol();
		} else {
			Report.warning(laSymbol, "Missing symbol ':' inserted.");
			nextSymbolIsError();
		}
		Type astType = parseType();
		result = new CompDecl(new Position(symIDENTIFIER, astType), symIDENTIFIER.lexeme, astType);
		endLog();
		return result;
	}

}
