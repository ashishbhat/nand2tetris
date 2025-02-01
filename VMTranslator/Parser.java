import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class Parser {
	private List<VMCommand> commands;
	private String sourceCode;

	public Parser(String sourceFile) {
		super();
		this.sourceCode = sourceFile;
		this.commands = new ArrayList<VMCommand>();
	}

	public List<VMCommand> parse() throws IOException {
		System.out.println("Parsing sourceCode: " + sourceCode);
		List<Path> contents = null;
		Path sourcePath = Paths.get(sourceCode);
		boolean isDirectory = Files.isDirectory(sourcePath);
		if (isDirectory) {
			contents = Files.list(sourcePath).filter(x -> x.getFileName().toString().endsWith(".vm"))
					.collect(Collectors.toList());
		} else {
			contents = new ArrayList<Path>();
			contents.add(sourcePath);
		}

		for (Path path : contents) {
			String sourceFileName = path.getFileName().toString().split("\\.")[0];
			List<String> allLines = null;
			try {
				allLines = Files.readAllLines(path);
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
			for (String line : allLines) {
				String[] tokens = line.split("/");
				if (tokens.length == 0)
					continue;
				line = tokens[0];
				if (line.contains("//") || line.isBlank()) {
					continue;
				}
				VMCommand vmCommand = getVmCommand(line.strip());
				vmCommand.setSourcefile(sourceFileName);
				commands.add(vmCommand);
			}
		}
		return commands;
	}

	private VMCommand getVmCommand(String line) {
		System.out.println("Parsing command: " + line);

		VMCommand vmCommand = new VMCommand();
		String[] tokens = line.split(" ");
		String commandName = tokens[0];
		VMCommand.Command command = getCommandName(commandName);
		VMCommand.CommandType commandType = getCommandType(command);
		vmCommand.setName(command);
		vmCommand.setCommandType(commandType);
		if (tokens.length == 2) {
			vmCommand.setArg1(tokens[1]);

		}
		if (tokens.length == 3) {
			vmCommand.setArg1(tokens[1]);
			vmCommand.setArg2(tokens[2]);

		}
		return vmCommand;
	}

	private VMCommand.Command getCommandName(String commandName) {
		commandName = commandName.toUpperCase();
		if (commandName.equalsIgnoreCase("if-goto"))
			return VMCommand.Command.valueOf("IF_GOTO");
		return VMCommand.Command.valueOf(commandName);
	}

	private VMCommand.CommandType getCommandType(VMCommand.Command command) {
		if (command == VMCommand.Command.ADD || command == VMCommand.Command.SUB || command == VMCommand.Command.NEG
				|| command == VMCommand.Command.EQ || command == VMCommand.Command.GT
				|| command == VMCommand.Command.LT) {
			return VMCommand.CommandType.AIRTHMETIC;
		} else if (command == VMCommand.Command.AND || command == VMCommand.Command.OR
				|| command == VMCommand.Command.NOT) {
			return VMCommand.CommandType.LOGIC;
		} else if (command == VMCommand.Command.POP || command == VMCommand.Command.PUSH) {
			return VMCommand.CommandType.MEMORY;
		} else if (command == VMCommand.Command.LABEL || command == VMCommand.Command.IF_GOTO
				|| command == VMCommand.Command.GOTO) {
			return VMCommand.CommandType.BRANCHING;
		} else if (command == VMCommand.Command.FUNCTION || command == VMCommand.Command.RETURN
				|| command == VMCommand.Command.CALL) {
			return VMCommand.CommandType.FUNCTION;
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
	 * @param sourceCode the sourceCode to set
	 */
	public void setSourceCode(String sourceCode) {
		this.sourceCode = sourceCode;
	}

}
