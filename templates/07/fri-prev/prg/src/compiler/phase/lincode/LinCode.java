package compiler.phase.lincode;

import java.util.*;

import compiler.*;
import compiler.common.report.*;
import compiler.phase.*;

import compiler.data.frg.*;
import compiler.data.imc.*;

/**
 * Linearization of the intermediate code.
 * 
 * @author sliva
 */
public class LinCode extends Phase {

	private Task task;

	/**
	 * Constructs the phase that performs linearization of the intermediate
	 * code.
	 * 
	 * @param task
	 *            The parameters and internal data of the compilation process.
	 */
	public LinCode(Task task) {
		super(task, "lincode");

		this.task = task;
		interpreter();
	}

	/**
	 * Terminates linearization of the intermediate code. If logging has been
	 * requested, this method produces the report by closing the logger.
	 */
	@Override
	public void close() {
		super.close();
	}

	// ----- INTERPRETER -----

	private boolean debug = false;

	// STACK SEGMENT TOP: 0xF000000000000000l
	// HEAP SEGMENT BOT: 0x2000000000000000l
	// DATA SEGMENT BOT: 0x1000000000000000l

	private HashMap<Long, Byte> memory;

	private HashMap<String, Long> dataSegLabels;

	private long[] registers;

	private int fp;
	private int sp;

	private long hp;

	private void interpreter() {
		memory = new HashMap<Long, Byte>();

		sp = TEMP.newTempName();
		registers = new long[sp + 1];
		//registers[sp] = 0xF000000000000000l;
		registers[sp] = 1000l;

		long dataSegPtr = 0x1000000000000000l;
		dataSegLabels = new HashMap<String, Long>();
		for (Fragment fragment : task.fragments.values()) {
			if (fragment instanceof DataFragment) {
				DataFragment dataFrg = (DataFragment) fragment;
				dataSegLabels.put(dataFrg.label, new Long(dataSegPtr));
				if (debug)
					System.err.printf("%4s @ #%16X\n", dataFrg.label, dataSegPtr);
				dataSegPtr += dataFrg.width;
			}
			if (fragment instanceof ConstFragment) {
				ConstFragment constFrg = (ConstFragment) fragment;
				dataSegLabels.put(constFrg.label, new Long(dataSegPtr));
				if (debug)
					System.err.printf("%4s @ #%16X (%s)\n", constFrg.label, dataSegPtr, constFrg.string);
				for (int i = 1; i < constFrg.string.length() - 1; i++) {
					char c = constFrg.string.charAt(i);
					if (c == '\\') {
						switch (constFrg.string.charAt(i + 1)) {
						case '\'':
							c = '\'';
							break;
						case '\"':
							c = '\"';
							break;
						case 'n':
							c = '\n';
							break;
						case 't':
							c = '\t';
							break;
						}
						i++;
					}
					memory.put(new Long(dataSegPtr), new Byte((byte) c));
					dataSegPtr++;
				}
				memory.put(new Long(dataSegPtr), new Byte((byte) 0));
				dataSegPtr++;
			}
		}

		hp = 0x2000000000000000l;

		execute(getCodeFragment("_"));

		registers = null;
		memory = null;
	}

	private CodeFragment getCodeFragment(String label) {
		for (Fragment fragment : task.fragments.values()) {
			if ((fragment instanceof CodeFragment) && (fragment.label.equals(label)))
				return (CodeFragment) fragment;
		}
		return null;
	}

	private void execute(CodeFragment codeFrg) {

		if (codeFrg == null)
			new InternalCompilerError();

		if (debug)
			System.err.printf("BEG CODE FRAGMENT %s\n", codeFrg.label);

		// PROLOGUE
		long[] storedRegisters = new long[sp + 1];
		for (int r = 0; r <= sp; r++)
			storedRegisters[r] = registers[r];
		fp = codeFrg.FP;
		registers[fp] = registers[sp];
		registers[sp] = registers[sp] - codeFrg.frame.size;

		// CORE
		execute(codeFrg.linCode.stmts());

		// EPILOGUE
		stMem(registers[fp], 8, registers[codeFrg.RV]);

		for (int r = 0; r <= sp; r++)
			registers[r] = storedRegisters[r];

		if (debug)
			System.err.printf("END CODE FRAGMENT %s\n", codeFrg.label);
	}

