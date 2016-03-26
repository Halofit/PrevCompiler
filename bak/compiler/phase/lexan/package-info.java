/**
 * Lexical analyzer.
 * 
 * <p>
 * Apart from the constructor and method
 * {@link compiler.phase.lexan.LexAn#close() close}, the lexical analyzer should
 * have only one public method, namely {@link compiler.phase.lexan.LexAn#lexAn()
 * lexAn}. Each time this method is called, it should return the next lexical
 * symbol from the source file. Hence, the lexical analysis is performed by
 * polling the lexical analyzer by calling
 * {@link compiler.phase.lexan.LexAn#lexAn() lexAn}.
 * </p>
 * 
 * <p>
 * To produce the correct log report (if it has been requested), method
 * {@link compiler.phase.lexan.Symbol#log(Logger) log} of every symbol returned
 * by {@link compiler.phase.lexan.LexAn#lexAn() lexAn} should be called before
 * the symbol is returned.
 * </p>
 * 
 * @author sliva
 */
package compiler.phase.lexan;