package compiler.phase.codegen;

import compiler.Task;
import compiler.common.report.InternalCompilerError;
import compiler.data.codegen.*;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.Fragment;
import compiler.data.imc.*;
import compiler.phase.Phase;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by gregor on 20.5.2016.
 */
public class CodeGen extends Phase {
	public static boolean commentAnnotations = true;

	private FixedRegister sp = new FixedRegister("SP");
	private FixedRegister fp = new FixedRegister("FP");
	private FixedRegister numberOfColors = new FixedRegister("colors");
	private FixedRegister reminderReg = new FixedRegister("rR");


	private ConstantOperand const0 = new ConstantOperand(0);
	private ConstantOperand const1 = new ConstantOperand(1);

	private HashMap<CodeFragment, InstructionSet> fragInstrs;
	private HashMap<IMC, InstructionSet> map;

	public CodeGen(Task task) {
		super(task, "codegen");
		map = new HashMap<>();
		fragInstrs = task.fragInstrs;
	}

	@Override
	public void close() {
		super.close();

		// Close the source file.
		PrintWriter writer;
		try {
			writer = new PrintWriter(task.srcFName + ".mmix", "US-ASCII");

			String indent = "\t";

			for (CodeFragment codeFragment : fragInstrs.keySet()) {
				writer.println("");
				writer.println(codeFragment.frame.label + ":");
				writer.println("... (prolog)");
				InstructionSet instrs = fragInstrs.get(codeFragment);
				for (Instruction instr : instrs.instrs) {
					if (instr instanceof Comment) {
						if (((Comment) instr).content.startsWith("BEG")) {
							indent = indent + '\t';
						} else if (((Comment) instr).content.startsWith("END")) {
							writer.print('\t');
							indent = indent.substring(0, indent.length() - 1);
						}
					}else{
						writer.print('\t');
					}

					writer.print(indent);
					writer.println(instr);
				}
				writer.println("... (epilog)");
				writer.println("");
				writer.println("");
			}

			writer.println("");
			writer.println("");
			writer.close();
		} catch (FileNotFoundException | UnsupportedEncodingException e) {
			e.printStackTrace();
		}
	}

	public void generateCode() {
		for (Fragment frag : task.fragments.values()) {
			if (frag instanceof CodeFragment) {
				InstructionSet fragis = generateFragmentCode((CodeFragment) frag);
				fragInstrs.put((CodeFragment) frag, fragis);
			}
		}
	}

	public InstructionSet generateFragmentCode(CodeFragment frag) {
		frag.stmt.visit(this);
		return getInstrs(frag.stmt);
	}

	private InstructionSet getInstrs(IMC imcode) {
		InstructionSet instrs = map.get(imcode);
		if (instrs == null) throw new InternalCompilerError();
		return instrs;
	}

	private void setInstrs(IMC imcode, InstructionSet instrs) {
		map.put(imcode, instrs);
	}

	public void tile(BINOP binop) {
		binop.expr1.visit(this);
		binop.expr2.visit(this);

		InstructionSet is1 = getInstrs(binop.expr1);
		InstructionSet is2 = getInstrs(binop.expr2);

		InstructionSet ownis = new InstructionSet("BINOP");
		ownis.add(is1);
		ownis.add(is2);

		ownis.set(VirtualRegister.create());

		{
			VirtualRegister ret = ownis.ret;
			VirtualRegister op1 = is1.ret;
			VirtualRegister op2 = is2.ret;

			switch (binop.oper) {
				case OR:
					ownis.add(new Mnemonic("OR", ret, op1, op2));
					break;
				case AND:
					ownis.add(new Mnemonic("AND", ret, op1, op2));
					break;
				case EQU:
					ownis.add(new Mnemonic("CMP", ret, op1, op2));
					ownis.add(new Mnemonic("OR", ret, ret, const1));
					ownis.add(new Mnemonic("CMP", ret, ret, const1));
					break;
				case NEQ:
					ownis.add(new Mnemonic("CMP", ret, op1, op2));
					break;
				case LTH:
					ownis.add(new Mnemonic("CMP", ret, op1, op2));
					ownis.add(new Mnemonic("ADD", ret, ret, const1));
					ownis.add(new Mnemonic("CMP", ret, ret, const0));
					ownis.add(new Mnemonic("SUB", ret, ret, const1));
					break;
				case GTH:
					ownis.add(new Mnemonic("CMP", ret, op1, op2));
					ownis.add(new Mnemonic("SUB", ret, ret, const1));
					ownis.add(new Mnemonic("CMP", ret, ret, const0));
					ownis.add(new Mnemonic("ADD", ret, ret, const1));
					break;
				case LEQ:
					ownis.add(new Mnemonic("CMP", ret, op1, op2));
					ownis.add(new Mnemonic("SUB", ret, ret, const1));
					break;
				case GEQ:
					ownis.add(new Mnemonic("CMP", ret, op1, op2));
					ownis.add(new Mnemonic("ADD", ret, ret, const1));
					break;
				case ADD:
					ownis.add(new Mnemonic("ADD", ret, op1, op2));
					break;
				case SUB:
					ownis.add(new Mnemonic("SUB", ret, op1, op2));
					break;
				case MUL:
					ownis.add(new Mnemonic("MUL", ret, op1, op2));
					break;
				case DIV:
					ownis.add(new Mnemonic("DIV", ret, op1, op2));
					break;
				case MOD:
					ownis.add(new Mnemonic("DIV", ret, op1, op2));
					ownis.add(new Mnemonic("GET", ret, reminderReg));
					break;
			}
		}

		setInstrs(binop, ownis);
	}

