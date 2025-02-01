package com.nand2tetris.VMTranslator;

import java.util.List;

public class VMTranslator {
	public static void main(String[] args) {
		Parser parser = new Parser(args[0]);
		List<VMCommand> commands = parser.parse();
		CodeWriter codeWriter = new CodeWriter("abc");
		codeWriter.translate(commands);
	}
}
