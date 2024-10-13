package com.nand2tetris.VMTranslator;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

import com.nand2tetris.VMTranslator.VMCommand.Command;
import com.nand2tetris.VMTranslator.VMCommand.CommandType;

public class Parser {
	private List<VMCommand> commands;
	private String sourceFile;

	public Parser(String sourceFile) {
		super();
		this.sourceFile = sourceFile;
		this.commands = new ArrayList<VMCommand>();
	}

	public List<VMCommand> parse() {
		Path sourcePath = Paths.get(sourceFile);
		String sourceFileName = sourcePath.getFileName().toString().split("\\.")[0];
		List<String> allLines = null;
		try {
			allLines = Files.readAllLines(sourcePath);
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		for (String line : allLines) {
			if (line.contains("//") || line.isBlank()) {
				continue;
			}
			VMCommand vmCommand = getVmCommand(line);
			vmCommand.setSourcefile(sourceFileName);
			commands.add(vmCommand);
		}
		return commands;
	}

	private VMCommand getVmCommand(String line) {
		VMCommand vmCommand = new VMCommand();
		String[] tokens = line.split(" ");
		String commandName = tokens[0];
		Command command = getCommandName(commandName);
		CommandType commandType = getCommandType(command);
		vmCommand.setName(command);
		vmCommand.setCommandType(commandType);
		if (vmCommand.getCommandType() == VMCommand.CommandType.MEMORY) {
			vmCommand.setArg1(tokens[1]);
			vmCommand.setArg2(tokens[2]);
		}
		return vmCommand;
	}

	private Command getCommandName(String commandName) {
		commandName = commandName.toUpperCase();
		return Command.valueOf(commandName);
	}

	private CommandType getCommandType(Command command) {
		if (command == VMCommand.Command.ADD || command == VMCommand.Command.SUB || command == VMCommand.Command.NEG
				|| command == VMCommand.Command.EQ || command == VMCommand.Command.GT
				|| command == VMCommand.Command.LT) {
			return VMCommand.CommandType.AIRTHMETIC;
		} else if (command == VMCommand.Command.AND || command == VMCommand.Command.OR
				|| command == VMCommand.Command.NOT) {
			return VMCommand.CommandType.LOGIC;
		} else if (command == VMCommand.Command.POP || command == VMCommand.Command.PUSH) {
			return VMCommand.CommandType.MEMORY;
		} else {
			throw new IllegalArgumentException("Invalid Command " + command.name());
		}
	}

	/**
	 * @return the commands
	 */
	public List<VMCommand> getCommands() {
		return commands;
	}

	/**
	 * @param commands the commands to set
	 */
	public void setCommands(List<VMCommand> commands) {
		this.commands = commands;
	}

	/**
	 * @param sourceFile the sourceFile to set
	 */
	public void setSourceFile(String sourceFile) {
		this.sourceFile = sourceFile;
	}
}
