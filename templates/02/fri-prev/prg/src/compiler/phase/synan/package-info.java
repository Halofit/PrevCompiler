/**
 * Syntax analyzer.
 * 
 * <p>
 * Apart from the constructor and method
 * {@link compiler.phase.synan.SynAn#close() close}, the syntax analyzer should
 * have only one public method, namely {@link compiler.phase.synan.SynAn#synAn()
 * synAn}. This method is a parser: once called, it parses the entire source
 * file.
 * </p>
 * 
 * <p>
 * To produce the correct log report (if it has been requested) during the
 * recursive descent parsing using an LL(1) parser, each method implementing a
 * subparser for a particular nonterminal should call method
 * {@link compiler.phase.synan.SynAn#begLog(String) begLog} first and method
 * {@link compiler.phase.synan.SynAn#endLog() endLog} at the very end. Each time
 * the lookahead buffer should be advanced, method
 * {@link compiler.phase.synan.SynAn#nextSymbol() nextSymbol} should be used
 * instead of calling the lexical analyzer directly.
 * </p>
 * 
 * Example:
 * 
 * <pre>
 * {@code
private void parseParameter() {
	begLog("Parameter");
	if (laSymbol.token == Symbol.Token.IDENTIFIER) {
		nextSymbol();
	} else {
		Report.warning(laSymbol, "Missing identifier inserted.");
		nextSymbolIsError();
	}
	if (laSymbol.token == Symbol.Token.COLON) {
		nextSymbol();
	} else {
		Report.warning(laSymbol, "Missing symbol ':' inserted.");
		nextSymbolIsError();
	}
	parseTypeExpression();
	endLog();
}
 * }
 * </pre>
 * 
 * <p>
 * During the implementation of the syntax analyzer, no methods for parsing
 * sentential forms (like the one above) returns anything, i.e., they are
 * declared <code>void</code>. Later, during the implementation of the abstract
 * syntax tree construction, they will be modified to return the representations
 * of the sentential forms.
 * </p>
 * 
 * @author sliva
 */
package compiler.phase.synan;