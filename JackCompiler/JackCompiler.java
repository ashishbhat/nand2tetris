import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

public class JackCompiler {
	public static void main(String[] args) throws IOException {
		Path sourcePath = Paths.get(args[0]);
		List<Path> files = Files.walk(sourcePath, 2).filter(p -> !Files.isDirectory(p))
				.filter(p -> p.getFileName().toString().endsWith(".jack")).collect(Collectors.toList());
		for (Path file : files) {
			CompilationEngine compilationEngine = new CompilationEngine(file);
			compilationEngine.compile();
		}
	}
}
