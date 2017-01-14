
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Vector;

import org.antlr.v4.runtime.ParserRuleContext;

public class MainClass extends JavaBaseListener {

	Vector<PropertyAttributes> propertyValue = new Vector<>(); // contains
																															// variable
																															// declarations
	Vector<MethodAttributes> methodValue = new Vector<>(); // contains method
																													// declarations

	HashMap<String, RelationStructure> relationStructMap = new HashMap<>(); // contains
																																					// relation
																																					// details

	JavaParser parser;
	String currentClass;

	boolean linkAddedVar = false;
	boolean linkAddedMtd = false;
	boolean linkAddedMtdAsso = false;
	boolean isInterface = false;

	String methodOutput = "";
	String propOutput = "";

	public MainClass(JavaParser parser) {
		this.parser = parser;
	}

	//add a new relation structure
	private RelationStructure fetchRelStruc(String className) {

		if (className != null) {
			RelationStructure valuesRel = (RelationStructure) relationStructMap.get(className);

			if (valuesRel == null) {
				valuesRel = new RelationStructure();
				valuesRel.endClass = className;
				relationStructMap.put(className, valuesRel);
			}

			return valuesRel;
		}
		else {
			return null;
		}
	}
	
	//to check if there are multiple instances of same class/interface
	private boolean checkRelStruc(String className) {

		if (className != null) {
			RelationStructure valuesRel = (RelationStructure) relationStructMap.get(className);

			if (valuesRel == null) {
				return false;
			}

			return true;
		}
		else {
			return false;
		}
	}

	@Override
	public void enterClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
		currentClass = ctx.getChild(1).getText();
		isInterface = false;

