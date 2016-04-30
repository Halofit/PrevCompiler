package compiler.phase.seman;

import java.util.LinkedList;

import compiler.common.report.*;
import compiler.data.ast.*;
import compiler.data.ast.attr.*;
import compiler.data.ast.code.*;
import compiler.data.typ.*;

/**
 * Type checker.
 * 
 * <p>
 * Type checker checks type of all sentential forms of the program and resolves
 * the component names as this cannot be done earlier, i.e., in
 * {@link compiler.phase.seman.EvalDecl}.
 * </p>
 * 
 * @author sliva
 */
public class EvalTyp extends FullVisitor {

	private final Attributes attrs;

	public EvalTyp(Attributes attrs) {
		this.attrs = attrs;
	}

	/** The symbol table. */
	private SymbolTable symbolTable = new SymbolTable();

	/** Phases of declaration checking. */
	private enum DeclMode {
		HEAD, BODY,
	}

	/**
	 * Denotes which part of the declaration is being checked.
	 * 
	 * <p>
	 * (Serves as a hidden parameter of the visitor.)
	 * </p>
	 */
	private DeclMode declMode;

	public void visit(ArrType arrType) {
		arrType.size.accept(this);
		arrType.elemType.accept(this);
		Long value = attrs.valueAttr.get(arrType.size);
		if (value == null) {
			Report.warning(arrType, "Array size is not a constant integer expression.");
		} else if (value < 1) {
			Report.warning(arrType, "Illegal array size.");
		}
		Typ typeTyp = attrs.typAttr.get(arrType.elemType);
		if (typeTyp == null) {
			// Warning reported earlier.
		}
		if ((value != null) && (typeTyp != null))
			attrs.typAttr.set(arrType, new ArrTyp(value.longValue(), typeTyp));
	}

	public void visit(AtomExpr atomExpr) {
		switch (atomExpr.type) {
		case INTEGER:
			attrs.typAttr.set(atomExpr, new IntegerTyp());
			break;
		case BOOLEAN:
			attrs.typAttr.set(atomExpr, new BooleanTyp());
			break;
		case CHAR:
			attrs.typAttr.set(atomExpr, new CharTyp());
			break;
		case STRING:
			attrs.typAttr.set(atomExpr, new StringTyp());
			break;
		case PTR:
			attrs.typAttr.set(atomExpr, new PtrTyp(new VoidTyp()));
			break;
		case VOID:
			attrs.typAttr.set(atomExpr, new VoidTyp());
			break;
		}
	}

	public void visit(AtomType atomType) {
		switch (atomType.type) {
		case INTEGER:
			attrs.typAttr.set(atomType, new IntegerTyp());
			break;
		case BOOLEAN:
			attrs.typAttr.set(atomType, new BooleanTyp());
			break;
		case CHAR:
			attrs.typAttr.set(atomType, new CharTyp());
			break;
		case STRING:
			attrs.typAttr.set(atomType, new StringTyp());
			break;
		case VOID:
			attrs.typAttr.set(atomType, new VoidTyp());
			break;
		}
	}