	public void tile(CALL call) {
		for (IMCExpr arg : call.args) {
			arg.visit(this);
		}

		InstructionSet ownis = new InstructionSet("CALL");
		ownis.set(VirtualRegister.create());

		long argOffsetCnt = 0;

		for (IMCExpr arg : call.args) {
			InstructionSet argis = getInstrs(arg);
			ownis.add(argis);
			ownis.add(new Mnemonic("STO", argis.ret, sp, new ConstantOperand(argOffsetCnt)));
			argOffsetCnt += 8;
		}

		ownis.add(new Mnemonic("PUSHJ", numberOfColors, new OperandLabel(call.label)));
		ownis.add(new Mnemonic("LDO", ownis.ret, sp, const0));

		setInstrs(call, ownis);
	}

	public void tile(CJUMP cjump) {
		cjump.cond.visit(this);

		InstructionSet ownis = new InstructionSet("CJUMP");
		ownis.add(getInstrs(cjump.cond));

		//NOTE: CJUMP assumes the negative label ALWAYS follows the cjump instruction
		// 			this is guaranteed by the basic blocks section
		//This means we jump on a non-negative value (true)
		ownis.add(new Mnemonic("BNZ", new OperandLabel(cjump.posLabel)));

		// If the previous assert doesn't hold, uncomment this line:
		//ownis.add(new Mnemonic("JMP", new OperandLabel(cjump.negLabel)));

		setInstrs(cjump, ownis);
	}


	public void tile(CONST constant) {
		long val = constant.value;

		InstructionSet ownis = new InstructionSet("CONST");
		ownis.set(VirtualRegister.create());
		VirtualRegister ret = ownis.ret;

		ownis.add(new Mnemonic("SETL", ret, new ConstantOperand(val & 0xFFFFL)));
		val = val >> 16;
		if (val != 0) {
			ownis.add(new Mnemonic("INCML", ret, new ConstantOperand(val & 0xFFFFL)));
			val = val >> 16;
		}

		if (val != 0) {
			ownis.add(new Mnemonic("INCMH", ret, new ConstantOperand(val & 0xFFFFL)));
			val = val >> 16;
		}

		if (val != 0) {
			ownis.add(new Mnemonic("INCH", ret, new ConstantOperand(val & 0xFFFFL)));
		}

		setInstrs(constant, ownis);
	}


	public void tile(ESTMT estmt) {
		estmt.expr.visit(this);
		setInstrs(estmt, getInstrs(estmt.expr));
	}


	public void tile(JUMP jump) {
		InstructionSet ownis = new InstructionSet("JUMP");
		ownis.add(new Mnemonic("JMP", new OperandLabel(jump.label)));

		setInstrs(jump, ownis);
	}


	public void tile(LABEL label) {
		InstructionSet ownis = new InstructionSet("LABEL");
		ownis.add(new Label(label.label));
		setInstrs(label, ownis);
	}


