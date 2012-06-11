package sem.test.tokeniser;

import static org.junit.Assert.assertTrue;

import java.util.ArrayList;

import org.junit.Test;

import sem.tokeniser.Tokeniser;

public class TokeniserTest {

	@Test
	public void test() {
		String text = "Recent studies have shown that TDP-43 can accumulate in RNA stress granules (SGs) in response to cell stresses and this could be associated with subsequent formation of TDP-43 ubiquinated protein aggregates. However, the initial mechanisms controlling endogenous TDP-43 accumulation in SGs during chronic disease are not understood.";
		
		String sentence1 = "Recent studies have shown that TDP-43 can accumulate in RNA stress granules ( SGs ) in response to cell stresses and this could be associated with subsequent formation of TDP-43 ubiquinated protein aggregates .";
		String sentence2 = "However , the initial mechanisms controlling endogenous TDP-43 accumulation in SGs during chronic disease are not understood .";
		
		ArrayList<String> sentences = Tokeniser.tokeniseAndSplit(text);
		assertTrue(sentences.size() == 2);
		assertTrue(sentences.get(0).equals(sentence1));
		assertTrue(sentences.get(1).equals(sentence2));
	}

}
