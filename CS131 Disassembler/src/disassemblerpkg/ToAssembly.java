package disassemblerpkg;

import java.io.*;

public class ToAssembly {
	
	private String readFile() {
		String originalCode = "", convertedCode = "";
		
		try (BufferedReader br = new BufferedReader(new FileReader("helloworld.c"))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			
			ToAssembly fnc = new ToAssembly();
			
			convertedCode += ".model small\n.data\n";
			
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				originalCode += line + "\n";
				if(line != null) {
					convertedCode += fnc.parse(line);
				}
				line = br.readLine();
			}
			System.out.println("originalCode\n"+originalCode);
			convertedCode += "mov ax, 4c00h\nint 21h\n\nmain endp\nend main";
			System.out.println("convertedCode\n"+convertedCode);
			return convertedCode;
			
		} catch (FileNotFoundException e) {
			System.out.println("The file could not be found or could not be opened.");
		} catch (IOException e) {
			System.out.println("Error reading file.");
		}
		return convertedCode;
	}
	
	private String parse(String line) {
		String convertedLine = "", var = "", value = "";
		if(line.equals("main(){")) {
			convertedLine += ".stack 100h\n.code\nmain proc\n\nmov ax, @data\nmov ds, ax\n\n";
		}
		
		if(line.contains("int ") || line.contains("String ")){
			int equalsIndex = line.indexOf("=");
			int spaceIndex = line.indexOf(" ");
			if (equalsIndex == -1) {
				var = line.substring(spaceIndex + 1, line.length() - 1).trim();
				convertedLine += "\t" + var + " db ?\n";
			}
			else {
				var = line.substring(spaceIndex, equalsIndex).trim();
				value = line.substring(equalsIndex + 1, line.length() - 1).trim();
				convertedLine += "\t" + var + " db " + value + "\n";
			}
		}
		
		return convertedLine;
	}
	
	public static void main(String[] args) {
		ToAssembly toa = new ToAssembly();
		String convertedCode = toa.readFile();
	}
}
