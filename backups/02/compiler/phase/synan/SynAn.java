package compiler.phase.synan;

import compiler.Task;
import compiler.common.logger.Transformer;
import compiler.common.report.PhaseErrors.SynAnError;
import compiler.common.report.Position;
import compiler.common.report.Report;
import compiler.phase.Phase;
import compiler.phase.lexan.LexAn;
import compiler.phase.lexan.Symbol;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;

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
	 * @ param lexAn
	 *            The lexical analyzer.
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
	 */
	public void synAn() {
		laSymbol = lexAn.lexAn();
		parseProgram();
		if (laSymbol.token != Symbol.Token.EOF)
			Report.warning(laSymbol, "Unexpected symbol(s) at the end of file.");
	}


	private Symbol skip(Symbol.Token token){
		if (laSymbol.token == token) {
			return nextSymbol();
		} else {
			throw new SynAnError("[skip] expected " + token + " got " + laSymbol.token);
		}
	}


	private void signalError(String funName) {
		throw new SynAnError("[" + funName + "]" + laSymbol.getPosition() + " | " + laSymbol.token );
	}

	private void signalError(String funName, String message) {
		throw new SynAnError("[" + funName + "] : " + message);
	}

	// All these methods are a part of a recursive descent implementation of an
	// LL(1) parser.

	//Program -> Expression .
	private void parseProgram() {
		begLog("Program");
		parseExpression();


		if(laSymbol.token != Symbol.Token.EOF){
			throw new SynAnError("Symbol at the end is not EOF");
		}

		endLog();
	}

	//Expression -> AssignmentExpression ExpressionPrime .
	private void parseExpression(){
		begLog("Expression");
		parseAssignmentExpression();
		parseExpressionPrime();
		endLog();
	}

	//ExpressionPrime -> where Declarations end ExpressionPrime .
	//ExpressionPrime -> .
	private void parseExpressionPrime(){
		begLog("ExpressionPrime");
		switch(laSymbol.token){
			case WHERE :
				skip(Symbol.Token.WHERE);
				parseDeclarations();
				skip(Symbol.Token.END);
				parseExpressionPrime();
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
				break;

			default:
				signalError("ExpressionPrime");

		}
		endLog();
	}


	//Expressions -> Expression ExpressionsPrime .
	private void parseExpressions(){
		begLog("Expressions");
		parseExpression();
		parseExpressionsPrime();
		endLog();
	}

	//ExpressionsPrime -> comma Expression ExpressionsPrime .
	//ExpressionsPrime -> .
	private void parseExpressionsPrime(){
		begLog("ExpressionsPrime");
		switch(laSymbol.token){
			case COMMA:
				skip(Symbol.Token.COMMA);
				parseExpression();
				parseExpressionPrime();
				break;
			case CLOSING_PARENTHESIS:
				break;
			default:
				signalError("ExpressionsPrime");
		}
		endLog();
	}

	//AssignmentExpression -> DisjunctiveExpression AssignmentExpressionPrime .
	private void parseAssignmentExpression(){
		begLog("AssignmentExpression");
		parseDisjunctiveExpression();
		parseAssignmentExpressionPrime();
		endLog();
	}


	//AssignmentExpressionPrime -> .
	//AssignmentExpressionPrime -> assign DisjunctiveExpression .
	private void parseAssignmentExpressionPrime(){
		begLog("AssignmentExpressionPrime");
		switch (laSymbol.token){
			case ASSIGN:
				skip(Symbol.Token.ASSIGN);
				parseDisjunctiveExpression();
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
				break;

			default:
				signalError("AssignmentExpressionPrime");
		}

		endLog();
	}

	//DisjunctiveExpression -> ConjunctiveExpression DisjunctiveExpressionPrime .
	private void parseDisjunctiveExpression(){
		begLog("DisjunctiveExpression");
		parseConjunctiveExpression();
		parseDisjunctiveExpressionPrime();
		endLog();
	}

	//DisjunctiveExpressionPrime -> or ConjunctiveExpression DisjunctiveExpressionPrime .
	//DisjunctiveExpressionPrime -> .
	private void parseDisjunctiveExpressionPrime(){
		begLog("DisjunctiveExpressionPrime");
		switch(laSymbol.token){
			case OR:
				skip(Symbol.Token.OR);
				parseConjunctiveExpression();
				parseDisjunctiveExpressionPrime();
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
				break;
			default:
				signalError("DisjunctiveExpressionPrime");
		}
		endLog();
	}

	//ConjunctiveExpression -> RelationalExpression ConjunctiveExpressionPrime .
	private void parseConjunctiveExpression(){
		begLog("ConjunctiveExpression");
		parseRelationalExpression();
		parseConjunctiveExpressionPrime();
		endLog();
	}

	//ConjunctiveExpressionPrime -> and RelationalExpression ConjunctiveExpressionPrime .
	//ConjunctiveExpressionPrime -> .
	private void parseConjunctiveExpressionPrime(){
		begLog("ConjunctiveExpressionPrime");
		switch(laSymbol.token){
			case AND:
				skip(Symbol.Token.AND);
				parseRelationalExpression();
				parseConjunctiveExpressionPrime();
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
				break;
			default:
				signalError("ConjunctiveExpressionPrime");
		}
		endLog();
	}

	//RelationalExpression -> AdditiveExpression RelationalExpressionPrime .
	private void parseRelationalExpression(){
		begLog("RelationalExpression");
		parseAdditiveExpression();
		parseRelationalExpressionPrime();
		endLog();
	}

	//RelationalExpressionPrime -> .
	//RelationalExpressionPrime -> equ AdditiveExpression .
	//RelationalExpressionPrime -> neq AdditiveExpression .
	//RelationalExpressionPrime -> lth AdditiveExpression .
	//RelationalExpressionPrime -> gth AdditiveExpression .
	//RelationalExpressionPrime -> leq AdditiveExpression .
	//RelationalExpressionPrime -> geq AdditiveExpression .
	private void parseRelationalExpressionPrime(){
		begLog("RelationalExpressionPrime");
		switch(laSymbol.token){
			case EQU:
				skip(Symbol.Token.EQU);
				parseAdditiveExpression();
				break;
			case NEQ:
				skip(Symbol.Token.NEQ);
				parseAdditiveExpression();
				break;
			case LTH:
				skip(Symbol.Token.LTH);
				parseAdditiveExpression();
				break;
			case GTH:
				skip(Symbol.Token.GTH);
				parseAdditiveExpression();
				break;
			case LEQ:
				skip(Symbol.Token.LEQ);
				parseAdditiveExpression();
				break;
			case GEQ:
				skip(Symbol.Token.GEQ);
				parseAdditiveExpression();
				break;

			case WHERE :
			case END :
			case COMMA :
			case ASSIGN :
			case OR :
			case AND :
			case CLOSING_BRACKET :
			case CLOSING_PARENTHESIS :
			case THEN :
			case ELSE :
			case COLON :
			case TYP :
			case FUN :
			case VAR :
			case EOF :
				break;
			default:
				signalError("RelationalExpressionPrime");
		}
		endLog();
	}

	//AdditiveExpression -> MultiplicativeExpression AdditiveExpressionPrime .
	private void parseAdditiveExpression(){
		begLog("AdditiveExpression");
		parseMultiplicativeExpression();
		parseAdditiveExpressionPrime();
		endLog();
	}

	//AdditiveExpressionPrime -> add MultiplicativeExpression AdditiveExpressionPrime .
	//AdditiveExpressionPrime -> sub MultiplicativeExpression AdditiveExpressionPrime .
	//AdditiveExpressionPrime -> .
	private void parseAdditiveExpressionPrime(){
		begLog("AdditiveExpressionPrime");
		switch(laSymbol.token){
			case ADD :
				skip(Symbol.Token.ADD);
				parseMultiplicativeExpression();
				parseAdditiveExpressionPrime();
				break;
			case SUB :
				skip(Symbol.Token.SUB);
				parseMultiplicativeExpression();
				parseAdditiveExpressionPrime();
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
				break;

			default:
				signalError("AdditiveExpressionPrime");

		}
		endLog();
	}

	//MultiplicativeExpression -> PrefixExpression MultiplicativeExpressionPrime .
	private void parseMultiplicativeExpression(){
		begLog("MultiplicativeExpression");
		parsePrefixExpression();
		parseMultiplicativeExpressionPrime();
		endLog();
	}

	//MultiplicativeExpressionPrime -> mul PrefixExpression MultiplicativeExpressionPrime .
	//MultiplicativeExpressionPrime -> div PrefixExpression MultiplicativeExpressionPrime .
	//MultiplicativeExpressionPrime -> mod PrefixExpression MultiplicativeExpressionPrime .
	//MultiplicativeExpressionPrime -> .
	private void parseMultiplicativeExpressionPrime(){
		begLog("MultiplicativeExpressionPrime");
		switch (laSymbol.token){
			case MUL :
				skip(Symbol.Token.MUL);
				parsePrefixExpression();
				parseMultiplicativeExpressionPrime();
				break;
			case DIV :
				skip(Symbol.Token.DIV);
				parsePrefixExpression();
				parseMultiplicativeExpressionPrime();
				break;
			case MOD :
				skip(Symbol.Token.MOD);
				parsePrefixExpression();
				parseMultiplicativeExpressionPrime();
				break;

			case WHERE :
			case END :
			case COMMA :
			case ASSIGN :
			case OR :
			case AND :
			case EQU :
			case NEQ :
			case LTH :
			case GTH :
			case LEQ :
			case GEQ :
			case ADD :
			case SUB :
			case CLOSING_BRACKET :
			case CLOSING_PARENTHESIS :
			case THEN :
			case ELSE :
			case COLON :
			case TYP :
			case FUN :
			case VAR :
			case EOF :
				break;

			default:
				signalError("MultiplicativeExpressionPrime");
		}
		endLog();
	}

	//PrefixExpression -> PostfixExpression .
	//PrefixExpression -> add PrefixExpression .
	//PrefixExpression -> sub PrefixExpression .
	//PrefixExpression -> not PrefixExpression .
	//PrefixExpression -> mem PrefixExpression .
	//PrefixExpression -> lbracket Type rbracket PrefixExpression .
	private void parsePrefixExpression(){
		begLog("PrefixExpression");
		switch(laSymbol.token){
			case ADD:
				skip(Symbol.Token.ADD);
				parsePrefixExpression();
				break;
			case SUB:
				skip(Symbol.Token.SUB);
				parsePrefixExpression();
				break;
			case NOT:
				skip(Symbol.Token.NOT);
				parsePrefixExpression();
				break;
			case MEM:
				skip(Symbol.Token.MEM);
				parsePrefixExpression();
				break;
			case OPENING_BRACKET:
				skip(Symbol.Token.OPENING_BRACKET);
				parseType();
				skip(Symbol.Token.CLOSING_BRACKET);
				parsePrefixExpression();
				break;
			default:
				parsePostfixExpression();
				break;
		}
		endLog();
	}

	//PostfixExpression -> AtomicExpression PostfixExpressionPrime .
	private void parsePostfixExpression(){
		begLog("PostfixExpression");
		parseAtomicExpression();
		parsePostfixExpressionPrime();
		endLog();
	}

	//PostfixExpressionPrime -> lbracket Expression rbracket PostfixExpressionPrime .
	//PostfixExpressionPrime -> dot IDENTIFIER PostfixExpressionPrime .
	//PostfixExpressionPrime -> val PostfixExpressionPrime .
	//PostfixExpressionPrime -> .
	private void parsePostfixExpressionPrime(){
		begLog("PostfixExpressionPrime");
		switch(laSymbol.token){
			case OPENING_BRACKET:
				skip(Symbol.Token.OPENING_BRACKET);
				parseExpression();
				skip(Symbol.Token.CLOSING_BRACKET);
				parsePostfixExpressionPrime();
				break;

			case DOT:
				skip(Symbol.Token.DOT);
				skip(Symbol.Token.IDENTIFIER);
				parsePostfixExpressionPrime();
				break;
			case VAL:
				skip(Symbol.Token.VAL);
				parsePostfixExpressionPrime();
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
				break;

			default:
				signalError("PostfixExpressionPrime");
		}
		endLog();
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
	private void parseAtomicExpression(){
		begLog("AtomicExpression");
		switch(laSymbol.token){
			case CONST_INTEGER:
				skip(Symbol.Token.CONST_INTEGER);
				break;
			case BOOLEAN:
				skip(Symbol.Token.CONST_BOOLEAN);
				break;
			case CONST_CHAR:
				skip(Symbol.Token.CONST_CHAR);
				break;
			case CONST_STRING:
				skip(Symbol.Token.CONST_STRING);
				break;
			case CONST_NULL:
				skip(Symbol.Token.CONST_NULL);
				break;
			case CONST_NONE:
				skip(Symbol.Token.CONST_NULL);
				break;
			case IDENTIFIER:
				skip(Symbol.Token.IDENTIFIER);
				parseArgumentsOpt();
				break;
			case OPENING_PARENTHESIS:
				skip(Symbol.Token.OPENING_PARENTHESIS);
				parseExpressions();
				skip(Symbol.Token.CLOSING_PARENTHESIS);
				break;
			//AtomicExpression -> if Expression then Expression else Expression end .
			case IF:
				skip(Symbol.Token.IF);
				parseExpression();
				skip(Symbol.Token.THEN);
				parseExpression();
				skip(Symbol.Token.ELSE);
				parseExpression();
				skip(Symbol.Token.END);
				break;
			//AtomicExpression -> for IDENTIFIER assign Expression comma Expression colon Expression end .
			case FOR:
				skip(Symbol.Token.FOR);
				skip(Symbol.Token.IDENTIFIER);
				skip(Symbol.Token.ASSIGN);
				parseExpression();
				skip(Symbol.Token.COMMA);
				parseExpression();
				skip(Symbol.Token.COLON);
				parseExpression();
				skip(Symbol.Token.END);
				break;

			//AtomicExpression -> while Expression colon Expression end .
			case WHILE:
				skip(Symbol.Token.WHILE);
				parseExpression();
				skip(Symbol.Token.COLON);
				parseExpression();
				skip(Symbol.Token.END);
				break;

			default:
				signalError("AtomicExpression");

		}

		endLog();
	}

	//ArgumentsOpt -> .
	//ArgumentsOpt -> lparen Expressions rparen .
	private void parseArgumentsOpt(){
		begLog("ArgumentsOpt");

		switch (laSymbol.token){
			case OPENING_PARENTHESIS:
				skip(Symbol.Token.OPENING_PARENTHESIS);
				parseExpressions();
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
				break;
			default:
				signalError("ArgumentsOpt");
		}

		endLog();
	}

	//Declarations -> Declaration DeclarationsPrime .
	private void parseDeclarations(){
		begLog("Declarations");
		parseDeclaration();
		parseDeclarationsPrime();
		endLog();
	}

	//DeclarationsPrime -> Declaration DeclarationsPrime .
	//DeclarationsPrime -> .
	private void parseDeclarationsPrime(){
		begLog("DeclarationsPrime");
		switch(laSymbol.token){
			case END:
				break;
			case TYP:
			case FUN:
			case VAR:
				parseDeclaration();
				parseDeclarationsPrime();
				break;
			default:
				signalError("DeclarationsPrime");
		}

		endLog();
	}

	//Declaration -> TypeDeclaration .
	//Declaration -> FunctionDeclaration .
	//Declaration -> VariableDeclaration .
	private void parseDeclaration(){
		begLog("Declaration");
			switch(laSymbol.token){
				case TYP:
					parseTypeDeclaration();
					break;
				case FUN:
					parseFunctionDeclaration();
					break;
				case VAR:
					parseVariableDeclaration();
					break;
				default:
					signalError("Declaration");
			}
		endLog();
	}

	//TypeDeclaration -> typ IDENTIFIER colon Type .
	private void parseTypeDeclaration(){
		begLog("TypeDeclaration");
		skip(Symbol.Token.TYP);
		skip(Symbol.Token.IDENTIFIER);
		skip(Symbol.Token.COLON);
		parseType();
		endLog();
	}

	//FunctionDeclaration -> fun IDENTIFIER lparen ParametersOpt rparen colon Type FunctionBodyOpt .
	private void parseFunctionDeclaration(){
		begLog("FunctionDeclaration");
		skip(Symbol.Token.FUN);
		skip(Symbol.Token.IDENTIFIER);
		skip(Symbol.Token.OPENING_PARENTHESIS);
		parseParametersOpt();
		skip(Symbol.Token.CLOSING_PARENTHESIS);
		skip(Symbol.Token.COLON);
		parseType();
		parseFunctionBodyOpt();
		endLog();
	}

	//ParametersOpt -> .
	//ParametersOpt -> Parameters .
	private void parseParametersOpt(){
		begLog("ParametersOpt");
		switch (laSymbol.token){
			case IDENTIFIER:
				parseParameters();
				break;
			case CLOSING_PARENTHESIS:
				break;
			default:
				signalError("ParametersOpt");
		}
		endLog();
	}

	//Parameters -> Parameter ParametersPrime .
	private void parseParameters(){
		begLog("Parameters");
		parseParameter();
		parseParametersPrime();
		endLog();
	}

	//ParametersPrime -> comma Parameter ParametersPrime .
	//ParametersPrime -> .
	private void parseParametersPrime(){
		begLog("ParametersPrime");
		switch (laSymbol.token){
			case COMMA:
				skip(Symbol.Token.COMMA);
				parseParameter();
				parseParametersPrime();
				break;

			case CLOSING_PARENTHESIS:
				break;
			default:
				signalError("ParametersPrime");
		}
		endLog();
	}

	//Parameter -> IDENTIFIER colon Type .
	private void parseParameter(){
		begLog("Parameter");
		skip(Symbol.Token.IDENTIFIER);
		skip(Symbol.Token.COLON);
		parseType();
		endLog();
	}

	//FunctionBodyOpt -> .
	//FunctionBodyOpt -> assign Expression .
	private void parseFunctionBodyOpt(){
		begLog("FunctionBodyOpt");
		switch (laSymbol.token) {
			case ASSIGN:
				skip(Symbol.Token.ASSIGN);
				parseExpression();
			case END:
			case TYP:
			case FUN:
			case VAR:
				break;
			default:
				signalError("FunctionBodyOpt");
		}
		endLog();
	}

	//VariableDeclaration -> var IDENTIFIER colon Type .
	private void parseVariableDeclaration(){
		begLog("VariableDeclaration");
		skip(Symbol.Token.VAR);
		skip(Symbol.Token.IDENTIFIER);
		skip(Symbol.Token.COLON);
		parseType();
		endLog();
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
	private void parseType(){
		begLog("Type");
		switch(laSymbol.token){
			case INTEGER:
				skip(Symbol.Token.INTEGER);
				break;
			case BOOLEAN:
				skip(Symbol.Token.BOOLEAN);
				break;
			case CHAR:
				skip(Symbol.Token.CHAR);
				break;
			case STRING:
				skip(Symbol.Token.STRING);
				break;
			case VOID:
				skip(Symbol.Token.VOID);
				break;
			case ARR:
				skip(Symbol.Token.ARR);
				skip(Symbol.Token.OPENING_BRACKET);
				parseExpression();
				skip(Symbol.Token.CLOSING_BRACKET);
				parseType();
				break;
			case REC:
				skip(Symbol.Token.REC);
				skip(Symbol.Token.OPENING_BRACE);
				parseComponents();
				skip(Symbol.Token.CLOSING_BRACE);
				break;
			case PTR:
				skip(Symbol.Token.PTR);
				parseType();
				break;
			case IDENTIFIER:
				skip(Symbol.Token.IDENTIFIER);
				break;
			default:
				signalError("Type");
		}
		endLog();
	}

	//Components -> Component ComponentsPrime .
	private void parseComponents(){
		begLog("Components");
		parseComponent();
		parseComponentsPrime();
		endLog();
	}

	//ComponentsPrime -> comma Component ComponentsPrime .
	//ComponentsPrime -> .
	private void parseComponentsPrime(){
		begLog("ComponentsPrime");
		switch (laSymbol.token){
			case COMMA:
				skip(Symbol.Token.COMMA);
				parseComponent();
				parseComponentsPrime();
				break;
			case CLOSING_BRACE:
				break;
			default:
				signalError("ComponentsPrime");
		}
		endLog();
	}

	//Component -> IDENTIFIER colon Type .
	private void parseComponent(){
		begLog("Component");
		skip(Symbol.Token.IDENTIFIER);
		skip(Symbol.Token.COLON);
		parseType();
		endLog();
	}

//TODO
}
