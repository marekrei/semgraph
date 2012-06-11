package sem.tokeniser;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;

import sem.util.FileWriter;

/**
 * A very simple tokeniser implementation.
 * 
 * <p>Separates text into tokens and sentences using heuristic rules.
 * It does not function well in cases where sentences are surrounded by quotes, so it is best to remove those beforehand.
 *
 */
public class Tokeniser{
	/**
	 * Split the text into tokens and sentences.
	 * Tokens are split on non-alphanumeric characters, except '-'.
	 * @param	 text 	Text to be tokenised.
	 * @return 			Tokenised text, with tokens separated by whitespace.
	 */
	public static String tokenise(String text) {
		if(text.trim().length() == 0)
			return "";
		
		StringBuilder stringBuilder = new StringBuilder();
		char c;
		stringBuilder.append(" ");
		
		// Iterate over every character in the original text
		for(int i = 0; i < text.length(); i++){
			c = text.charAt(i);
			// If it is a letter, digit, - or space, append the character.
			if(Character.isLetter(c) || Character.isDigit(c) || c == '-' || c == ' ')
				stringBuilder.append(text.charAt(i));
			// If it is a dot between two digits (e.g. 0.75), append the character
			else if(c == '.' && ((i > 0 && Character.isDigit(text.charAt(i-1)) && i < text.length()-1 && Character.isDigit(text.charAt(i+1)))))
				stringBuilder.append(text.charAt(i));
			// Otherwise, separate the character between spaces as a separate token
			else
				stringBuilder.append(" " + c + " ");
		}
		stringBuilder.append(" ");
		
		// Apply some post-processing rules to correct special cases
		HashMap<String,String> substitutionPatterns = new HashMap<String,String>();
		substitutionPatterns.put(" etc . ", " etc. ");
		substitutionPatterns.put(" e . g . ", " e.g. ");
		substitutionPatterns.put(" e g . ", " eg. ");
		substitutionPatterns.put(" vs . ", " vs. ");
		substitutionPatterns.put(" et al . ", " et al. ");
		substitutionPatterns.put(" etc . ", " etc. ");
		substitutionPatterns.put(" et . al . ", " et. al. ");
		substitutionPatterns.put(" mr . ", " mr. ");
		substitutionPatterns.put(" mrs . ", " mrs. ");
		substitutionPatterns.put(" ms . ", " ms. ");
		substitutionPatterns.put(" Mr . ", " Mr. ");
		substitutionPatterns.put(" Mrs . ", " Mrs. ");
		substitutionPatterns.put(" Ms . ", " Ms. ");
		
		String tokenised = stringBuilder.toString();
		for(Entry<String,String> e : substitutionPatterns.entrySet())
			tokenised = tokenised.replace(e.getKey(), e.getValue());
		
		
		return tokenised.trim();
	}
	
	/**
	 * Take tokenised text and split it into separate sentences.
	 * @param 	tokenisedText	Tokenised text.
	 * @return					ArrayList of sentences.
	 */
	public static ArrayList<String> sentenceSplit(String tokenisedText){
		ArrayList<String> sentences = new ArrayList<String>();
		
		String[] tokens = tokenisedText.trim().split("\\s+");
		String token;
		String sentence = "";
		
		// Iterate over all tokens and separate them into sentences
		for(int i = 0; i < tokens.length; i++){
			token = tokens[i];
			sentence += token + " ";
			
			// If the token is '?', '!' or '.', start a new sentence
			if(token.equals("?") || token.equals("!") || token.equals(".")){
				sentences.add(sentence.trim());
				sentence = "";
			}
		}
		if(sentence.length() > 0){
			sentences.add(sentence.trim());
		}
		
		return sentences;
	}
	
	/**
	 * First tokenise, then sentence-split the text.
	 * @param 	text	Input text.
	 * @return			Tokenised and split sentences.
	 */
	public static ArrayList<String> tokeniseAndSplit(String text){
		return sentenceSplit(tokenise(text));
	}
	
	private static String readFile(String path) throws IOException {
		  FileInputStream stream = new FileInputStream(new File(path));
		  try {
		    FileChannel fc = stream.getChannel();
		    MappedByteBuffer bb = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
		    /* Instead of using default, pass in a decoder. */
		    return Charset.defaultCharset().decode(bb).toString();
		  }
		  finally {
		    stream.close();
		  }
		}

	
	public static void main(String[] args){
		try {
			String input = readFile("examples/plaintext/file2.txt");
			input = input.replace("\"", "");
			FileWriter fw = new FileWriter("examples/plaintext/file2_tok.txt");
			for(String s : tokeniseAndSplit(input)){
				fw.writeln(s);
			}
			fw.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
}
