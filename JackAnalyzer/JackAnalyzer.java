import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class JackAnalyzer {
	public static void main(String[] args) throws IOException {
		Path sourcePath = Paths.get(args[0]);
		/*-
		JackTokenizer tokenizer = new JackTokenizer(sourcePath);
		StringBuilder tokenizedOutput = new StringBuilder();
		
		// PRINT TOKEN OUTPUT
		tokenizedOutput.append("<tokens>\n");
		tokenizer.advance();
		while (tokenizer.hasMoreTokens()) {
			String token = tokenizer.token();
			String tokenType = tokenizer.tokenType();
			tokenizedOutput.append("<" + tokenType + ">");
			tokenizedOutput.append(" " + token + " ");
			tokenizedOutput.append("</" + tokenType + ">");
			tokenizedOutput.append("\n");
			tokenizer.advance();
		}
		tokenizedOutput.append("</tokens>");
		// System.out.print(tokenizedOutput);
		*/

		// START COMPILATION
		List<Path> files = Files.walk(sourcePath, 2).filter(p -> !Files.isDirectory(p)).collect(Collectors.toList());
		for (Path file : files) {
			CompilationEngine compilationEngine = new CompilationEngine(file);
			compilationEngine.compile();
		}
	}
}
