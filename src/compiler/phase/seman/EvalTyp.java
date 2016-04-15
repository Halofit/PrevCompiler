package compiler.phase.seman;

import compiler.common.report.InternalCompilerError;
import compiler.data.ast.*;
import compiler.data.ast.attr.Attributes;
import compiler.data.ast.code.FullVisitor;
import compiler.data.typ.*;

import java.util.LinkedList;

/**
 * Type checker.
 * <p>
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

	/**
	 * The symbol table.
	 */
	private SymbolTable symbolTable = new SymbolTable();

	// TODO

	@Override
	public void visit(ArrType arrType) {
		super.visit(arrType);

		Long size = attrs.valueAttr.get(arrType.size);
		if (size == null) SemAn.signalError("Arrays must be of constant size.", arrType);
		if (size < 1) SemAn.signalError("Arrays must be at least size of 1", arrType);

		Typ baseType = attrs.typAttr.get(arrType.elemType);
		ArrTyp at = new ArrTyp(size, baseType);
		attrs.typAttr.set(arrType, at);
	}

	@Override
	public void visit(AtomExpr atomExpr) {
		super.visit(atomExpr);

		Typ atTyp;

		switch (atomExpr.type) {
			case INTEGER:
				atTyp = new IntegerTyp();
				break;
			case BOOLEAN:
				atTyp = new BooleanTyp();
				break;
			case CHAR:
				atTyp = new CharTyp();
				break;
			case STRING:
				atTyp = new StringTyp();
				break;
			case PTR:
				atTyp = new PtrTyp(new VoidTyp());
				break;
			case VOID:
				atTyp = new VoidTyp();
				break;
			default:
				//Should never happen, except if I add a new type and forget about it
				throw new InternalCompilerError();
		}

		attrs.typAttr.set(atomExpr, atTyp);
	}

	@Override
	public void visit(AtomType atomType) {
		super.visit(atomType);

		Typ aTyp;
		switch (atomType.type) {
			case INTEGER:
				aTyp = new IntegerTyp();
				break;
			case BOOLEAN:
				aTyp = new BooleanTyp();
				break;
			case CHAR:
				aTyp = new CharTyp();
				break;
			case STRING:
				aTyp = new StringTyp();
				break;
			case VOID:
				aTyp = new VoidTyp();
				break;
			default:
				//Should never happen, except if I add a new type and forget about it
				throw new InternalCompilerError();
		}

		attrs.typAttr.set(atomType, aTyp);
	}

	@Override
	public void visit(BinExpr binExpr) {
		super.visit(binExpr);
		Expr op1 = binExpr.fstExpr;
		Expr op2 = binExpr.sndExpr;
		Typ op1T = attrs.typAttr.get(op1);
		Typ op2T = attrs.typAttr.get(op2);

		Typ binE = null;

		switch (binExpr.oper) {
			case ASSIGN:
				//if op1T is typ1 and opt2T is typ2 and typ1 is in memory than this is void type
				if (!isAssignable(op1T)) SemAn.signalError("Type is not assignable.", binExpr);
				if (!op1T.isStructEquivTo(op2T)) SemAn.signalError("Assignement operator type mismatch.", binExpr);
				binE = new VoidTyp();
				break;
			case OR:
			case AND:
				if (op1T instanceof BooleanTyp && op2T instanceof BooleanTyp) {
					binE = new BooleanTyp();
				} else {
					SemAn.signalError("Boolean expresions are only allowed on boolean types", binExpr);
				}
				break;
			case EQU:
			case NEQ:
			case LTH:
			case GTH:
			case LEQ:
			case GEQ:
				if (!isComparable(op1T)) {
					SemAn.signalError("Comparisons are only allowed on pointers, integers, booleans and characters",
									  binExpr);
				}
				if (!op1T.isStructEquivTo(op2T)) SemAn.signalError("Can only compare values of the same type", binExpr);

				binE = new BooleanTyp();
				break;

			case ADD:
			case SUB:
			case MUL:
			case DIV:
			case MOD:
				if (op1T instanceof IntegerTyp && op2T instanceof IntegerTyp) {
					binE = new IntegerTyp();
				} else {
					SemAn.signalError("Arithmetic operation attempted with non-integers.", binExpr);
				}
				break;

			case ARR:
				if (!(op2T instanceof IntegerTyp)) SemAn.signalError("Arrays must be indexed with integers", binExpr);
				binE = op1T.actualTyp();
				break;
			case REC:
				//TODO determine types of components
				//TODO resolve name of components
				if (op1T instanceof RecTyp) {
					VarName recVar = (VarName) op1;
					Decl recDecl = attrs.declAttr.get(recVar);
					if (recDecl == null) SemAn.signalError("Undeclared record use.", binExpr);
					Typ recType = attrs.typAttr.get(recDecl);
					if (recType == null) SemAn.signalError("Cannot determine the record's type.", binExpr);
					if (!(op2 instanceof CompName)) {
						SemAn.signalError("Right side of . (dot) operator must be a component name.", binExpr);
					}

					//Resolve the component name
					Decl decl = symbolTable.fndDecl(((Integer) recType.hashCode()).toString(), ((VarName) op1).name());
					if (decl == null) SemAn.signalError("Cannot resolve component name.", binExpr);
					attrs.declAttr.set((CompName) op2, decl);

					Typ compType = attrs.typAttr.get(op2);
					if (compType == null) SemAn.signalError("Cannot determine component's type.", binExpr);
					binE = compType;
				} else {
					SemAn.signalError("Can only use . (dot) operator on record types.", binExpr);
				}
				break;

		}

		attrs.typAttr.set(binExpr, binE);
	}

	@Override
	public void visit(CastExpr castExpr) {
		super.visit(castExpr);

		Typ castTyp = attrs.typAttr.get(castExpr.type);
		Typ exprTyp = attrs.typAttr.get(castExpr.expr);

		if (castTyp == null) SemAn.signalError("Cannot determine cast type", castExpr);
		if (exprTyp == null) SemAn.signalError("Cannot determine expr type", castExpr);

		if (!(castTyp instanceof PtrTyp)
			|| !(exprTyp instanceof PtrTyp)
			|| !(((PtrTyp) exprTyp).baseTyp instanceof VoidTyp)) {
			SemAn.signalError("Can only cast void pointer types to other ptr types", castExpr);
		}

		attrs.typAttr.set(castExpr, castTyp);
	}

	@Override
	public void visit(CompDecl compDecl) {
		super.visit(compDecl);

		Typ compType = attrs.typAttr.get(compDecl.type);
		if (compType == null) {
			SemAn.signalError("Cannot determine type in component declaration.", compDecl);
		}
		attrs.typAttr.set(compDecl, compType);
	}

	@Override
	public void visit(CompName compName) {
		super.visit(compName);

		//FIXME this is wrong
		Typeable t = attrs.declAttr.get(compName);
		if (t == null) SemAn.signalError("Cannot resolve component name", compName);
		Typ compType = attrs.typAttr.get(t);
		if (compType == null) SemAn.signalError("Cannot resolve component type", compName);
		attrs.typAttr.set(compName, compType);
	}

	@Override
	public void visit(DeclError declError) {
		super.visit(declError);
		SemAn.signalError("DeclError in AST tree, exiting.", declError);
	}

	@Override
	public void visit(Exprs exprs) {
		super.visit(exprs);

		for (Expr e : exprs.exprs) {
			if (attrs.typAttr.get(e) == null) {
				SemAn.signalError("One of expressions has an unindentified type.", exprs);
			}
		}

		Expr last = exprs.lastExpr();
		Typ exprsTyp = attrs.typAttr.get(last);
		if (exprsTyp == null) SemAn.signalError("Cannot find last expressions type.", exprs); //Redundant
		attrs.typAttr.set(exprs, exprsTyp);
	}

	@Override
	public void visit(ExprError exprError) {
		super.visit(exprError);
		SemAn.signalError("ExprError in AST tree, exiting.", exprError);
	}

	@Override
	public void visit(ForExpr forExpr) {
		super.visit(forExpr);

		Typ varT = attrs.typAttr.get(forExpr.var);
		Typ loT = attrs.typAttr.get(forExpr.loBound);
		Typ hiT = attrs.typAttr.get(forExpr.hiBound);
		Typ bodyT = attrs.typAttr.get(forExpr.body);

		if (varT == null) SemAn.signalError("Iter variable must have a type.", forExpr);
		if (loT == null) SemAn.signalError("Low bound must have a type.", forExpr);
		if (hiT == null) SemAn.signalError("Hi bound must have a type.", forExpr);
		if (bodyT == null) SemAn.signalError("For body must have a type.", forExpr);

		//Do type checking on the for statement
		if (!(varT instanceof IntegerTyp)) SemAn.signalError("For iteration variable must be an integer.", forExpr);
		if (!(loT instanceof IntegerTyp)) SemAn.signalError("For lower bound must be an integer.", forExpr);
		if (!(hiT instanceof IntegerTyp)) SemAn.signalError("For high bound must be an integer.", forExpr);


		//For statement is of typ void
		attrs.typAttr.set(forExpr, new VoidTyp());
	}

	@Override
	public void visit(FunCall funCall) {
		super.visit(funCall);

		FunDecl decl = (FunDecl) attrs.declAttr.get(funCall);
		FunTyp declType = (FunTyp) attrs.typAttr.get(decl);
		if (decl == null || declType == null) {
			SemAn.signalError("Cannot determine function delaration type.", funCall);
		}

		if (decl.numPars() != funCall.numArgs()) {
			SemAn.signalError(
					"Argument parity error: expected " + decl.numPars() + "arguments, got " + funCall.numArgs() + ".",
					funCall);
		}

		for (int i = 0; i < decl.numPars(); i++) {
			Typ parTyp = attrs.typAttr.get(decl.par(i));
			Typ argTyp = attrs.typAttr.get(funCall.arg(i));

			if (parTyp == null) SemAn.signalError("Cannot determine parameter type.", funCall);
			if (argTyp == null) SemAn.signalError("Cannot determine argument type.", funCall);

			if (!parTyp.getClass().equals(argTyp.getClass())) {
				SemAn.signalError("Type mismatch in argument " + i + ".", funCall);
			}
		}

		attrs.typAttr.set(funCall, declType.resultTyp);
	}

	@Override
	public void visit(FunDecl funDecl) {
		symbolTable.enterScope();
		super.visit(funDecl);
		symbolTable.leaveScope();

		LinkedList<Typ> parTyps = new LinkedList<>();
		for (ParDecl e : funDecl.pars) {
			Typ t = attrs.typAttr.get(e);
			if (t == null) SemAn.signalError("Cannot determine param type.", funDecl);
			parTyps.add(t);
		}

		Typ funT = attrs.typAttr.get(funDecl.type);
		if (funT == null) SemAn.signalError("Cannot determine function type.", funDecl);
		if (!isAssignable(funT) && !(funT instanceof VoidTyp)) {
			SemAn.signalError("Function type must be assignable or void.", funDecl);
		}

		attrs.typAttr.set(funDecl, new FunTyp(parTyps, funT));
	}

	@Override
	public void visit(FunDef funDef) {
		symbolTable.enterScope();
		super.visit(funDef);
		symbolTable.leaveScope();

		LinkedList<Typ> parTyps = new LinkedList<>();
		for (ParDecl e : funDef.pars) {
			Typ t = attrs.typAttr.get(e);
			if (t == null) SemAn.signalError("Cannot determine param type.", funDef);
			parTyps.add(t);
		}

		Typ funT = attrs.typAttr.get(funDef.type);
		if (funT == null) SemAn.signalError("Cannot determine function type.", funDef);
		if (!isAssignable(funT) && !(funT instanceof VoidTyp)) {
			SemAn.signalError("Function type must be assignable or void.", funDef);
		}

		Typ bodyT = attrs.typAttr.get(funDef.body);
		if (bodyT == null) SemAn.signalError("Cannot determine function body type.", funDef);
		if (!funT.isStructEquivTo(bodyT)) SemAn.signalError("Return type mismatch.", funDef);

		attrs.typAttr.set(funDef, new FunTyp(parTyps, funT));
	}

	@Override
	public void visit(IfExpr ifExpr) {
		super.visit(ifExpr);

		Typ condT = attrs.typAttr.get(ifExpr.cond);
		Typ thenT = attrs.typAttr.get(ifExpr.thenExpr);
		Typ elseT = attrs.typAttr.get(ifExpr.elseExpr);

		if (condT == null) SemAn.signalError("Cannot condition's type.", ifExpr);
		if (thenT == null) SemAn.signalError("Cannot determine then branch type.", ifExpr);
		if (elseT == null) SemAn.signalError("Cannot determine else branch type.", ifExpr);

		if (!(condT instanceof BooleanTyp)) SemAn.signalError("Condition must be of a boolean type", ifExpr);

		attrs.typAttr.set(ifExpr, new VoidTyp());
	}

	@Override
	public void visit(ParDecl parDecl) {
		super.visit(parDecl);

		Typ t = attrs.typAttr.get(parDecl.type);
		if (t == null) SemAn.signalError("Cannot determine parameter's type.", parDecl);
		if (!isAssignable(t)) SemAn.signalError("Parameters must be of an assignable type.", parDecl);

		attrs.typAttr.set(parDecl, t);
	}

	@Override
	public void visit(Program program) {
		super.visit(program);

		Typ t = attrs.typAttr.get(program.expr);
		if (t == null) SemAn.signalError("Cannot determine top level expression's type.", program);

		attrs.typAttr.set(program, t);
	}

	@Override
	public void visit(PtrType ptrType) {
		super.visit(ptrType);

		Typ t = attrs.typAttr.get(ptrType.baseType);
		if (t == null) SemAn.signalError("Cannot determine nested pointer's type.", ptrType);

		attrs.typAttr.set(ptrType, new PtrTyp(t));
	}

	@Override
	public void visit(RecType recType) {
		super.visit(recType);

		LinkedList<Typ> compTypes = new LinkedList<>();

		// HACK - not guaranteed by standard to be unique
		// Use the hashCode of the object itself.
		// Hashcodes by default are normally unique, since they represent the internal Java ID
		String nsname = ((Integer) recType.hashCode()).toString();
		symbolTable.enterNamespace(nsname);
		for (CompDecl cd : recType.comps) {
			symbolTable.insDecl(nsname, cd.name, cd);

			Typ cT = attrs.typAttr.get(cd);
			if (cT == null) SemAn.signalError("Cannot determine component's type.", recType);
			compTypes.add(cT);
		}
		symbolTable.leaveNamespace();

		attrs.typAttr.set(recType, new RecTyp(nsname, compTypes));
	}

	@Override
	public void visit(TypeDecl typDecl) {
		super.visit(typDecl);
		Typ t = attrs.typAttr.get(typDecl.type);
		if (t == null) SemAn.signalError("Cannot determine type in Type declaration.", typDecl);

		attrs.typAttr.set(typDecl, t);
	}

	@Override
	public void visit(TypeError typeError) {
		super.visit(typeError);

		SemAn.signalError("Type error in AST. Exiting.", typeError);
	}

	@Override
	public void visit(TypeName typeName) {
		super.visit(typeName);

		Typeable typeDecl = attrs.declAttr.get(typeName);
		if (typeDecl == null) SemAn.signalError("Cannot find the type declaration.", typeName);

		Typ t = attrs.typAttr.get(typeDecl);
		if (t == null) SemAn.signalError("Cannot determine type declaration's type.", typeName);
		attrs.typAttr.set(typeName, t);
	}

	@Override
	public void visit(UnExpr unExpr) {
		super.visit(unExpr);

		Typ subT = attrs.typAttr.get(unExpr.subExpr);
		if (subT == null) SemAn.signalError("Cannot determine sub-expressions's type.", unExpr);

		Typ finalT;

		switch (unExpr.oper) {
			case ADD:
			case SUB:
				if (!(subT instanceof IntegerTyp)) {
					SemAn.signalError("Unary operator + and - can only be applied to integer types", unExpr);
				}
				finalT = subT;
				break;
			case NOT:
				if (!(subT instanceof BooleanTyp)) {
					SemAn.signalError("Operator NOT(!) can only be applied to Boolean types", unExpr);
				}
				finalT = subT;
				break;
			case VAL:
				if (!(subT instanceof PtrTyp)) {
					SemAn.signalError("Operator ^ can only be applied to pointer types", unExpr);
				}
				finalT = ((PtrTyp)subT).baseTyp;
				break;
			case MEM:
				finalT = new PtrTyp(subT);
				break;
			default:
				finalT = null;
				throw new InternalCompilerError();
		}

		attrs.typAttr.set(unExpr, finalT);
	}

	@Override
	public void visit(VarDecl varDecl) {
		super.visit(varDecl);

		Typ t = attrs.typAttr.get(varDecl.type);
		if (t == null) SemAn.signalError("Cannot determine variable declaration's type.", varDecl);

		attrs.typAttr.set(varDecl, t);
	}

	@Override
	public void visit(VarName varName) {
		super.visit(varName);

		Typeable varDecl = attrs.declAttr.get(varName);
		if (varDecl == null) SemAn.signalError("Cannot find the variable's declaration.", varName);

		Typ t = attrs.typAttr.get(varDecl);
		if (t == null) SemAn.signalError("Cannot determine variable declaration's type.", varName);
		attrs.typAttr.set(varName, t);
	}

	@Override
	public void visit(WhereExpr whereExpr) {
		//NOTE: DONT DO THIS super.visit(whereExpr); EXPAND:

		//First set types
		for(Decl d: whereExpr.decls){
			if(d instanceof TypeDecl) d.accept(this);
		}

		for(Decl d: whereExpr.decls){
			if(d instanceof FunDef) ((FunDecl) d).accept(this); //skip function bodies
			else d.accept(this); //redo type decls, no harm done
		}

		for(Decl d: whereExpr.decls){
			if(d instanceof FunDef) d.accept(this); //recheck entire functions
		}

		//Check expression last
		whereExpr.expr.accept(this);

		attrs.typAttr.set(whereExpr, new VoidTyp());
	}

	@Override
	public void visit(WhileExpr whileExpr) {
		super.visit(whileExpr);

		Typ condT = attrs.typAttr.get(whileExpr.cond);
		Typ bodyT = attrs.typAttr.get(whileExpr.body);

		if (condT == null) SemAn.signalError("While condition must have a type.", whileExpr);
		if (bodyT == null) SemAn.signalError("While body must have a type.", whileExpr);

		if (!(condT instanceof BooleanTyp)) SemAn.signalError("While codtidion must be a bool.", whileExpr);

		//While statement is of typ void
		attrs.typAttr.set(whileExpr, new VoidTyp());
	}


	private static boolean isComparable(Typ t) {
		return (t instanceof CharTyp || t instanceof IntegerTyp || t instanceof BooleanTyp || t instanceof PtrTyp);
	}

	private static boolean isAssignable(Typ t) {
		return isComparable(t) || t instanceof StringTyp;
	}
}