	public void visit(BinExpr binExpr) {
		Typ fstExprTyp = null;
		Typ sndExprTyp = null;

		binExpr.fstExpr.accept(this);
		fstExprTyp = attrs.typAttr.get(binExpr.fstExpr);

		if (!(binExpr.sndExpr instanceof CompName)) {
			binExpr.sndExpr.accept(this);
			sndExprTyp = attrs.typAttr.get(binExpr.sndExpr);
		}

		Typ binExprTyp = null;
		switch (binExpr.oper) {
		case ASSIGN: {
			if (fstExprTyp == null) {
				// Warning reported earlier.
			} else if (fstExprTyp.actualTyp() instanceof AssignableTyp)
				binExprTyp = binExprTyp == null ? fstExprTyp : binExprTyp;
			else
				Report.warning(binExpr.fstExpr, "Unassignable type on the left size of assignment.");
			if (sndExprTyp == null) {
				// Warning reported earlier.
			} else if (sndExprTyp.actualTyp() instanceof AssignableTyp)
				binExprTyp = binExprTyp == null ? sndExprTyp : binExprTyp;
			else
				Report.warning(binExpr.sndExpr, "Unassignable type on the right size of assignment.");

			if (binExprTyp != null)
				attrs.typAttr.set(binExpr, binExprTyp);
			if (!Typ.equiv(fstExprTyp, sndExprTyp)) {
				Report.warning(binExpr, "Incompatible types in assignment.");
			}
			break;
		}
		case OR:
		case AND: {
			if (fstExprTyp == null) {
				// Warning reported earlier.
			} else if (!(fstExprTyp.actualTyp() instanceof BooleanTyp))
				Report.warning(binExpr.fstExpr, "The left argument of a logical operator must be a boolean.");
			if (sndExprTyp == null) {
				// Warning reported earlier.
			} else if (!(sndExprTyp.actualTyp() instanceof BooleanTyp))
				Report.warning(binExpr.sndExpr, "The right argument of a logical operator must be a boolean.");

			attrs.typAttr.set(binExpr, new BooleanTyp());
			break;
		}
		case EQU:
		case NEQ:
		case LTH:
		case GTH:
		case LEQ:
		case GEQ: {
			if (fstExprTyp == null) {
				// Warning reported earlier.
			} else if (!(fstExprTyp.actualTyp() instanceof ComparableTyp))
				Report.warning(binExpr.fstExpr, "Uncomparable type on the left size of a relational operator.");
			if (sndExprTyp == null) {
				// Warning reported earlier.
			} else if (!(sndExprTyp.actualTyp() instanceof ComparableTyp))
				Report.warning(binExpr.sndExpr, "Uncomparable type on the right size of a relational operator.");

			if (!Typ.equiv(fstExprTyp, sndExprTyp)) {
				Report.warning(binExpr, "Incompatible types in a relational expression.");
			}
			attrs.typAttr.set(binExpr, new BooleanTyp());
			break;
		}
		case ADD:
		case SUB:
		case MUL:
		case DIV:
		case MOD: {
			if (fstExprTyp == null) {
				// Warning reported earlier.
			} else if (!(fstExprTyp.actualTyp() instanceof IntegerTyp))
				Report.warning(binExpr.fstExpr, "The left argument of an arithmetic operator must be an integer.");
			if (sndExprTyp == null) {
				// Warning reported earlier.
			} else if (!(sndExprTyp.actualTyp() instanceof IntegerTyp))
				Report.warning(binExpr.sndExpr, "The right argument of an arithmetic operator must be an integer.");

			attrs.typAttr.set(binExpr, new IntegerTyp());
			break;
		}
		case ARR: {
			if (fstExprTyp == null) {
				// Warning reported earlier.
			} else if (fstExprTyp.actualTyp() instanceof ArrTyp)
				binExprTyp = ((ArrTyp) (fstExprTyp.actualTyp())).elemTyp;
			else
				Report.warning(binExpr.fstExpr, "The left argument of an array access must be an array.");
			if (sndExprTyp == null) {
				// Warning reported earlier.
			} else if (!(sndExprTyp.actualTyp() instanceof IntegerTyp))
				Report.warning(binExpr.sndExpr, "The right argument of an array access must be an integer.");

			if (binExprTyp != null)
				attrs.typAttr.set(binExpr, binExprTyp);
			break;
		}
		case REC: {
			RecTyp recTyp = (RecTyp) (attrs.typAttr.get(binExpr.fstExpr).actualTyp());
			CompName compName = (CompName) (binExpr.sndExpr);
			if ((recTyp != null) && (compName != null)) {
				symbolTable.enterNamespace(recTyp.nameSpace);
				try {
					Decl compDecl = symbolTable.fndDecl(recTyp.nameSpace, compName.name());
					attrs.declAttr.set(compName, compDecl);
					binExprTyp = attrs.typAttr.get(compDecl.type);
					if (binExprTyp != null)
						attrs.typAttr.set(compName, binExprTyp);
				} catch (CannotFndNameDecl ex) {
					Report.warning(binExpr.sndExpr, "Name '" + compName.name() + "' is not a valid component name.");
				}
				symbolTable.leaveNamespace();
			}
			if (binExprTyp != null)
				attrs.typAttr.set(binExpr, binExprTyp);
			break;
		}
		}
	}