	private void execute(Vector<IMCStmt> stmts) {
		int pc = 0;
		while (true) {
			if (pc >= stmts.size())
				return;
			IMCStmt stmt = stmts.get(pc);

			if (stmt instanceof CJUMP) {
				String dest = null;

				long cond = execute(((CJUMP) stmt).cond);
				if (cond == 0)
					dest = ((CJUMP) stmt).negLabel;
				if (cond == 1)
					dest = ((CJUMP) stmt).posLabel;
				if (dest == null)
					throw new InternalCompilerError();

				pc = 0;
				while (true) {
					if (pc >= stmts.size())
						break;

					IMCStmt newStmt = stmts.get(pc);
					if ((newStmt instanceof LABEL) && (((LABEL) newStmt).label.equals(dest)))
						break;

					pc++;
				}

				continue;
			}

			if (stmt instanceof MOVE) {
				if (((MOVE) stmt).dst instanceof TEMP) {
					int reg = ((TEMP) (((MOVE) stmt).dst)).name;
					long srcValue = execute(((MOVE) stmt).src);
					registers[reg] = srcValue;
					if (debug)
						System.err.printf("T%d <- %1d\n", reg, srcValue);
				}
				if (((MOVE) stmt).dst instanceof MEM) {
					long addr = execute(((MEM) (((MOVE) stmt).dst)).addr);
					long srcValue = execute(((MOVE) stmt).src);
					stMem(addr, ((MEM) (((MOVE) stmt).dst)).width, srcValue);
				}
				pc++;
				continue;
			}

			if (stmt instanceof JUMP) {
				String dest = ((JUMP) stmt).label;

				pc = 0;
				while (true) {
					if (pc >= stmts.size())
						break;

					IMCStmt newStmt = stmts.get(pc);
					if ((newStmt instanceof LABEL) && (((LABEL) newStmt).label.equals(dest)))
						break;

					pc++;
				}
				continue;
			}

			pc++;
		}
	}

	private long execute(IMCExpr expr) {
		if (expr instanceof CONST) {
			return ((CONST) expr).value;
		}

		if (expr instanceof BINOP) {
			long value1 = execute(((BINOP) expr).expr1);
			long value2 = execute(((BINOP) expr).expr2);

			switch (((BINOP) expr).oper) {
			case OR:
				return ((value1 == 1) || (value2 == 1)) ? 1 : 0;
			case AND:
				return ((value1 == 1) && (value2 == 1)) ? 1 : 0;
			case EQU:
				return value1 == value2 ? 1 : 0;
			case NEQ:
				return value1 != value2 ? 1 : 0;
			case LTH:
				return value1 < value2 ? 1 : 0;
			case GTH:
				return value1 > value2 ? 1 : 0;
			case LEQ:
				return value1 <= value2 ? 1 : 0;
			case GEQ:
				return value1 >= value2 ? 1 : 0;
			case ADD:
				return value1 + value2;
			case SUB:
				return value1 - value2;
			case MUL:
				return value1 * value2;
			case DIV:
				return value1 / value2;
			case MOD:
				return value1 % value2;
			}
		}

		if (expr instanceof CALL) {
			CALL call = (CALL) expr;

			if (call.label.equals("_printChr")) {
				long value = execute(call.args(1));
				System.out.printf("%c", (char) value);
				return 0;
			}
			if (call.label.equals("_printInt")) {
				long value = execute(call.args(1));
				System.out.printf("%d", value);
				return 0;
			}
			if (call.label.equals("_printStr")) {
				long addr = execute(call.args(1));
				long c = -1;
				while (c != 0) {
					c = ldMem(addr, 1);
					if (c != 0)
						System.out.printf("%c", (char) c);
					addr++;
				}
				return 0;
			}
			
			long d = 0;
			for (int arg = 0; arg < call.numArgs(); arg++) {
				long value = execute(call.args(arg));
				stMem(registers[sp] + d, call.widths(arg), value);
				d += call.widths(arg);
			}
			
			execute(getCodeFragment(call.label));
			long value = ldMem(registers[sp], 8);
			return value;
		}

		if (expr instanceof MEM) {
			long addr = execute(((MEM) expr).addr);
			return ldMem(addr, ((MEM) expr).width);
		}

		if (expr instanceof NAME) {
			return dataSegLabels.get(((NAME) expr).name).longValue();
		}

		if (expr instanceof NOP) {
			return 0;
		}

		if (expr instanceof TEMP) {
			return registers[((TEMP) expr).name];
		}

		if (expr instanceof UNOP) {
			long value = execute(((UNOP) expr).expr);

			switch (((UNOP) expr).oper) {
			case ADD:
				return +value;
			case SUB:
				return -value;
			case NOT:
				return (value == 1) ? 0 : 1;
			}
		}

		throw new InternalCompilerError();
	}

	private void stMem(long addr, long width, long value) {
		if (debug)
			System.err.printf("[%1d] <- %1d\n", addr, value);
		for (int d = 0; d < width; d++) {
			// System.err.printf("*[%16X] <- %d\n", addr + d, (byte) value);
			memory.put(addr + d, (byte) value);
			value = value >> 8;
		}
	}

	private long ldMem(long addr, long width) {
		long value = 0;
		for (int d = 0; d < width; d++) {
			Byte b = memory.get(addr + d);
			if (b == null)
				b = new Byte((byte) 0);
			long ub = (b < 0) ? (256 + (long) b) : (0 + (long) b);
			// System.err.printf("*[%1d] -> %d\n", addr + d, ub);
			value = value + (ub << (d * 8));
		}
		if (debug)
			System.err.printf("[%1d] -> %1d\n", addr, value);
		return value;
	}

}
