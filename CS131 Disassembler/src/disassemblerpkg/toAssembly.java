package disassemblerpkg;

import java.util.ArrayList;
import java.util.StringTokenizer;

public class toAssembly {

	private static String convertedCode = new String();
	
	private static ArrayList<String> dataContent = new ArrayList<String>();
	
	/*
	 * Function:
	 * 	Converts input C code to assembly code
	 * Parameter:
	 * 	String originalCode - the C code to be converted
	 * Returns:
	 * 	none
	 * */
	toAssembly(String originalCode) {
		
		/*
		 * convertedCode[0] - from start of assembly code to .data
		 * convertedCode[1] - the content of the data segment
		 * convertedCode[2] - from .code to the start of the body
		 * convertedCode[3] - the body
		 * convertedCode[4] - the end of body
		 * */
		String[] convertedCode = {"", "", "", "", ""};
		
		convertedCode[0] = ".model small \n";
		convertedCode[0] += ".stack 100h \n";
		convertedCode[0] += ".data \n";
		
		StringTokenizer tokenizer = new StringTokenizer(originalCode, "\n");
		while(tokenizer.hasMoreTokens()){
			String line = tokenizer.nextToken();
			
			if(line.contains("main(){")){
				convertedCode[2] += ".code \n\n";
				convertedCode[2] += "main proc \n\n";
				convertedCode[2] += "\t mov ax, @data \n";
				convertedCode[2] += "\t mov ds, ax \n\n";
				
				line = tokenizer.nextToken();
				String body = new String();
				while(!(line.contains("}") && !tokenizer.hasMoreTokens())){
					body += line + "\n";
					line = tokenizer.nextToken();
				}
				
				convertedCode[3] = toAssembly.bodyParser(body);
				
				convertedCode[4] += "\t mov ax, 4c00h \n";
				convertedCode[4] += "\t int 21h \n\n";
				convertedCode[4] += "main endp \n";
				convertedCode[4] += "end main";
			}
		}
		
		for(int i = 0; i < dataContent.size(); i++){
			convertedCode[1] += dataContent.get(i);
		}
		
		for(String code :  convertedCode){
			toAssembly.convertedCode += code;
		}
	}
	
	/*
	 * Function:
	 * 	converts the body of the assembly code into C Language
	 * Parameter:
	 * 	String body - the assembly code to be converted
	 * Returns:
	 * 	String - the converted assembly body code
	 * */
	private static String bodyParser(String body){
		
		/*
		 * FILL UP DATA CONTENT IF IF NECESSARY
		 * */
		
		return body;
	}

	/*
	 * Function:
	 * 	Returns the converted assembly language
	 * Parameter:
	 * 	none
	 * Returns:
	 * 	String - the converted assembly code
	 * */
	String getCode() {
		return toAssembly.convertedCode;
	}
}
