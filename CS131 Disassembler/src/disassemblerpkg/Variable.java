package disassemblerpkg;

public class Variable {
	public String cType;
	public String asmType;
	public String value;
	
	public Variable(){
		cType = "";
		asmType = "";
		value = "";
	}
	
	public Variable(String value, String cType, String asmType){
		this.cType = cType;
		this.asmType = asmType;
		this.value = value;
	}
}
