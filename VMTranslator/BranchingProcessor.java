
public class BranchingProcessor {
	StringBuilder asm;
	private static final String LINE = "\n";

	public BranchingProcessor(StringBuilder asm) {
		super();
		this.asm = asm;
	}

	public void processLabel(VMCommand command) {
		String labelName = command.getArg1();
		String label = "(" + labelName + ")";
		asm.append(label + LINE);
	}

	public void processIfGoto(VMCommand command) {
		String labelName = command.getArg1();
		asm.append("@SP\n");
		asm.append("M=M-1\n");
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@" + labelName + LINE);
		asm.append("D;JNE\n");

	}

	public void processGoto(VMCommand vmCommand) {
		String labelName = vmCommand.getArg1();
		asm.append("@" + labelName + LINE);
		asm.append("0;JEQ\n");
	}
}
