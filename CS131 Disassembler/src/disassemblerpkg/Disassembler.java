package disassemblerpkg;

public class Disassembler {

	public static void main(String[] args) {
		
		/* To be put here the interface of input */
		
		String filename = "helloworld.c";
		
		/* To be put here the interface of input */
	
		String originalCode =new readFile(filename).getCode();
		String convertedCode = new String();
		
		String extension = filename.substring(filename.indexOf('.')+1).toLowerCase();
		if(extension.equals("c")){
			convertedCode = new toAssembly(originalCode).getCode();
		} 
		else if(extension.equals("asm")){
			convertedCode = new toCLanguage(originalCode).getCode();
		} 
		else{
			System.out.println("Not an assembly code or a C Langage code!");
		}
		
		System.out.println(convertedCode);
		
	}
}
