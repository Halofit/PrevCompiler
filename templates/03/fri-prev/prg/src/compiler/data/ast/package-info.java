/**
 * Abstract syntax trees.
 * 
 * <p>
 * Classes in this package are all subclasses of class
 * {@link compiler.data.ast.AST} (which is itself a subclass of class
 * {@link compiler.common.report.Position}). Apart from a few classes and
 * interfaces mentioned below, all classes resemble the concrete syntax close
 * enough that no special documentation is provided.
 * </p>
 * 
 * <ul>
 * <li>{@link compiler.data.ast.AST}: The root class of the abstract
 * syntax tree hierarchy.</li>
 * <li>{@link compiler.data.ast.DeclError},
 * {@link compiler.data.ast.ExprError},
 * {@link compiler.data.ast.TypeError}: Objects of these classes replace
 * abstract syntax subtrees that cannot be constructed due to errors in the
 * source file.</li>
 * <li>{@link compiler.data.ast.Declarable}: An interface that must be
 * implemented by all abstract syntax tree classes that represent entities that
 * could be declared.</li>
 * <li>{@link compiler.data.ast.Typeable}: An interface that must be
 * implemented by all abstract syntax tree classes that represent entities that
 * could be typed.</li>
 * <li>{@link compiler.data.ast.attr.Attribute}: A class used to append
 * attributes to abstract syntax tree nodes.</li>
 * </ul>
 * 
 * @author sliva
 */
package compiler.data.ast;