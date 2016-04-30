/**
 * Semantic analyzer.
 * 
 * <p>
 * Semantic analyzer performs all computations using specialized
 * {@link compiler.data.ast.code.Visitor}s. At the moment, the following
 * {@link compiler.data.ast.code.Visitor}s are implemented:
 * </p>
 * 
 * <ul>
 * <li>{@link compiler.phase.seman.EvalValue}: evaluation of simple integer
 * constant expressions (needed for deducing array sizes);</li>
 * <li>{@link compiler.phase.seman.EvalDecl}: name resolving (except component
 * name resolving which must be done during type checking).</li>
 * <li>{@link compiler.phase.seman.EvalTyp}: type checking (and component name
 * resolving)</li>
 * <li>{@link compiler.phase.seman.EvalMem}: evaluating which expressions
 * evaluate to addressable values that can be used on the left side of an
 * assignment.</li>
 * </ul>
 * 
 * @author sliva
 */
package compiler.phase.seman;