package compiler.data.ast.attr;

import java.util.*;

import compiler.common.report.*;
import compiler.data.ast.*;

/**
 * AST node's attribute.
 * 
 * <p>
 * This class represents a mapping from from AST nodes to attribute values and
 * thus represents a single AST's attribute. Hence it contains all values, for
 * all applicable AST nodes of a given abstract syntax tree, rather than a value
 * of one particular AST node.
 * </p>
 * 
 * <p>
 * Attributes are defined and computed in later phases and this design enables
 * adding new attributes to AST without changing AST hierarchy.
 * </p>
 * 
 * @author sliva
 */
public class Attribute<Node extends AST, Value> {

	/** A mapping from AST nodes to attribute values. */
	private HashMap<Node, Value> mapping = mapping = new HashMap<Node, Value>();;

	/**
	 * Sets a value of the AST nodes' attribute (if it has not been set
	 * already.)
	 * 
	 * @param node
	 *            AST node.
	 * @param value
	 *            AST node's attribute value.
	 * @throws InternalCompilerError
	 *             If the value has already been set or if the value to be
	 *             inserted is <code>null</code>.
	 */
	public void set(Node node, Value value) {
		if (mapping.containsKey(node))
			throw new InternalCompilerError();
		if (value == null)
			throw new InternalCompilerError();
		mapping.put(node, value);
	}

	/**
	 * Gets a value of the AST node's attribute.
	 * 
	 * @param node
	 *            AST node.
	 * @return AST node's attribute value or <code>null</code> if the value has
	 *         not yet been set.
	 */
	public Value get(Node node) {
		return mapping.get(node);
	}

}
