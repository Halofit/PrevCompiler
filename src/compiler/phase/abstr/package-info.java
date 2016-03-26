/**
 * Construction of abstract syntax trees.
 * 
 * <p>
 * The abstract syntax tree representing the distilled essence of the program
 * syntactical structure is represented by a tree consisting of objects found in
 * package {@link compiler.phase.abstr}. The construction is performed during
 * the syntax analysis, i.e., in class {@link compiler.phase.synan.SynAn}.
 * </p>
 * 
 * Example:
 * 
 * <pre>
 * {@code
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
 * }
 * </pre>
 * 
 * @author sliva
 */
package compiler.phase.abstr;