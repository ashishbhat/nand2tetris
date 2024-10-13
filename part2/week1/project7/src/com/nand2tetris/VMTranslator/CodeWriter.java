package com.nand2tetris.VMTranslator;

import java.util.List;

public class CodeWriter {
	private String outFile;
	private String stackPointer = "R0";
	private StringBuilder asm = new StringBuilder();

	private static final String LINE = "\n";

	public CodeWriter(String outFile) {
		super();
		this.outFile = outFile;
	}

	public void translate(List<VMCommand> commands) {
		for (VMCommand vmCommand : commands) {
			if (vmCommand.getName() == VMCommand.Command.PUSH) {
				generatePushCommand(vmCommand);
			} else if (vmCommand.getName() == VMCommand.Command.POP) {
				generatePopCommand(vmCommand);
			} else if (vmCommand.getName() == VMCommand.Command.NEG) {
				generateNegateCommand();
			} else if (vmCommand.getName() == VMCommand.Command.ADD) {
				generateAddCommand();
			} else if (vmCommand.getName() == VMCommand.Command.SUB) {
				generateSubCommand();
			} else if (vmCommand.getName() == VMCommand.Command.AND) {
				generateAndCommand();
			} else if (vmCommand.getName() == VMCommand.Command.OR) {
				generateOrCommand();
			} else if (vmCommand.getName() == VMCommand.Command.NOT) {
				generateNotCommand();
			} else if (vmCommand.getName() == VMCommand.Command.EQ) {
			} else if (vmCommand.getName() == VMCommand.Command.LT) {
			} else if (vmCommand.getName() == VMCommand.Command.GT) {
			}
		}
		generateNoop();
		System.out.println(asm);
	}

	private void generateNotCommand() {
		asm.append("@" + stackPointer + LINE);
		asm.append("A=M-1\n");
		asm.append("M=!M\n");
	}

	private void generateOrCommand() {
		decrementStackPointer();
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@" + stackPointer + "\n");
		asm.append("A=M-1\n");
		asm.append("D=D|M\n");
		asm.append("M=D\n");
	}

	private void generateAndCommand() {
		decrementStackPointer();
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@" + stackPointer + "\n");
		asm.append("A=M-1\n");
		asm.append("D=D&M\n");
		asm.append("M=D\n");
	}

	private void generateSubCommand() {
		decrementStackPointer();
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@" + stackPointer + "\n");
		asm.append("A=M-1\n");
		asm.append("D=D-M\n");
		asm.append("D=-D\n");
		asm.append("M=D\n");
	}

	private void generateAddCommand() {
		decrementStackPointer();
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@" + stackPointer + "\n");
		asm.append("A=M-1\n");
		asm.append("D=D+M\n");
		asm.append("M=D\n");
	}

	private void generateNegateCommand() {
		asm.append("@" + stackPointer);
		asm.append("A=M-1");
		asm.append("M=-M");
	}

	private void generatePopCommand(VMCommand vmCommand) {
		String segmentPointer = "R1";
		String segmentIndex = vmCommand.getArg2();

		calculateSegmentAddr(segmentPointer, segmentIndex);
		decrementStackPointer();

		// read from stack and store in segment
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@addr\n");
		asm.append("A=M\n");
		asm.append("M=D\n");
	}

	private void generatePushCommand(VMCommand vmCommand) {
		String segmentIndex = vmCommand.getArg2();
		String segmentPointer = "R1";

		// first calculate addr = segmentPointer + segmentIndex
		calculateSegmentAddr(segmentPointer, segmentIndex);
		// Push to stack
		asm.append("A=M\n");
		asm.append("D=M\n");
		asm.append("@" + stackPointer + LINE);
		asm.append("A=M\n");
		asm.append("M=D\n");
		incrementStackPointer();
	}

	private void generateNoop() {
		asm.append("(STOP)\n");
		asm.append("@STOP\n");
		asm.append("0;JEQ\n");
	}

	private void incrementStackPointer() {
		// increment the stack pointer
		asm.append("@" + stackPointer + "\n");
		asm.append("M=M+1\n");
	}

	private void decrementStackPointer() {
		// decrement the stack pointer
		asm.append("@" + stackPointer + "\n");
		asm.append("M=M-1\n");
	}

	private void calculateSegmentAddr(String segmentPointer, String segmentIndex) {
		asm.append("@" + segmentPointer + LINE);
		asm.append("D=M\n");
		asm.append("@" + segmentIndex + LINE);
		asm.append("D=D+A\n");
		asm.append("@addr\n");
		asm.append("M=D\n");
	}
}