	public void visit(CastExpr castExpr) {
		castExpr.type.accept(this);
		castExpr.expr.accept(this);

		Typ typeTyp = attrs.typAttr.get(castExpr.type);
		Typ exprTyp = attrs.typAttr.get(castExpr.expr);

		Typ castExprTyp = null;
		if (typeTyp == null) {
			// Warning reported earlier.
		} else
			castExprTyp = typeTyp;
		if (exprTyp == null) {
			// Warning reported earlier.
		}

		if (((typeTyp != null) && (exprTyp != null)) && (!((Typ.equiv(typeTyp, exprTyp))
				|| ((typeTyp.actualTyp() instanceof CastableTyp) && (exprTyp.actualTyp() instanceof CastableTyp)))))
			Report.warning(castExpr, "Incompatible types in a cast expression.");

		if (castExprTyp != null)
			attrs.typAttr.set(castExpr, castExprTyp);
	}

	public void visit(CompDecl compDecl) {
		compDecl.type.accept(this);

		Typ compTyp = attrs.typAttr.get(compDecl.type);
		if (compTyp != null)
			attrs.typAttr.set(compDecl, compTyp);
	}

	public void visit(CompName compName) {
	}

	public void visit(Exprs exprs) {
		for (int e = 0; e < exprs.numExprs(); e++)
			exprs.expr(e).accept(this);

		Typ exprTyp = attrs.typAttr.get(exprs.expr(exprs.numExprs() - 1));
		if (exprTyp != null)
			attrs.typAttr.set(exprs, exprTyp);
	}

	public void visit(ForExpr forExpr) {
		forExpr.var.accept(this);
		forExpr.loBound.accept(this);
		forExpr.hiBound.accept(this);
		forExpr.body.accept(this);

		Typ varTyp = attrs.typAttr.get(forExpr.var);
		Typ loBoundTyp = attrs.typAttr.get(forExpr.loBound);
		Typ hiBoundTyp = attrs.typAttr.get(forExpr.hiBound);
		Typ bodyTyp = attrs.typAttr.get(forExpr.body);

		if (varTyp == null) {
			// Warning reported earlier.
		} else if (!(varTyp.actualTyp() instanceof IntegerTyp))
			Report.warning(forExpr.var, "The variable in the for loop must be of type integer.");
		if (loBoundTyp == null) {
			// Warning reported earlier.
		} else if (!(loBoundTyp.actualTyp() instanceof IntegerTyp))
			Report.warning(forExpr.loBound, "The lower bound of the for loop must be an integer.");
		if (hiBoundTyp == null) {
			// Warning reported earlier.
		} else if (!(hiBoundTyp.actualTyp() instanceof IntegerTyp))
			Report.warning(forExpr.loBound, "The upper bound of the for loop must be an integer.");
		if (bodyTyp == null) {
			// Warning reported earlier.
		} else if (!(bodyTyp.actualTyp() instanceof VoidTyp))
			Report.warning(forExpr.body, "The body of the for loop must be of type void.");

		attrs.typAttr.set(forExpr, new VoidTyp());
	}

	public void visit(FunCall funCall) {
		FunDecl funDecl = (FunDecl) attrs.declAttr.get(funCall);
		if (funDecl == null) {
			for (int a = 0; a < funCall.numArgs(); a++)
				funCall.arg(a).accept(this);
			return;
		}
		FunTyp funTyp = (FunTyp) attrs.typAttr.get(funDecl);
		if (funTyp == null) {
			for (int a = 0; a < funCall.numArgs(); a++)
				funCall.arg(a).accept(this);
			return;
		}

		if ((funTyp.numPars() != funCall.numArgs()))
			Report.warning(funCall, "Illegal number of arguments.");

		for (int a = 0; a < funCall.numArgs(); a++) {
			funCall.arg(a).accept(this);
			if (a < funTyp.numPars()) {
				Typ parTyp = funTyp.parTyp(a);
				Typ argTyp = attrs.typAttr.get(funCall.arg(a));
				if (!((argTyp != null) && (Typ.equiv(parTyp, argTyp))))
					Report.warning(funCall.arg(a), "Illegal argument type.");
			}
		}

		if (funTyp.resultTyp != null)
			attrs.typAttr.set(funCall, funTyp.resultTyp);
	}