	public void tile(MEM mem) {
		mem.addr.visit(this);

		InstructionSet addris = getInstrs(mem.addr);
		InstructionSet ownis = new InstructionSet("MEM");
		VirtualRegister locReg = addris.ret;

		String mnemonic;

		switch ((byte) mem.width) {
			case 1:
				mnemonic = "LDB";
				break;
			case 2:
				mnemonic = "LDW";
				break;
			case 4:
				mnemonic = "LDT";
				break;
			case 8:
				mnemonic = "LDO";
				break;
			default:
				throw new InternalCompilerError();
		}

		ownis.set(VirtualRegister.create());
		ownis.add(addris);
		ownis.add(new Mnemonic(mnemonic, ownis.ret, locReg, const0));

		setInstrs(mem, ownis);
	}

	// BTW -> I hate this ImCode, who thought that MEM being used for 2 different things is a good idea?
	public void tile(MOVE move) {
		move.dst.visit(this);
		move.src.visit(this);

		InstructionSet dstis = getInstrs(move.dst);
		InstructionSet srcis = getInstrs(move.src);
		InstructionSet ownis = new InstructionSet("MOVE");

		//This ordering needs to be this way so the fixing below can work
		ownis.add(srcis);
		ownis.add(dstis);

		VirtualRegister dstret = dstis.ret;
		VirtualRegister srcret = srcis.ret;

		if (move.dst instanceof MEM) {
			//If the left side is MEM then we have to remove the last instruction
			// which should be LDB/LDW/LDT/LDO and replace with SDB/SDW/SDT/SDO

			Instruction lasttmp = ownis.instrs.pop();
			if (!(lasttmp instanceof Mnemonic)) throw new InternalCompilerError();
			Mnemonic last = (Mnemonic) lasttmp;
			String mnemonic;
			switch (last.mnemonic) {
				case "LDB":
					mnemonic = "STB";
					break;
				case "LDW":
					mnemonic = "STB";
					break;
				case "LDT":
					mnemonic = "STB";
					break;
				case "LDO":
					mnemonic = "STB";
					break;
				default:
					throw new InternalCompilerError();
			}

			ownis.add(new Mnemonic(mnemonic, srcret, last.operands[1], last.operands[2]));
		} else {
			ownis.add(new Mnemonic("ADD", dstret, srcret, const0));
		}


		setInstrs(move, ownis);

	}


	public void tile(NAME name) {
		InstructionSet ownis = new InstructionSet("NAME");
		ownis.set(VirtualRegister.create());
		ownis.add(new Mnemonic("GETA", ownis.ret, new OperandLabel(name.name)));

		setInstrs(name, ownis);
	}


	public void tile(NOP nop) {
		InstructionSet ownis = new InstructionSet("NOP");
		setInstrs(nop, ownis);
	}


	public void tile(SEXPR sexpr) {
		sexpr.stmt.visit(this);
		sexpr.expr.visit(this);

		InstructionSet stmtis = getInstrs(sexpr.stmt);
		InstructionSet expris = getInstrs(sexpr.expr);

		InstructionSet ownis = new InstructionSet("SEXPR");
		ownis.add(stmtis);
		ownis.add(expris);
		ownis.set(expris.ret);

		setInstrs(sexpr, ownis);
	}


	public void tile(STMTS stmts) {
		for (IMCStmt stmt : stmts.stmts) {
			stmt.visit(this);
		}

		InstructionSet ownis = new InstructionSet("STMTS");

		for (IMCStmt stmt : stmts.stmts) {
			ownis.add(getInstrs(stmt));
		}

		setInstrs(stmts, ownis);
	}


	public void tile(TEMP temp) {
		InstructionSet ownis = new InstructionSet("TEMP");
		ownis.set(VirtualRegister.create(temp.name));

		setInstrs(temp, ownis);
	}


	public void tile(UNOP unop) {
		unop.expr.visit(this);

		InstructionSet ownis = new InstructionSet("UNOP");
		InstructionSet expris = getInstrs(unop.expr);

		ownis.add(expris);
		VirtualRegister ret = ownis.ret;
		ownis.set(ret);

		switch (unop.oper) {
			case ADD:
				break;
			case SUB:
				ownis.add(new Mnemonic("NEG", ret, ret));
				break;
			case NOT:
				ownis.add(new Mnemonic("CMP", ret, ret, const0));
				ownis.add(new Mnemonic("OR", ret, ret, const1));
				ownis.add(new Mnemonic("SUB", ret, ret, const1));
				break;
		}

		setInstrs(unop, ownis);
	}


}