		DiagramGenerator.classInterfaceOutput = DiagramGenerator.classInterfaceOutput + ctx.getChild(0).getText() + " "
				+ currentClass;
		for (int i = 2; i < ctx.getChildCount(); i++) {
			if (ctx.getChild(i).getText().contains("implements")) {
				int implementCount = ctx.getChild(i + 1).getChildCount();
				if (implementCount == 1) {
					RelationStructure relStr = fetchRelStruc(ctx.getChild(i + 1).getText());
					relStr.isImplements = true;
				}
				else {
					for (int k = 0; k < implementCount; k = k + 2) {
						RelationStructure relStr = fetchRelStruc(ctx.getChild(i + 1).getChild(k).getText());
						relStr.isImplements = true;
					}
				}
			}
			if (ctx.getChild(i).getText().contains("extends")) {
				RelationStructure relStr = fetchRelStruc(ctx.getChild(i + 1).getText());
				relStr.isExtends = true;
			}
		}
	}

	@Override
	public void enterInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
		super.enterInterfaceDeclaration(ctx);
		currentClass = ctx.getChild(1).getText();
		isInterface = true;
		DiagramGenerator.classInterfaceOutput = DiagramGenerator.classInterfaceOutput + ctx.getChild(0).getText() + " "
				+ currentClass;
	}

	@Override
	public void enterFieldDeclaration(JavaParser.FieldDeclarationContext ctx) {
		String symbol = "";

		String accessModifier = ctx.getParent().getParent().getChild(0).getChild(0).getText();
		if (accessModifier.equals("public")) {
			symbol = "+";
		}
		if (accessModifier.equals("private")) {
			symbol = "-";
		}
		if (ctx.type() != null) { 

			if (ctx.type().primitiveType() != null && !(accessModifier.equals("protected")) && ctx.getParent().getParent().getChildCount() > 1) {
				propertyValue.add(new PropertyAttributes(symbol, ctx.getChild(1).getChild(0).getChild(0).getText(),
						ctx.getChild(0).getText()));
			}
			if (ctx.type().classOrInterfaceType() != null) {
				
				if (!checkClassPresent(ctx.getChild(0).getText()) && !(accessModifier.equals("protected")) && ctx.getParent().getParent().getChildCount() > 1 && !checkClassContains(ctx.getChild(0).getChild(0).getText())) {
					
					propertyValue.add(new PropertyAttributes(symbol, ctx.getChild(1).getChild(0).getChild(0).getText(),
							ctx.getChild(0).getText()));
				}
				else if (checkCollectionType(ctx.getChild(0).getChild(0).getText()))
				{
					RelationStructure relStr = fetchRelStruc(
							ctx.getChild(0).getChild(0).getChild(1).getChild(1).getChild(0).getText());
					relStr.isLocalVar = true;
					relStr.isCollection = true;
				}
				else {
					boolean isColl = false;
					if (checkRelStruc(ctx.type().classOrInterfaceType().getChild(0).getText())) {
						isColl = true;
					}
					
					RelationStructure relStr = fetchRelStruc(ctx.type().classOrInterfaceType().getChild(0).getText());
					relStr.isLocalVar = true;
					
					if (isColl) {
						relStr.isCollection = true;
					}
				}
			}
		}
	}
	
	private boolean checkClassPresent(String className) 
	{
		return DiagramGenerator.classNames.contains(className);
	}
	
	private boolean checkClassContains(String className) 
	{
		boolean contains = false;
		
		for (String cName : DiagramGenerator.classNames) 
		{
			if (className.contains("<" + cName + ">")) 
			{
				contains = true;
				break;
			}
		}
		
		return contains;
	}
	
	private boolean checkCollectionType(String className) 
	{
		boolean contains = false;
		
			if (className.contains("<") && className.contains(">") && className.indexOf("<") < className.indexOf(">")) 
			{
				contains = true;
			}
		
		return contains;
	}

	@Override
	public void enterFormalParameter(JavaParser.FormalParameterContext ctx) {
		if (ctx.type().classOrInterfaceType() != null
				&& !(ctx.type().classOrInterfaceType().getChild(0).getText().contains("String")) && linkAddedMtd == false) {
			linkAddedMtd = true;
			RelationStructure relStr = fetchRelStruc(ctx.type().classOrInterfaceType().getChild(0).getText());
			relStr.isMethod = true;
		}
	}

	@Override
	public void enterLocalVariableDeclaration(JavaParser.LocalVariableDeclarationContext ctx) {
		if (ctx.type().classOrInterfaceType() != null
				&& !(ctx.type().classOrInterfaceType().getChild(0).getText().contains("String")) && linkAddedVar == false) {
			linkAddedVar = true;
			RelationStructure relStr = fetchRelStruc(ctx.type().classOrInterfaceType().getChild(0).getText());
			relStr.isLocalVar = true;
		}
	}

	@Override
	public void enterMethodDeclaration(JavaParser.MethodDeclarationContext ctx) {
		enterCommonMethodDeclaraion(ctx, ctx.formalParameters(), false);
	}

	@Override
	public void enterConstructorDeclaration(JavaParser.ConstructorDeclarationContext ctx) {
		enterCommonMethodDeclaraion(ctx, ctx.formalParameters(), true);
	}

	@Override
	public void enterInterfaceMethodDeclaration(JavaParser.InterfaceMethodDeclarationContext ctx) {
		enterCommonMethodDeclaraion(ctx, ctx.formalParameters(), false);
	}

	public void enterCommonMethodDeclaraion(ParserRuleContext ctx,
			JavaParser.FormalParametersContext formalParametersContext, Boolean isConst) {

		String accessModifier = ctx.getParent().getParent().getChild(0).getText();
		String containsStatic = "";
		
		if (accessModifier.equals("public")) {
			MethodAttributes methodAttributes;
			if (formalParametersContext.getChildCount() > 2) {
				ArrayList<FormalParameters> formalParameterList = new ArrayList<>();
				for (int i = 0; i < formalParametersContext.formalParameterList().children.size(); i = i + 2) {
					FormalParameters formalParameters = new FormalParameters();
					formalParameters.nameClass = formalParametersContext.formalParameterList().getChild(i).getChild(0).getText();
					formalParameters.nameInstance = formalParametersContext.formalParameterList().getChild(i).getChild(1)
							.getText();

					formalParameterList.add(formalParameters);
				}
				methodAttributes = new MethodAttributes(containsStatic + "+", ctx.getChild(1).getText(), ctx.getChild(0).getText(),
						formalParameterList, isConst);
			}
			else {
				methodAttributes = new MethodAttributes(containsStatic + "+", ctx.getChild(1).getText(), ctx.getChild(0).getText(), null,
						isConst);
			}
			methodValue.add(methodAttributes);
		}
	}

	@Override
	public void exitInterfaceDeclaration(JavaParser.InterfaceDeclarationContext ctx) {
		super.exitInterfaceDeclaration(ctx);
		DiagramGenerator.classInterfaceOutput += "{" + "\n";
		for (int i = 0; i < propertyValue.size(); i++) {
			DiagramGenerator.classInterfaceOutput += propertyValue.get(i).accessModifier + propertyValue.get(i).propertyName
					+ ":" + propertyValue.get(i).dataType + "\n";
		}

		for (int i = 0; i < methodValue.size(); i++) {
			MethodAttributes methodAttributes = methodValue.get(i);
			methodOutput += methodAttributes.methodFormat() + "\n";
		}

		DiagramGenerator.classInterfaceOutput += methodOutput;
		DiagramGenerator.classInterfaceOutput += "}" + "\n";
	}

	public static String decapitalizeString(String string) {
		return ((string == null) || (string.isEmpty()) ? ""
				: Character.toLowerCase(string.charAt(0)) + string.substring(1));
	}

	@Override
	public void exitClassDeclaration(JavaParser.ClassDeclarationContext ctx) {
		super.exitClassDeclaration(ctx);
		ArrayList<String> variableList = new ArrayList<>();
	// removing getter setter methods if any and making the attribute public
		for (int i = 0; i < propertyValue.size(); i++) {
			variableList.add(propertyValue.get(i).propertyName);
		}

		Vector<MethodAttributes> methodValTemp = new Vector<>(methodValue);

		for (int i = 0; i < methodValTemp.size(); i++) {

			if (!methodValTemp.get(i).isConstructor) {
				if (methodValTemp.get(i).methodName.startsWith("get")) {
					String getterName = methodValTemp.get(i).methodName.replaceFirst("get", "");
					String varName = decapitalizeString(getterName);
					String setterName = "set" + getterName;

					for (int j = 0; j < methodValTemp.size(); j++) {
						if (methodValTemp.get(j).methodName.equals(setterName)) {
							if (variableList.contains(varName)) {
								int index = variableList.indexOf(varName);
								propertyValue.get(index).accessModifier = "+";
								methodValue.remove(methodValTemp.get(j));
								methodValue.remove(methodValTemp.get(i));
								break;
							}
						}
					}
				}
			}
		}

		for (int i = 0; i < methodValue.size(); i++) {
			MethodAttributes methodAttributes = methodValue.get(i);
			methodOutput += methodAttributes.methodFormat() + "\n";
		}

		DiagramGenerator.classInterfaceOutput += "{" + "\n";
		for (int i = 0; i < propertyValue.size(); i++) {
			PropertyAttributes propertyAttributes = propertyValue.get(i);
			propOutput += propertyAttributes.propertyFormat() + "\n";
		}
		DiagramGenerator.classInterfaceOutput += propOutput;
		DiagramGenerator.classInterfaceOutput += methodOutput;
		DiagramGenerator.classInterfaceOutput += "}" + "\n";
	}
}