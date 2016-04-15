package compiler.phase.seman;


import compiler.data.ast.*;
import compiler.data.ast.attr.Attributes;
import compiler.data.ast.code.FullVisitor;

/**
 * Declaration resolver.
 * <p>
 * <p>
 * Declaration resolver maps each AST node denoting a
 * {@link compiler.data.ast.Declarable} name to the declaration where
 * this name is declared. In other words, it links each use of each name to a
 * declaration of that name.
 * </p>
 *
 * @author sliva
 */
public class EvalDecl extends FullVisitor {

	private final Attributes attrs;

	public EvalDecl(Attributes attrs) {
		this.attrs = attrs;
	}

	/**
	 * The symbol table.
	 */
	private SymbolTable symbolTable = new SymbolTable();


	@Override
	public void visit(WhereExpr whereExpr) {
		symbolTable.enterScope();

		for (int d = 0; d < whereExpr.numDecls(); d++) {
			whereExpr.decl(d).accept(this);
		}
		whereExpr.expr.accept(this);

		symbolTable.leaveScope();
	}


	@Override
	public void visit(FunDef funDef) {
		symbolTable.insDecl(funDef.name, funDef);
		funDef.type.accept(this);

		symbolTable.enterScope();

		for (ParDecl pd : funDef.pars) {
			pd.accept(this);
		}
		funDef.body.accept(this);

		symbolTable.leaveScope();
	}


	@Override
	public void visit(FunDecl funDecl) {
		symbolTable.insDecl(funDecl.name, funDecl);

		funDecl.type.accept(this);

		symbolTable.enterScope();
		for (ParDecl pd : funDecl.pars) {
			pd.accept(this);
		}

		symbolTable.leaveScope();
	}

	@Override
	public void visit(CompDecl compDecl) {
		super.visit(compDecl);
		// TODO check: Do nothing here, read below: ... visit(RecType recType) { ...
	}

	@Override
	public void visit(ParDecl parDecl) {
		super.visit(parDecl);
		symbolTable.insDecl(parDecl.name, parDecl);
	}

	@Override
	public void visit(TypeDecl typDecl) {
		super.visit(typDecl);
		symbolTable.insDecl(typDecl.name, typDecl);
	}

	@Override
	public void visit(VarDecl varDecl) {
		super.visit(varDecl);
		symbolTable.insDecl(varDecl.name, varDecl);
	}

	@Override
	public void visit(RecType recType) {
		super.visit(recType);
		//TODO check: since symbolsTable is thrown away later, you should do this in EvalTyp.java
		// components should not be checked here
	}


	@Override
	public void visit(TypeName typeName) {
		super.visit(typeName);

		Decl decl = symbolTable.fndDecl(typeName.name());
		attrs.declAttr.set(typeName, decl);
	}

	@Override
	public void visit(VarName varName) {
		super.visit(varName);

		Decl decl = symbolTable.fndDecl(varName.name());
		attrs.declAttr.set(varName, decl);
	}

	@Override
	public void visit(CompName compName) {
		super.visit(compName);
		//TODO you do nothing here, since you don't actually know what namespace you belong to
	}

	@Override
	public void visit(FunCall funCall) {
		Decl decl = symbolTable.fndDecl(funCall.name());
		attrs.declAttr.set(funCall, decl);

		super.visit(funCall);
	}

}

