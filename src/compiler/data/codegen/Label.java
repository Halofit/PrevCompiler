package compiler.data.codegen;

/**
 * Created by gregor on 20.5.2016.
 */
public class Label extends Instruction {
	public String label;

	public Label(String label){
		this.label = label;
	}

	@Override
	public String toString() {
		return label + ": ";
	}
}
