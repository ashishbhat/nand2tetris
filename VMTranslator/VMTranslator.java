import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

public class VMTranslator {
	public static void main(String[] args) throws IOException {
		Path sourcePath = Paths.get(args[0]);
		boolean isDirectory = Files.isDirectory(sourcePath);
		Parser parser = new Parser(args[0]);
		List<VMCommand> commands = parser.parse();
		File sourceCode = new File(args[0]);
		String outFile = "";
		if (isDirectory) {
			outFile = sourceCode.getPath() + "/" + sourcePath.getFileName().toString() + ".asm";
		} else {
			outFile = sourceCode.getParent() + "/" + sourcePath.getFileName().toString().split("\\.")[0] + ".asm";
		}
		CodeWriter codeWriter = new CodeWriter(outFile, isDirectory);
		codeWriter.translate(commands);
	}
}
