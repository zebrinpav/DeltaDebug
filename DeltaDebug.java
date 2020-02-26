import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Scanner;

public class DeltaDebug {

	/**
	 * deltaDebug is the method is what will run the delta debug algorithm !!!!!!
	 * IMPORTANT: DO NOT CHANGE THE TYPE/METHOD SIGNATURE AS IT MUST BE THE SAME AS
	 * PROVIDED FOR GRADING !!!!!!
	 * 
	 * @param char_granularity     - if false, use line granularity for the
	 *                             algorithm
	 * @param program              - the path of the program you're testing, e.g.
	 *                             "./SecretCoder"
	 * @param failing_file         - path of provided failing input file, e.g.
	 *                             "./input_file.txt"
	 * @param error_msg            - the program output that Delta should treat as
	 *                             an error, e.g.
	 *                             "java.lang.ArrayIndexOutOfBoundsException"
	 * @param final_minimized_file - path to write minimized output file to
	 */
	public void deltaDebug(Boolean char_granularity, String program, String failing_file, String error_msg,
			String final_minimized_file) {
		emsg = error_msg;
		prgName = program;
		lines = readFile(failing_file);

		List<String> result = ddmin(lines);
		if (char_granularity) {
			isCharGran = true;
			ArrayList<String> lines1 = new ArrayList<String>();
			for (String l : result) {
				for (int i = 0; i < l.length(); i++) {
					lines1.add(l.substring(i, i + 1));
				}

			}
			result = ddmin(lines1);
		}
		writeToFile(final_minimized_file, result);
	}

	// global variables
	boolean isCharGran = false;
	String prgName = "";
	String emsg = "";
	ArrayList<String> lines = null;
	Integer[] lines_for_remove = null;

	List<String> difference(List<String> a, List<String> b) {
		List<String> result = new LinkedList<String>();
		result.addAll(a);
		result.removeAll(b);
		return result;
	}

	boolean PTest(List<String> a) {
		String fname = "temp.txt";
		writeToFile(fname, a);
		return runCommand(prgName + " " + fname, emsg);
	}

	List<String> ddmin(List<String> input) {

		int n = 2;
		while (input.size() >= 2) {
			List<List<String>> subsets = split(input, n);
			boolean compFailing = false, subFailing = false;
			for (List<String> subset : subsets) {
				if (PTest(subset)) {
					input = subset;
					n = 2;
					subFailing = true;
					break;
				}
			}

			if (!subFailing) {
				for (int i = 0; i < subsets.size(); i++) {
					// System.out.println(i);
					List<String> complement = diff1(input, i, n);
					if (PTest(complement)) {
						input = complement;
						n = Math.max(n - 1, 2);
						compFailing = true;
						break;
					}
				}
				if (!compFailing) {
					if (n == input.size()) {
						break;
					}

					// increase set granularity
					n = Math.min(n * 2, input.size());
				}
			}
		}

		return input;
	}

	List<String> diff1(List<String> s, int i, int n) {
		List<String> subset = new ArrayList<String>();
		int pos1 = s.size() * i / n;
		int pos2 = Math.min(n, s.size() * (i + 1) / n);
		if (pos1 > 0)
			subset.addAll(s.subList(0, pos1));
		if (pos2 < n)
			subset.addAll(s.subList(pos2, n));
		return subset;
	}

	List<List<String>> split(List<String> s, int n) {
		List<List<String>> subsets = new LinkedList<List<String>>();
		int position = 0;
		for (int i = 0; i < n; i++) {
			List<String> subset = s.subList(position, position + (s.size() - position) / (n - i));
			subsets.add(subset);
			position += subset.size();
		}
		return subsets;
	}

	/**
	 * readFile reads input from a file line by line You can update this method to
	 * pass in more parameters or return something if needed
	 * 
	 * @param file - file to read
	 */
	public ArrayList<String> readFile(String file) {
		ArrayList<String> lines = new ArrayList<String>();
		Scanner scan;
		try {
			scan = new Scanner(new File(file));
			while (scan.hasNextLine()) {
				// do something
				String line = scan.nextLine();
				if (isCharGran) {
					for (int i = 0; i < line.length(); i++) {
						String c = line.substring(i, i + 1);
						lines.add(c);
					}
				} else
					lines.add(line);
			}
			scan.close();
		} catch (FileNotFoundException e) {
			e.printStackTrace();
		}
		return lines;
	}

	/**
	 * writeFile can be used to write to a file You can update this method to pass
	 * in more/different parameters or return something if needed
	 * 
	 * @param file - file to write to
	 * @param      ArrayList<String> - this is just a placeholder example, you can
	 *             use whatever data structure you want
	 */
	public void writeToFile(String file, List<String> ArrayList) {
		Path out = Paths.get(file);

		try {
			if (isCharGran) {
				String txt = String.join("", ArrayList);
				ArrayList<String> tmp = new ArrayList<String>();
				tmp.add(txt);
				Files.write(out, tmp, Charset.defaultCharset());
			} else {
				Files.write(out, ArrayList, Charset.defaultCharset());
			}
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * runCommand can be used to run a program You can update this method to pass in
	 * more/different parameters or return something if needed
	 * 
	 * @param command - complete command you want to run (program location + any
	 *                command args)
	 * @param error   - error message you're looking for
	 */
	public boolean runCommand(String command, String error_msg) {
		String s = null;
		try {
			Process p = Runtime.getRuntime().exec(command);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			// read the output from the command
			while ((s = stdInput.readLine()) != null) {
				// System.out.println(s);
				if (s.contains(error_msg))
					return true;
			}

		} catch (IOException e) {
			System.out.println(command + "failed to run");
			System.exit(-1);
		}
		return false;
	}

}
