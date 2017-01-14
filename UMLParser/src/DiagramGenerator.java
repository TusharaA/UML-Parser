
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.ParserRuleContext;
import org.antlr.v4.runtime.Token;
import org.antlr.v4.runtime.tree.ParseTree;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import net.sourceforge.plantuml.SourceStringReader;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class DiagramGenerator {

	static InputStream inputStream;
	static String classInterfaceOutput = "";
	static ArrayList<String> classNames = new ArrayList<>();

	public static void main(String[] args) throws Exception {

		RelationParser relParser = new RelationParser();
		
		File folder = new File(args[0]);
		File[] listOfFiles = folder.listFiles();
		
		 for (File file : listOfFiles) {
			if (file.isFile() && file.getName().contains(".java")) {
			String name = file.getName().substring(0, file.getName().length() - 5);
			classNames.add(name);
			}
		}
		
		for (File file : listOfFiles) {
			if (file.isFile() && file.getName().contains(".java")) {
			inputStream = new FileInputStream(file); 
			ANTLRInputStream input = new ANTLRInputStream(inputStream);

			JavaLexer lexer = new JavaLexer(input);
			CommonTokenStream tokens = new CommonTokenStream(lexer);
			JavaParser parser = new JavaParser(tokens);
			ParseTree tree = parser.compilationUnit();

			ParseTreeWalker walker = new ParseTreeWalker();
			MainClass extractor = new MainClass(parser);
			walker.walk(extractor, tree);

			relParser.interfaceMap.put(extractor.currentClass, extractor.isInterface);
			relParser.relParserMap.put(extractor.currentClass, extractor.relationStructMap);
		}
		}

		String aRelation = relParser.parseRelMap();
		String inputToPlantUml = "@startuml\n" + classInterfaceOutput + aRelation + "@enduml\n";
		//System.out.println(inputToPlantUml);

		try {
			String imageName =  args[1] + ".jpg";
			OutputStream finalImage = new FileOutputStream(imageName);
			SourceStringReader reader = new SourceStringReader(inputToPlantUml);
			System.out.println("Generating Image " + imageName);
			reader.generateImage(finalImage);
			System.out.println("Opening Image " + imageName);
		}
		catch (Exception e) {

		} 
	}
}