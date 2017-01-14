
import java.util.ArrayList;

public class MethodAttributes {

	String methodAccessModifier;
	String methodName;
	String returnType; // returnType will be null in case of constructor
	boolean isConstructor = false;
	ArrayList<FormalParameters> formalParameterList;

	public MethodAttributes(String methodAccessModifier, String methodName, String returnType,
			ArrayList<FormalParameters> formalParameterList, boolean isConstructor) {

		this.methodAccessModifier = methodAccessModifier;
		this.methodName = methodName;
		this.returnType = returnType;
		this.formalParameterList = formalParameterList;
		this.isConstructor = isConstructor;
	}

	public String methodFormat() {

		String aMethodName = methodName;
		if (isConstructor) {
			aMethodName = returnType;
		}

		String mDescr = methodAccessModifier + aMethodName + "(";

		if (formalParameterList != null) {
			for (int i = 0; i < formalParameterList.size(); i++) {
				if (i != 0) {
					mDescr += ",";
				}
				mDescr += formalParameterList.get(i).description();
			}
		}

		if (isConstructor)
			mDescr += ")";
		else
			mDescr += "):" + returnType;
		return mDescr;
	}
}
