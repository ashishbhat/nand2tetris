import java.nio.file.Path;
import java.nio.file.Paths;

public class JackAnalyzer {
	public static void main(String[] args) {
		Path sourcePath = Paths.get("/home/ashishbh/Downloads/projects/10/Square/SquareGame.jack");
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

		// START COMPILATION
		CompilationEngine compilationEngine = new CompilationEngine(sourcePath);
		compilationEngine.compile();
	}
}
