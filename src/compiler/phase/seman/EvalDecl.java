package compiler.phase.seman;

import compiler.common.report.CompilerError;
import compiler.common.report.PhaseErrors.SemAnError;
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
		try {
			symbolTable.insDecl(funDef.name, funDef);
		} catch (CannotInsNameDecl ex) {
			throw new SemAnError(ex.getMessage());
		}
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
		try {
			symbolTable.insDecl(funDecl.name, funDecl);
		} catch (CannotInsNameDecl ex) {
			throw new SemAnError(ex.getMessage());
		}

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
		//TODO check, I belive you do nothing here since you don't know the namespace
	}

	@Override
	public void visit(ParDecl parDecl) {
		super.visit(parDecl);
		try {
			symbolTable.insDecl(parDecl.name, parDecl);
		} catch (CannotInsNameDecl ex) {
			throw new SemAnError(ex.getMessage());
		}
	}

	@Override
	public void visit(TypeDecl typDecl) {
		super.visit(typDecl);
		try {
			symbolTable.insDecl(typDecl.name, typDecl);
		} catch (CannotInsNameDecl ex) {
			throw new SemAnError(ex.getMessage());
		}
	}

	@Override
	public void visit(VarDecl varDecl) {
		super.visit(varDecl);
		try {
			symbolTable.insDecl(varDecl.name, varDecl);
		} catch (CannotInsNameDecl ex) {
			throw new SemAnError(ex.getMessage());
		}
	}

	@Override
	public void visit(RecType recType) {
		symbolTable.enterNamespace(recType.);
		for(CompDecl cd : recType.comps){

			cd.accept(this);
		}
	}
}