	public void visit(FunDecl funDecl) {
		switch (declMode) {
		case HEAD:
			LinkedList<Typ> parTyps = new LinkedList<Typ>();
			for (int p = 0; p < funDecl.numPars(); p++) {
				funDecl.par(p).accept(this);
				Typ parTyp = attrs.typAttr.get(funDecl.par(p));
				if ((parTyp == null) || (!(parTyp.actualTyp() instanceof PassableTyp))) {
					Report.warning(funDecl.par(p), "Illegal parameter type.");
					parTyps = null;
				} else
					parTyps.add(parTyp);
			}
			funDecl.type.accept(this);
			Typ resultTyp = attrs.typAttr.get(funDecl.type);
			if ((resultTyp == null) || (!(resultTyp.actualTyp() instanceof ReturnableTyp))) {
				Report.warning(funDecl.type, "Illegal return type.");
			}
			if ((parTyps != null) && (resultTyp != null))
				attrs.typAttr.set(funDecl, new FunTyp(parTyps, resultTyp));
			break;
		case BODY:
			break;
		}
	}

	public void visit(FunDef funDef) {
		switch (declMode) {
		case HEAD: {
			LinkedList<Typ> parTyps = new LinkedList<Typ>();
			for (int p = 0; p < funDef.numPars(); p++) {
				funDef.par(p).accept(this);
				Typ parTyp = attrs.typAttr.get(funDef.par(p));
				if ((parTyp == null) || (!(parTyp.actualTyp() instanceof PassableTyp))) {
					Report.warning(funDef.par(p), "Illegal parameter type.");
					parTyps = null;
				} else
					parTyps.add(parTyp);
			}
			funDef.type.accept(this);
			Typ resultTyp = attrs.typAttr.get(funDef.type);
			if ((resultTyp == null) || (!(resultTyp.actualTyp() instanceof ReturnableTyp))) {
				Report.warning(funDef.type, "Illegal return type.");
			}
			if ((parTyps != null) && (resultTyp != null))
				attrs.typAttr.set(funDef, new FunTyp(parTyps, resultTyp));
			break;
		}
		case BODY: {
			FunTyp funTyp = (FunTyp) attrs.typAttr.get(funDef);
			if (funTyp == null) {
				// Warning reported earlier.
			}

			funDef.body.accept(this);
			Typ bodyTyp = attrs.typAttr.get(funDef.body);
			if (bodyTyp == null) {
				// Warning reported earlier.
			}

			if ((bodyTyp != null) && (funTyp != null) && (funTyp.resultTyp != null)
					&& (!Typ.equiv(funTyp.resultTyp, bodyTyp)))
				Report.warning(funDef.body, "Incompatible function body type.");
			break;
		}
		}
	}

	public void visit(IfExpr ifExpr) {
		ifExpr.cond.accept(this);
		ifExpr.thenExpr.accept(this);
		ifExpr.elseExpr.accept(this);

		Typ condTyp = attrs.typAttr.get(ifExpr.cond);
		Typ thenExprTyp = attrs.typAttr.get(ifExpr.thenExpr);
		Typ elseExprTyp = attrs.typAttr.get(ifExpr.elseExpr);

		if (condTyp == null) {
			// Warning reported earlier.
		} else if (!(condTyp.actualTyp() instanceof BooleanTyp))
			Report.warning(ifExpr.cond, "The condition in the if expression must be of type boolean.");
		if (thenExprTyp == null) {
			// Warning reported earlier.
		} else if (!(thenExprTyp.actualTyp() instanceof VoidTyp))
			Report.warning(ifExpr.thenExpr, "The then part of the if expression must be of type void.");
		if (elseExprTyp == null) {
			// Warning reported earlier.
		} else if (!(elseExprTyp.actualTyp() instanceof VoidTyp))
			Report.warning(ifExpr.elseExpr, "The else part of the if expression must be of type void.");

		attrs.typAttr.set(ifExpr, new VoidTyp());
	}

