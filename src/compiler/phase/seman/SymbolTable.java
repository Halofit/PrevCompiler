package compiler.phase.seman;

import compiler.common.report.InternalCompilerError;
import compiler.common.report.Position;
import compiler.data.ast.Decl;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Stack;

/**
 * Symbol table.
 * <p>
 * <p>
 * The symbol table is used during name resolving, i.e., connecting AST nodes
 * where names are used to AST nodes where names are defined. It supports
 * handling of
 * </p>
 * <p>
 * <ul>
 * <li><i>scopes</i>: A new, i.e., inner, scope is created by calling
 * {@link compiler.phase.seman.SymbolTable#enterScope() enterScope}, flushing
 * out all names declared at the current scope and returning to the previous
 * scope is performed by calling
 * {@link compiler.phase.seman.SymbolTable#leaveScope() leaveScope}. Scopes are
 * nested and each declaration made within outer scopes is visible within the
 * inner scope unless replaced by a new declaration of the same name within the
 * inner scope.</li>
 * <li><i>namespaces</i>: The default namespace is <code>#</code>. A new
 * namespace is entered by calling
 * {@link compiler.phase.seman.SymbolTable#enterNamespace(String)
 * enterNamespace} and providing the unique namespace name enterNamespace} where
 * the uniqueness of the namespace's name is the responsibility of the
 * programmer. The namespace is leaved by calling
 * {@link compiler.phase.seman.SymbolTable#leaveNamespace() leaveNamespace}.
 * Namespaces are nested and each declaration made within outer namespaces is
 * visible within the inner namespace unless replaced by a new declaration of
 * the same name within the inner namespace.</li>
 * </ul>
 *
 * @author sliva
 */
public class SymbolTable {

	/**
	 * Creates a new symbol table.
	 * <p>
	 * The initial scope is entered and the default namespace is prepared.
	 */
	public SymbolTable() {
		symbolTable = new HashMap<String, LinkedList<ScopedDecl>>();

		scope = 0;
		scopes = new LinkedList<LinkedList<String>>();
		scopes.addFirst(new LinkedList<String>());

		namespaces = new Stack<String>();
		namespaces.push("");
	}

	// Scopes.

	/**
	 * A declaration of a name at a particular scope.
	 *
	 * @author sliva
	 */
	private class ScopedDecl {

		/**
		 * The scope.
		 */
		public int scope;

		/**
		 * The declaration.
		 */
		public Decl decl;

		@Override
		public String toString() {
			return "ScopedDecl{" +
				   "scope=" + scope +
				   ", decl=" + decl +
				   '}';
		}
	}

	/**
	 * The current scope.
	 */
	private int scope;

	/**
	 * The mapping of each node to its declarations at different scopes. The of
	 * name's declarations includes all declarations at different scopes, with
	 * the most recent declaration at the head of the list.
	 */
	private HashMap<String, LinkedList<ScopedDecl>> symbolTable;

	/**
	 * A stack of spaces, i.e., a list of all declarations by scopes, used for
	 * flushing out declarations when leaving a scope.
	 * <p>
	 * <p>
	 * The inner list includes all names that have been declared at a particular
	 * scope, and thus the outer list contains all names declared at different
	 * scopes, with the most recent scope at the head of the list.
	 * </p>
	 */
	private LinkedList<LinkedList<String>> scopes;

	/**
	 * Enters a new scope.
	 */
	public void enterScope() {
		scope++;
		scopes.addFirst(new LinkedList<String>());
	}

	/**
	 * Flushes out all declarations made within the current scope and leaves the
	 * current scope.
	 */
	public void leaveScope() {
		for (String name : scopes.peek()) {
			LinkedList<ScopedDecl> scopedDecls = symbolTable.get(name);
			scopedDecls.removeFirst();
			if (scopedDecls.isEmpty()) {
				symbolTable.remove(name);
			}
		}
		scopes.removeFirst();
		scope--;
	}

