package disassemblerpkg;

import java.util.StringTokenizer;

public class Disassembler {

	public static void main(String[] args) {
		
		/* To be put here the interface of input */
		
		String filename = "helloworld.c";
		
		/* To be put here the interface of input */
	
		String originalCode =new ReadFile(filename).getCode();
		String convertedCode = new String();
		
		String trimmedOriginalCode = new String();
			StringTokenizer tokenizer = new StringTokenizer(originalCode, "\n");
			while(tokenizer.hasMoreTokens()){
				trimmedOriginalCode += tokenizer.nextToken().trim() + "\n";
			}
		
		String extension = filename.substring(filename.indexOf('.')+1).toLowerCase();
		if(extension.equals("c")){
			convertedCode = new ToAssembly(trimmedOriginalCode).getCode();
		} 
		else if(extension.equals("asm")){
			convertedCode = new ToCLanguage(trimmedOriginalCode).getCode();
		} 
		else{
			System.out.println("Not an assembly code or a C Langage code!");
		}
		
		System.out.println("Original Code: \n\n" + originalCode);
		System.out.println("Converted Code: \n\n" + convertedCode);
		
	}
}
