package compiler.data.codegen;

/**
 * Created by gregor on 22. 05. 2016.
 */
public class FutureConstant extends Operand {
	private String name;

	public FutureConstant(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