	public void visit(ParDecl parDecl) {
		parDecl.type.accept(this);
		Typ parTyp = attrs.typAttr.get(parDecl.type);
		if (parTyp != null)
			attrs.typAttr.set(parDecl, parTyp);
	}

	public void visit(Program program) {
		program.expr.accept(this);
		Typ exprTyp = attrs.typAttr.get(program.expr);
		if (exprTyp == null) {
			// Warning reported earlier.
		} else {
			if (exprTyp.actualTyp() instanceof IntegerTyp)
				attrs.typAttr.set(program, exprTyp);
			else
				Report.warning(program, "The program must return an integer.");
		}
	}

	public void visit(PtrType ptrType) {
		ptrType.baseType.accept(this);
		Typ baseTyp = attrs.typAttr.get(ptrType.baseType);
		if (baseTyp != null)
			attrs.typAttr.set(ptrType, new PtrTyp(baseTyp));
	}

	public void visit(RecType recType) {
		String namespace = symbolTable.newNamespace(recType.hashCode() + "");
		symbolTable.enterNamespace(namespace);
		for (int c = 0; c < recType.numComps(); c++) {
			try {
				symbolTable.insDecl(namespace, recType.comp(c).name, recType.comp(c));
			} catch (CannotInsNameDecl ex) {
				Report.warning(recType.comp(c), "Cannot redeclare '" + recType.comp(c).name + "'.");
			}
		}
		symbolTable.leaveNamespace();
		for (int c = 0; c < recType.numComps(); c++)
			recType.comp(c).accept(this);
		LinkedList<Typ> compTyps = new LinkedList<Typ>();
		for (int c = 0; c < recType.numComps(); c++)
			compTyps.add(attrs.typAttr.get(recType.comp(c)));
		attrs.typAttr.set(recType, new RecTyp(namespace, compTyps));
	}

	public void visit(TypeDecl typDecl) {
		switch (declMode) {
		case HEAD:
			attrs.typAttr.set(typDecl, new TypName(typDecl.name + " @" + ((Position) (typDecl))));
			break;
		case BODY:
			typDecl.type.accept(this);
			Typ typeTyp = attrs.typAttr.get(typDecl.type);
			if (typeTyp != null)
				((TypName) (attrs.typAttr.get(typDecl))).setType(typeTyp);
			break;
		}
	}

	public void visit(TypeName typeName) {
		Decl decl = attrs.declAttr.get(typeName);
		if (decl != null) {
			Typ typeTyp = attrs.typAttr.get(decl);
			if (typeTyp != null)
				attrs.typAttr.set(typeName, typeTyp);
		}
	}

	public void visit(UnExpr unExpr) {
		Typ subExprTyp = null;
		unExpr.subExpr.accept(this);
		subExprTyp = attrs.typAttr.get(unExpr.subExpr);

		Typ unExprTyp = null;
		switch (unExpr.oper) {
		case ADD:
		case SUB: {
			if (subExprTyp == null) {
				// Warning reported earlier.
			} else if (!(subExprTyp.actualTyp() instanceof IntegerTyp))
				Report.warning(unExpr.subExpr, "The argument of a sign operator must be an integer.");

			attrs.typAttr.set(unExpr, new IntegerTyp());
			break;
		}
		case NOT: {
			if (subExprTyp == null) {
				// Warning reported earlier.
			} else if (!(subExprTyp.actualTyp() instanceof BooleanTyp))
				Report.warning(unExpr.subExpr, "The argument of a logical operator must be a boolean.");

			attrs.typAttr.set(unExpr, new BooleanTyp());
			break;
		}
		case VAL: {
			if (subExprTyp == null) {
				// Warning reported earlier.
			} else if (subExprTyp.actualTyp() instanceof PtrTyp)
				unExprTyp = ((PtrTyp) (subExprTyp.actualTyp())).baseTyp;
			else {
				Report.warning(unExpr.subExpr, "Pointer dereferencing works on pointers only.");
			}

			if (unExprTyp != null)
				attrs.typAttr.set(unExpr, unExprTyp);
			break;
		}
		case MEM: {
			if (subExprTyp == null) {
				// Warning reported earlier.
			} else
				unExprTyp = new PtrTyp(subExprTyp);

			if (unExprTyp != null)
				attrs.typAttr.set(unExpr, unExprTyp);
			break;
		}
		}
	}

