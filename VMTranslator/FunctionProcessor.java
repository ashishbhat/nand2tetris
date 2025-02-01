import java.util.HashMap;
import java.util.Map;

public class FunctionProcessor {
	StringBuilder asm;
	private static final String LINE = "\n";
	private static final Map<String, Integer> retLabelCount = new HashMap<String, Integer>();

	public FunctionProcessor(StringBuilder asm) {
		super();
		this.asm = asm;
	}

	void processFunction(VMCommand command) {
		String functionName = command.getArg1();
		int nVars = Integer.parseInt(command.getArg2());
		String functionLabel = "(" + functionName + ")";
		String initLocalLabel = "INIT_LOCAL." + functionName;
		asm.append(functionLabel + LINE);
		asm.append("@" + nVars + LINE);
		asm.append("D=A" + LINE);
		asm.append("(" + initLocalLabel + ")" + LINE);
		asm.append("@END." + functionName + "\n");
		asm.append("D;JEQ\n");
		asm.append("@LCL\n");
		asm.append("A=D+M\n");
		asm.append("A=A-1\n");
		asm.append("M=0\n");
		asm.append("@" + initLocalLabel + LINE);
		asm.append("D=D-1;JMP\n");
		asm.append("(END." + functionName + ")\n");

		asm.append("@" + nVars + LINE);
		asm.append("D=A\n");
		asm.append("@SP\n");
		asm.append("M=D+M\n");

	}

	public void processReturn(VMCommand vmCommand, String currentFunction) {
		String returnAddress = "ret" + currentFunction;
		// endFrame = LCL
		asm.append("@LCL\n");
		asm.append("D=M\n");
		asm.append("@endFrame\n");
		asm.append("M=D\n");

		// retAddr = LCL-5
		asm.append("@5\n");
		asm.append("D=D-A\n");
		asm.append("A=D\n");
		asm.append("D=M\n");
		asm.append("@" + returnAddress + "\n");
		asm.append("M=D\n");

		// *Arg = pop()
		asm.append("@SP\n");
		asm.append("M=M-1\n");
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@ARG\n");
		asm.append("A=M\n");
		asm.append("M=D\n");

		// SP = ARG +1
		asm.append("@ARG\n");
		asm.append("D=M+1\n");
		asm.append("@SP\n");
		asm.append("M=D\n");

		// THAT = endFrame - 1
		asm.append("@endFrame\n");
		asm.append("D=M\n");
		asm.append("@1\n");
		asm.append("D=D-A\n");
		asm.append("A=D\n");
		asm.append("D=M\n");
		asm.append("@THAT\n");
		asm.append("M=D\n");

		// THIS = endFrame - 2
		asm.append("@endFrame\n");
		asm.append("D=M\n");
		asm.append("@2\n");
		asm.append("D=D-A\n");
		asm.append("A=D\n");
		asm.append("D=M\n");
		asm.append("@THIS\n");
		asm.append("M=D\n");

		// ARG = endFrame - 3
		asm.append("@endFrame\n");
		asm.append("D=M\n");
		asm.append("@3\n");
		asm.append("D=D-A\n");
		asm.append("A=D\n");
		asm.append("D=M\n");
		asm.append("@ARG\n");
		asm.append("M=D\n");

		// LCL = endFrame - 4
		asm.append("@endFrame\n");
		asm.append("D=M\n");
		asm.append("@4\n");
		asm.append("D=D-A\n");
		asm.append("A=D\n");
		asm.append("D=M\n");
		asm.append("@LCL\n");
		asm.append("M=D\n");

		// goto retAddr
		asm.append("@" + returnAddress + "\n");
		asm.append("A=M\n");
		asm.append("0;JEQ\n");
	}

	public void processFunctionCall(VMCommand command, String callerFunction) {
		int labelCount = getLabelCount(callerFunction);
		String retLabelName = callerFunction + "$ret." + labelCount;
		int nArgs = Integer.parseInt(command.getArg2());
		asm.append("@" + retLabelName + LINE);
		asm.append("D=A\n");
		asm.append("@SP\n");
		asm.append("A=M\n");
		asm.append("M=D\n");
		asm.append("@SP\n");
		asm.append("M=M+1\n");

		asm.append("@LCL\n");
		asm.append("D=M\n");
		asm.append("@SP\n");
		asm.append("A=M\n");
		asm.append("M=D\n");
		asm.append("@SP\n");
		asm.append("M=M+1\n");

		asm.append("@ARG\n");
		asm.append("D=M\n");
		asm.append("@SP\n");
		asm.append("A=M\n");
		asm.append("M=D\n");
		asm.append("@SP\n");
		asm.append("M=M+1\n");

		asm.append("@THIS\n");
		asm.append("D=M\n");
		asm.append("@SP\n");
		asm.append("A=M\n");
		asm.append("M=D\n");
		asm.append("@SP\n");
		asm.append("M=M+1\n");

		asm.append("@THAT\n");
		asm.append("D=M\n");
		asm.append("@SP\n");
		asm.append("A=M\n");
		asm.append("M=D\n");
		asm.append("@SP\n");
		asm.append("M=M+1\n");

		// re-position the ARG and LCL pointer
		asm.append("@SP\n");
		asm.append("D=M\n");
		asm.append("@LCL\n");
		asm.append("M=D\n");
		asm.append("@5\n");
		asm.append("D=D-A\n");
		asm.append("@" + nArgs + LINE);
		asm.append("D=D-A\n");
		asm.append("@ARG\n");
		asm.append("M=D\n");

		// generate goto command
		String calleFunction = command.getArg1();
		asm.append("@" + calleFunction + LINE);
		asm.append("0;JEQ\n");
		asm.append("(" + retLabelName + ")" + LINE);

	}

	private int getLabelCount(String label) {
		int count = retLabelCount.get(label) == null ? 0 : retLabelCount.get(label);
		retLabelCount.put(label, count + 1);
		return count;
	}
}
