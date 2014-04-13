import java.io.*;
import java.util.*;

public class SHuffman {

	/* main entry for program */
	public static void main(String args[]) {
		SHuffman h = new SHuffman();
		if (args.length == 3) {
			long startuptime = System.currentTimeMillis();
			if (args[0].compareTo("-d") == 0) {
				System.out.print("Decoding");
				h.decode(args[1], args[2]);
			} else if (args[0].compareTo("-e") == 0) {
				System.out.println("Encoding");
				h.encode(args[1], args[2]);
				System.out.println(" compress ratio %" + (100 - (new File(args[2])).length() * 100 / (new File(args[1])).length()));
			} else {
				System.out.println("parameters are illegal!");
				return;
			}
			System.out.println(" took " + (System.currentTimeMillis() - startuptime) + " miliseconds.");
		} else {
			System.out.println("USAGE: java SHuffman -option inputFile outputFile");
			System.out.println("\toptions are:");
			System.out.println("\t -d decode (uncompress)");
			System.out.println("\t -e encode (compress)");
		}
	} /* end of public static void main(String args[]) */

	public SHuffman() {
		freqTable = new int[257]; /* + 1 for EOF */
		table = new Tree[257];
	} /* end of default constructor */

	/* this function decodes given input file (huffman coded) and reproduces orginal one */
	public void decode(String inputFile, String outputFile) {
		DataInputStream in = null;
		BufferedOutputStream out = null;
		try {
			in = new DataInputStream(new BufferedInputStream(new FileInputStream(inputFile)));
			out = new BufferedOutputStream(new FileOutputStream(outputFile));
		} catch (FileNotFoundException e) {e.printStackTrace(); System.exit(1);}

		try {

			// load freqTable
			for (int i = 0; i < 256; i++) {
				freqTable[i] = in.readInt();
			}
			// generate Huffman Tree
			freqTable[256] = 1;
			Tree root = buildHuffmanTree();

			Tree iterator = root; // start at root
			int bitNumber = 7;
			int nextByte;

			while ((nextByte = in.read()) != -1) {
				while (true) {
					if (iterator.left != null) { // is a leaf? no
						if ((1 << bitNumber & (byte) nextByte) == 0) iterator = iterator.left;
						 else iterator = iterator.right;
						if (bitNumber-- == 0) {bitNumber = 7; break;}
					} else { // is a leaf? yes
						if (iterator == table[256]) break;
						out.write(iterator.val);
						iterator = root; // go back to the root
					}
				}
			}
			in.close();
			out.close();
		} catch (IOException e) {e.printStackTrace(); System.exit(2);}
	} /* end of public void decode(String inputFile, String outputFile) */


	/* this function encodes given input file and produces a Huffman coded file */
	public void encode(String inputFile, String outputFile) {
		buildFreqTable(inputFile); // generate frequency table
		Tree root = buildHuffmanTree(); // build huffman tree

		BufferedInputStream in = null;
		DataOutputStream out = null;
		try {
			in = new BufferedInputStream(new FileInputStream(inputFile));
			out = new DataOutputStream(new BufferedOutputStream(new FileOutputStream(outputFile)));
		} catch (FileNotFoundException e) {e.printStackTrace(); System.exit(1);}

		try {
			Tree temp;
			int intRead;
			byte data[] = new byte[1000]; // stack like structure
			int top; // top value for data[]
			int nextByte = 0;
			int bitNumber = 7;

			// write freq table
			for (int i = 0; i < 256; i++) {
				out.writeInt(freqTable[i]);
			}

			// write code
			while ((intRead = in.read()) != -1) {
				if (table[intRead] != null) {
					temp = table[intRead];
					top = 0;
					while (temp.parent != null) {
						if (temp.parent.left == temp) data[top++] = 0;
						 else data[top++] = 1;
						temp = temp.parent;
					}
					for (int i = top; i > 0; i--) {
						if (data[--top] == 1) // write 1
						 nextByte |= 1 << bitNumber;
						if (bitNumber-- == 0) {
							bitNumber = 7;
							out.write(nextByte);
							nextByte = 0;
						}
					}
				} else {System.out.println("this should not happen"); System.exit(2);}
			};
			// write eof
			temp = table[256];
			top = 0;
			while (temp.parent != null) {
				if (temp.parent.left == temp) data[top++] = 0;
				 else data[top++] = 1;
				temp = temp.parent;
			}
			for (int i = top; i > 0; i--) {
				if (data[--top] == 1) // write 1
				 nextByte |= 1 << bitNumber;
				if (bitNumber-- == 0) {
					bitNumber = 7;
					out.write(nextByte);
					nextByte = 0;
				}
			}
			in.close();
			if (bitNumber < 7) out.write(nextByte);
			out.close();
		} catch (IOException e) {e.printStackTrace(); System.exit(2);}
	} /* end of public void encode(String inputFile, String outputFile) */

	/* this function creates huffman tree */
	private Tree buildHuffmanTree() {

		LinkedList list = new LinkedList();

		for (int i = 0; i < 257; i++) {
			if (freqTable[i] > 0) {
				int j = 0;
				for (; j < list.size(); j++)
					if (((Tree) list.get(j)).weight >= freqTable[i]) break;
				list.add(j, table[i] = new Tree(i, freqTable[i], null, null, null));
			}
		}

		while (list.size() > 1) {
			Tree left = (Tree) list.removeFirst();
			Tree right = (Tree) list.removeFirst();
			Tree tmpTree = new Tree(0, left.weight + right.weight, left, right, null);
			left.parent = tmpTree;
			right.parent = tmpTree;
			int i = 0;
			for (; i < list.size(); i++)
				if (((Tree) list.get(i)).weight >= tmpTree.weight) break;
			list.add(i, tmpTree);
		}

		return (Tree) list.removeFirst();
	} /* end of private Tree buildHuffmanTree() */

	/* this function generates frequency table from given file */
	private void buildFreqTable(String inputFile) {
		BufferedInputStream in = null;
		try {
			in = new BufferedInputStream(new FileInputStream(inputFile));
		} catch (Exception e) {e.printStackTrace(); System.exit(1);}

		try {
			int ch;
			while ((ch = in.read()) != -1) {
					freqTable[ch]++;
			}
			freqTable[256] = 1;
			in.close();
		} catch (Exception e) {e.printStackTrace(); System.exit(2);}
	} /* end of private void buildFreqTable(String inputFile) */

	private int freqTable[]; // frequency table
	private Tree table[]; // fast look up for leaves

	private class Tree {

		Tree() {
			val = 0;
			weight = 0;
			left = null;
			right = null;
			parent = null;
		} // end of default constructor

		Tree(int value, int wei, Tree l, Tree r, Tree p) {
			val = value;
			weight = wei;
			left = l;
			right = r;
			parent = p;
		} // end of constructor Tree(int value, int wei, Tree l, Tree r, Tree p)

		Tree parent;
		Tree left;
		Tree right;
		int val;
		int weight;
	} /* end of private class Tree */
}