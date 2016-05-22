package compiler.data.codegen;

/**
 * Created by gregor on 21. 05. 2016.
 */
public class FixedRegister extends Register {
	String name;

	public FixedRegister(String name) {
		this.name = name;
	}

	@Override
	public String toString() {
		return name;
	}
}
