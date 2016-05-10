package compiler.data.ast.attr;

import compiler.data.acc.*;
import compiler.data.ast.*;
import compiler.data.typ.*;
import compiler.data.frm.*;
import compiler.data.imc.*;
import compiler.data.frg.*;

/**
 * @author sliva
 */
public class Attributes {

	/**
	 * Values of simple integer constant expressions. Value <code>null</code>
	 * signals that the value of the attribute cannot be computed.
	 */
	public Attribute<Expr, Long> valueAttr = new Attribute<Expr, Long>();

	/**
	 * Declarations of declarable entities, i.e., every entry denotes a link
	 * from an AST node where a name is used to an AST node where the name is
	 * declared. Value <code>null</code> signals that the value of the attribute
	 * cannot be computed.
	 */
	public Attribute<Declarable, Decl> declAttr = new Attribute<Declarable, Decl>();

	/**
	 * A type associated with an AST node. If the AST node denotes
	 * <ul>
	 * <li>an expression, the value of this attribute is the type of an
	 * expression;</li>
	 * <li>a type, the value of this attribute denotes the type itself;</li>
	 * <li>a declaration, the value of this attribute denotes the type of the
	 * defined name.</li>
	 * </ul>
	 * Value <code>null</code> signals that the value of the attribute cannot be
	 * computed.
	 */
	public Attribute<Typeable, Typ> typAttr = new Attribute<Typeable, Typ>();

	/**
	 * A flag signaling whether an expression evaluates to an address (and
	 * provided it is of assignable type can therefore stand on the left side of
	 * an assignment).
	 */
	public Attribute<Expr, Boolean> memAttr = new Attribute<Expr, Boolean>();

	/**
	 * A function's stack frame.
	 */
	public Attribute<ASTNode, Frame> frmAttr = new Attribute<ASTNode, Frame>();

	/**
	 * A variable's access.
	 */
	public Attribute<VarDecl, Access> accAttr = new Attribute<VarDecl, Access>();
	
	/**
	 * Intermediate code - expressions.
	 */
	public Attribute<ASTNode, IMC> imcAttr = new Attribute<ASTNode, IMC>();
	
	/**
	 * Intermediate code fragments.
	 */
	public Attribute<ASTNode, Fragment> frgAttr = new Attribute<ASTNode, Fragment>();

}
