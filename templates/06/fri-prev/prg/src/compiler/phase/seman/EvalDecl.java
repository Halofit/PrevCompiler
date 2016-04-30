package compiler.phase.seman;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;

/**
 * Declaration resolver.
 * 
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

	/** The symbol table. */
	private SymbolTable symbolTable = new SymbolTable();

	public void visit(FunCall funCall) {
		try {
			Decl decl = symbolTable.fndDecl(funCall.name());
			if (decl instanceof FunDecl)
				attrs.declAttr.set(funCall, (FunDecl) decl);
			else
				Report.warning(funCall, "Name '" + funCall.name() + "' is not a function name.");
		} catch (CannotFndNameDecl ex) {
			Report.warning(funCall, "Unknown function '" + funCall.name() + "'.");
		}
		for (int a = 0; a < funCall.numArgs(); a++)
			funCall.arg(a).accept(this);
	}

	public void visit(FunDecl funDecl) {
		symbolTable.enterScope();
		for (int p = 0; p < funDecl.numPars(); p++) {
			try {
				symbolTable.insDecl(funDecl.par(p).name, funDecl.par(p));
			} catch (CannotInsNameDecl ex) {
				Report.warning(funDecl.par(p), "Cannot redeclare name '" + funDecl.par(p).name + "'.");
			}
		}
		for (int p = 0; p < funDecl.numPars(); p++)
			funDecl.par(p).accept(this);
		funDecl.type.accept(this);
		symbolTable.leaveScope();
	}

	public void visit(FunDef funDef) {
		symbolTable.enterScope();
		for (int p = 0; p < funDef.numPars(); p++) {
			try {
				symbolTable.insDecl(funDef.par(p).name, funDef.par(p));
			} catch (CannotInsNameDecl ex) {
				Report.warning(funDef.par(p), "Cannot redeclare name '" + funDef.par(p).name + "'.");
			}
		}
		for (int p = 0; p < funDef.numPars(); p++)
			funDef.par(p).accept(this);
		funDef.type.accept(this);
		funDef.body.accept(this);
		symbolTable.leaveScope();
	}

	public void visit(TypeName typeName) {
		try {
			Decl decl = symbolTable.fndDecl(typeName.name());
			if (decl instanceof TypeDecl) {
				TypeDecl typDecl = (TypeDecl) decl;
				attrs.declAttr.set(typeName, typDecl);
			} else
				Report.warning(typeName, "Name '" + typeName.name() + "' is not a type name.");
		} catch (CannotFndNameDecl ex) {
			Report.warning(typeName, "Unknown type '" + typeName.name() + "'.");
		}
	}

	public void visit(VarName varName) {
		try {
			Decl decl = symbolTable.fndDecl(varName.name());
			if (decl instanceof VarDecl) {
				attrs.declAttr.set(varName, (VarDecl) decl);
			} else
				Report.warning(varName, "Name '" + varName.name() + "' is not a variable (or parameter) name.");
		} catch (CannotFndNameDecl ex) {
			Report.warning(varName, "Unknown variable (or parameter) '" + varName.name() + "'.");
		}
	}

	public void visit(WhereExpr whereExpr) {
		symbolTable.enterScope();
		for (int d = 0; d < whereExpr.numDecls(); d++) {
			try {
				symbolTable.insDecl(whereExpr.decl(d).name, whereExpr.decl(d));
			} catch (CannotInsNameDecl ex) {
				Report.warning(whereExpr.decl(d), "Cannot redeclare name '" + whereExpr.decl(d).name + "'.");
			}
		}
		for (int d = 0; d < whereExpr.numDecls(); d++)
			whereExpr.decl(d).accept(this);
		whereExpr.expr.accept(this);
		symbolTable.leaveScope();
	}

}
