import java.io.*;
import java.util.*;

public class test {
	public static void main(String args[]) {
		BitReader in = null;
		BitWriter out = null;
		try {
			out = new BitWriter("testdata");
			out.write_0();
			out.writeChar('a');
			out.write_1();
			out.close();
			in = new BitReader("testdata");
			/*
			while (!in.isEOF())
				System.out.print(in.readBit());
			*/
			in.readBit();
			System.out.print("a : " + (char) in.readChar());
			in.close();
			System.out.println();
/*
			in = new BitReader("temp.huf");
			while (!in.isEOF())
				System.out.print(in.readBit());
			in.close();
*/
		} catch (Exception e) {e.printStackTrace(); System.exit(1);}
	}
}