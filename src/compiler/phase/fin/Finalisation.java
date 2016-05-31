package compiler.phase.fin;

import compiler.Task;
import compiler.common.report.InternalCompilerError;
import compiler.data.codegen.Instruction;
import compiler.data.codegen.InstructionSet;
import compiler.data.frg.CodeFragment;
import compiler.data.frg.ConstFragment;
import compiler.data.frg.DataFragment;
import compiler.data.frg.Fragment;
import compiler.phase.Phase;
import compiler.phase.regalloc.RegisterAlloc;

import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.HashMap;

/**
 * Created by gregor on 31. 05. 2016.
 */
public class Finalisation extends Phase {

	private HashMap<CodeFragment, InstructionSet> fragInstrs;
	private HashMap<String, Fragment> fragments;

	public Finalisation(Task task) {
		super(task, "fin", false);

		this.fragInstrs = task.fragInstrs;
		this.fragments = task.fragments;
	}

	public void finishCode() {
		for (CodeFragment cf : fragInstrs.keySet()) {
			InstructionSet is = fragInstrs.get(cf);
			is.injectEntryAndExit(cf);
		}

		PrintWriter writer;
		try {
			writer = new PrintWriter(task.srcFName + ".mmix", "US-ASCII");

			writer.println("\t\tLOC	Data_Segment");

			//define the common registers
			writer.println("\t\tFP IS $253");
			writer.println("\t\tSP IS $254");
			writer.println("\t\tRV IS $0");
			writer.println("\t\tCOLORS IS $" + RegisterAlloc.physicalRegisters);
			writer.println();
			writer.println();

			for (String s : fragments.keySet()) {
				Fragment f = fragments.get(s);

				if (f instanceof CodeFragment) {
					//For now do nothing, we should first handle other fragments
				} else if (f instanceof ConstFragment) {
					ConstFragment cf = (ConstFragment) f;
					writer.println(s + "\tBYTE\t" + cf.getStringAsValues());
				} else if (f instanceof DataFragment) {
					writer.println(s + "\tOCTA\t" + 0);
				} else {
					throw new InternalCompilerError();
				}
			}

			String indent = "\t";
			for (CodeFragment codeFragment : fragInstrs.keySet()) {
				writer.println("");
				writer.println(codeFragment.frame.label + ":");
				InstructionSet instrs = fragInstrs.get(codeFragment);
				for (Instruction instr : instrs.instrs) {
					writer.print(indent);
					writer.println(instr);
				}
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
}