	/**
	 * Inserts a declaration of a name within the current scope and within the
	 * specified namespace.
	 *
	 * @param nameSpace The namespace that the declaration of the name is made within.
	 * @param name      The name declared.
	 * @param decl      The declaration of the name.
	 * @throws CannotInsNameDecl If the name cannot be declared at this scope as it has
	 *                           already been.
	 */
	public void insDecl(String nameSpace, String name, Decl decl) throws CannotInsNameDecl {
		LinkedList<ScopedDecl> scopedDecls = symbolTable.get(nameSpace + name);
		if (scopedDecls == null) {
			scopedDecls = new LinkedList<ScopedDecl>();
			{
				ScopedDecl scopedDecl = new ScopedDecl();
				scopedDecl.scope = scope;
				scopedDecl.decl = decl;
				scopedDecls.addFirst(scopedDecl);
				scopes.peek().addFirst(nameSpace + name);
			}
			symbolTable.put(nameSpace + name, scopedDecls);
		} else {
			ScopedDecl scopedDecl = scopedDecls.peekFirst();
			if (scopedDecl.scope == scope) {
				throw new CannotInsNameDecl("Duplicate name " + nameSpace + name +
											" in this scope: " + ((Position) scopedDecl.decl).toString());
			} else {
				scopedDecl = new ScopedDecl();
				scopedDecl.scope = scope;
				scopedDecl.decl = decl;
				scopedDecls.addFirst(scopedDecl);
				scopes.peek().addFirst(nameSpace + name);
			}
		}
	}

	/**
	 * Inserts a declaration of a name within the current scope and within the
	 * default namespace.
	 *
	 * @param name The name declared.
	 * @param decl The declaration of the name.
	 * @throws CannotInsNameDecl If the name cannot be declared at this scope as it has
	 *                           already been.
	 */
	public void insDecl(String name, Decl decl) throws CannotInsNameDecl {
		insDecl("#", name, decl);
	}

	/**
	 * Returns the declaration of a name within all active scopes and within a
	 * specified namespace.
	 *
	 * @param nameSpace The namespace that the declaration of the name is made within.
	 * @param name      The name the declaration is being looked for.
	 * @return The declaration of the name.
	 * @throws CannotFndNameDecl If the declaration is not found.
	 */
	public Decl fndDecl(String nameSpace, String name) throws CannotFndNameDecl {
		LinkedList<ScopedDecl> scopedDecls = symbolTable.get(nameSpace + name);
		if ((scopedDecls == null) || (scopedDecls.isEmpty())) {
			throw new CannotFndNameDecl("Declaration of " + nameSpace + (nameSpace.equals("#") ? "" : "#") + name + " was not found");
		} else {
			return scopedDecls.peekFirst().decl;
		}
	}

	/**
	 * Returns the declaration of a name within all active scopes and within the
	 * default namespace.
	 *
	 * @param name The name the declaration is being looked for.
	 * @return The declaration of the name.
	 * @throws CannotFndNameDecl If the declaration is not found.
	 */
	public Decl fndDecl(String name) throws CannotFndNameDecl {
		return fndDecl("#", name);
	}

	// Namespaces.

	/**
	 * The stack of namespaces.
	 */
	private Stack<String> namespaces;

	/**
	 * Generates a new namespace name.
	 *
	 * @param name The name associated with this namespace.
	 * @return A new namespace name unique within a current context.
	 */
	public String newNamespace(String name) {
		return (namespaces.size() > 0 ? namespaces.peek() : "") + name + "#";
	}

	/**
	 * Enters a new namespace;
	 *
	 * @param namespace The namespace's name.
	 */
	public void enterNamespace(String namespace) {
		namespaces.push(namespace);
	}

	/**
	 * Leaves the current namespace.
	 */
	public void leaveNamespace() {
		if (namespaces.size() > 0) {
			namespaces.pop();
		} else {
			throw new InternalCompilerError();
		}
	}

	public void printTableData() {
		System.out.println("Scope: " + scope);

		System.out.println("Symbol Table: ");
		for (String name : symbolTable.keySet()) {
			String value = symbolTable.get(name).toString();
			System.out.println(name + " " + value);
		}
		System.out.println("Visible namespaces:");
		for (String ns : namespaces) {
			System.out.println(ns);
		}
	}
}
