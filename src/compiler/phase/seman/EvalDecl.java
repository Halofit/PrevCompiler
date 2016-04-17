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

	private static boolean resolveOnlyBody = true;

	@Override
	public void visit(WhereExpr whereExpr) {
		symbolTable.enterScope();

		boolean resolveBak = resolveOnlyBody;
		resolveOnlyBody = false;
		for (Decl d : whereExpr.decls) {
			d.accept(this);
		}

		resolveOnlyBody = true;
		for (Decl d : whereExpr.decls) {
			d.accept(this);
		}
		resolveOnlyBody = resolveBak;

		//Finnaly resolve the nested expression
		whereExpr.expr.accept(this);

		symbolTable.leaveScope();
	}


	@Override
	public void visit(FunDef funDef) {
		if (!resolveOnlyBody) {
			symbolTable.insDecl(funDef.name, funDef);
		}

		if (resolveOnlyBody) {
			funDef.type.accept(this);

			symbolTable.enterScope();

			for (ParDecl pd : funDef.pars) {
				pd.accept(this);
			}
			funDef.body.accept(this);

			symbolTable.leaveScope();
		}
	}


	@Override
	public void visit(FunDecl funDecl) {
		if (!resolveOnlyBody) {
			symbolTable.insDecl(funDecl.name, funDecl);
		}else{
			funDecl.type.accept(this);

			symbolTable.enterScope();
			for (ParDecl pd : funDecl.pars) {
				pd.accept(this);
			}
			symbolTable.leaveScope();
		}
	}

	@Override
	public void visit(CompDecl compDecl) {
		super.visit(compDecl);
		// Do nothing here, read below: ... visit(RecType recType) { ...
	}

	@Override
	public void visit(ParDecl parDecl) {
		super.visit(parDecl);
		symbolTable.insDecl(parDecl.name, parDecl);
	}

	@Override
	public void visit(TypeDecl typeDecl) {
		if (resolveOnlyBody) {
			super.visit(typeDecl);
		} else {
			symbolTable.insDecl(typeDecl.name, typeDecl);
		}
	}

	@Override
	public void visit(VarDecl varDecl) {
		if (resolveOnlyBody) {
			super.visit(varDecl);
		} else {
			symbolTable.insDecl(varDecl.name, varDecl);
		}
	}

	@Override
	public void visit(RecType recType) {
		super.visit(recType);
		// since symbolsTable is thrown away later, you should do this in EvalTyp.java
		// components should not be checked here
	}


	@Override
	public void visit(TypeName typeName) {
		super.visit(typeName);

		Decl decl = null;
		try {
			decl = symbolTable.fndDecl(typeName.name());
		} catch (CannotFndNameDecl cannotFndNameDecl) {
			SemAn.signalError(cannotFndNameDecl.getMessage(), typeName);
		}
		attrs.declAttr.set(typeName, decl);
	}

	@Override
	public void visit(VarName varName) {
		super.visit(varName);

		Decl decl = null;
		try {
			decl = symbolTable.fndDecl(varName.name());
			if (!(decl instanceof VarDecl)) {
				SemAn.signalError(decl.getClass().getSimpleName() + " " + varName.name() +
								  " used as a variable.", varName);
			}
		} catch (CannotFndNameDecl cannotFndNameDecl) {
			SemAn.signalError(cannotFndNameDecl.getMessage(), varName);
		}
		attrs.declAttr.set(varName, decl);
	}

	@Override
	public void visit(CompName compName) {
		super.visit(compName);
		// Do nothing here, since you don't actually know what namespace you belong to
	}

	@Override
	public void visit(FunCall funCall) {
		Decl decl = null;
		try {
			decl = symbolTable.fndDecl(funCall.name());
		} catch (CannotFndNameDecl cannotFndNameDecl) {
			SemAn.signalError(cannotFndNameDecl.getMessage(), funCall);
		}
		attrs.declAttr.set(funCall, decl);

		super.visit(funCall);
	}

}

