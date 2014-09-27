package disassemblerpkg;

import java.io.*;

public class ToAssembly {
	public static void main(String[] args) {
		try (BufferedReader br = new BufferedReader(new FileReader("helloworld.c"))) {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();
			
			System.out.println("Contents of the .c file:\n");
			
			while (line != null) {
				sb.append(line);
				sb.append(System.lineSeparator());
				line = br.readLine();
			}
			String everything = sb.toString();
			System.out.println(everything);
		} catch (FileNotFoundException e) {
			System.out
					.println("The file could not be found or could not be opened.");
		} catch (IOException e) {
			System.out.println("Error reading file.");
		}
	}
}
