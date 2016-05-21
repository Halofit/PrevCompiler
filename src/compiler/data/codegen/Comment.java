package compiler.data.codegen;

/**
 * Created by gregor on 21. 05. 2016.
 */
public class Comment extends Instruction {

	public String content;

	public Comment(String content) {
		this.content = content;
	}

	@Override
	public String toString() {
		return "%" + content;
	}
}
