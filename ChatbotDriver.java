import java.io.*;
import java.util.*;
import java.util.regex.*;

public class ChatbotDriver {

	private static List<String> vocabulary;

	private static void readVocabulary() {
		File file = new File("vocabulary.txt");
		vocabulary = new ArrayList<String>();
		vocabulary.add("OOV"); // 0 -> OOV

		try (Scanner fin = new Scanner(file)) {
			while (fin.hasNextLine()) {
				vocabulary.add(fin.nextLine());
			}
		} catch (FileNotFoundException ex) {
			System.out.println("File not found.");
		}
	}

	public static String generateCommand(String userInput) {
		StringBuilder sb = new StringBuilder("java Chatbot 700 -1 ");
		List<Integer> prefix = new ArrayList<Integer>();
		
		String response = "";
		
		if (userInput.matches("(?i)(.*)(opinion on)(.*)")) {
			Pattern pattern = Pattern.compile("(?i)(.*)(opinion on)(.*)");
			Matcher matcher = pattern.matcher(userInput);
			while (matcher.find())
				response = removeFinalPunctuation(pronounChange(matcher.group(3)));
		}
		else if (userInput.matches("(?i)(.*)(what is)(.*)")) {
			Pattern pattern = Pattern.compile("(?i)(.*)(what is)(.*)");
			Matcher matcher = pattern.matcher(userInput);
			while (matcher.find())
				response = removeFinalPunctuation(pronounChange(matcher.group(3)));
		}
		else if (userInput.matches("(?i)(tell me a joke)(.*)") || 
			     userInput.matches("(?i)(say anything)(.*)")) {
			response = "";
		}
		else {
			response = removeFinalPunctuation(pronounChange(userInput));
		}

		for (String s : response.split(" ")) {
			int idx = vocabulary.indexOf(s);
			if (idx == -1) // OOV
				prefix.add(0);
			else
				prefix.add(idx);
		}
		sb.append(prefix.size() + " ");
		for (int i : prefix) {
			sb.append(i + " ");
		}
		System.out.print(response + " ");

		return sb.toString();
	}

	public static String removeFinalPunctuation(String input) {
		if (input.length() > 1 && (input.charAt(input.length() - 1) == '.' || input.charAt(input.length() - 1) == '?'
				|| input.charAt(input.length() - 1) == '!'))
			input = input.substring(0, input.length() - 1);
		return input.trim();
	}

	public static String pronounChange(String input) {
		String[] tokens = input.split(" ");
		StringBuilder sb = new StringBuilder();
		for (String token : tokens) {
			if (token.equalsIgnoreCase("i"))
				sb.append("you ");
			else if (token.equalsIgnoreCase("me"))
				sb.append("you ");
			else if (token.equalsIgnoreCase("my"))
				sb.append("your ");
			else if (token.equalsIgnoreCase("mine"))
				sb.append("yours ");
			else if (token.equalsIgnoreCase("you"))
				sb.append("i ");
			else if (token.equalsIgnoreCase("your"))
				sb.append("my ");
			else if (token.equalsIgnoreCase("yours"))
				sb.append("mine ");
			else if (token.equalsIgnoreCase("am"))
				sb.append("are ");
			else if (token.equalsIgnoreCase("are"))
				sb.append("am ");
			else
				sb.append(token + " ");
		}
		return sb.toString().trim();
	}

	public static void main(String[] args) throws Exception {
		Scanner in = new Scanner(System.in);
		readVocabulary();

		System.out.print("You: ");
		while (in.hasNextLine()) {
			String input = in.nextLine().trim().toLowerCase();
			System.out.print("Chatbot: ");
			String command = generateCommand(input);

			Runtime rt = Runtime.getRuntime();
			Process proc = rt.exec(command);

			BufferedReader stdInput = new BufferedReader(new InputStreamReader(proc.getInputStream()));

			String line;
			while ((line = stdInput.readLine()) != null) {
				String response = vocabulary.get(Integer.parseInt(line));
				System.out.print(removeFinalPunctuation(response) + " ");
			}

			System.out.println();
			System.out.print("You: ");
		}
		in.close();
	}

}