	public void visit(VarDecl varDecl) {
		switch (declMode) {
		case HEAD:
			varDecl.type.accept(this);
			Typ varTyp = attrs.typAttr.get(varDecl.type);
			if (varTyp != null)
				attrs.typAttr.set(varDecl, varTyp);
			break;
		case BODY:
			break;
		}
	}

	public void visit(VarName varName) {
		Decl decl = attrs.declAttr.get(varName);
		if (decl != null) {
			Typ varTyp = attrs.typAttr.get(decl);
			if (varTyp != null)
				attrs.typAttr.set(varName, varTyp);
		}
	}

	public void visit(WhereExpr whereExpr) {
		{
			declMode = DeclMode.HEAD;
			for (int d = 0; d < whereExpr.numDecls(); d++)
				if (whereExpr.decl(d) instanceof TypeDecl)
					whereExpr.decl(d).accept(this);
			for (int d = 0; d < whereExpr.numDecls(); d++)
				if (whereExpr.decl(d) instanceof VarDecl)
					whereExpr.decl(d).accept(this);
			for (int d = 0; d < whereExpr.numDecls(); d++)
				if (whereExpr.decl(d) instanceof FunDecl)
					whereExpr.decl(d).accept(this);
		}
		{
			declMode = DeclMode.BODY;
			for (int d = 0; d < whereExpr.numDecls(); d++)
				if (whereExpr.decl(d) instanceof TypeDecl)
					whereExpr.decl(d).accept(this);
			for (int d = 0; d < whereExpr.numDecls(); d++)
				if (whereExpr.decl(d) instanceof VarDecl)
					whereExpr.decl(d).accept(this);
			for (int d = 0; d < whereExpr.numDecls(); d++)
				if (whereExpr.decl(d) instanceof FunDecl)
					whereExpr.decl(d).accept(this);
		}
		for (int d = 0; d < whereExpr.numDecls(); d++)
			if (whereExpr.decl(d) instanceof TypeDecl) {
				TypName typName = (TypName) (attrs.typAttr.get(whereExpr.decl(d)));
				if ((typName != null) && (typName.isCircular())) {
					Report.warning(whereExpr.decl(d),
							"Type '" + whereExpr.decl(d).name + "' is circular but it should not be.");
				}
			}

		whereExpr.expr.accept(this);
		Typ exprTyp = attrs.typAttr.get(whereExpr.expr);
		if (exprTyp != null)
			attrs.typAttr.set(whereExpr, exprTyp);
	}

	public void visit(WhileExpr whileExpr) {
		whileExpr.cond.accept(this);
		whileExpr.body.accept(this);

		Typ condTyp = attrs.typAttr.get(whileExpr.cond);
		Typ bodyTyp = attrs.typAttr.get(whileExpr.body);

		if (condTyp == null) {
			// Warning reported earlier.
		} else if (!(condTyp.actualTyp() instanceof BooleanTyp))
			Report.warning(whileExpr.cond, "The condition in the while loop must be of type boolean.");
		if (bodyTyp == null) {
			// Warning reported earlier.
		} else if (!(bodyTyp.actualTyp() instanceof VoidTyp))
			Report.warning(whileExpr.body, "The body of the while loop must be of type void.");

		attrs.typAttr.set(whileExpr, new VoidTyp());
	}

}
