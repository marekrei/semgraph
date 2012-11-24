package sem.graph;
/**
 * A node class for the dependency graph. It has a field for lemma and part-of-speech.
 *
 */
public class Node {
	private String lemma;
	private String pos;
	
	/**
	 * Create a new node.
	 */
	public Node(){
		this.lemma = null;
		this.pos = null;
	}
	
	/**
	 * Create new node.
	 * @param	lemma	Lemma
	 * @param	pos		Part-of-speech
	 */
	public Node(String lemma, String pos) {
		this.lemma = lemma;
		this.pos = pos;
	}
	
	/**
	 * Get the lemma of this node.
	 * @return Lemma
	 */
	public String getLemma(){
		return this.lemma;
	}
	
	/**
	 * Get the POS of this node
	 * @return POS
	 */
	public String getPos(){
		return this.pos;
	}
	
	/**
	 * Get the combined representation of lemma and POS: lemma and POS joined by underscore (lemma_POS).
	 * @return A label for this node.
	 */
	public String getLabel() {
		return this.getLemma() + "_" + this.getPos();
	}
	
	/**
	 * Print the label to System.out.
	 */
	public void print(){
		System.out.println(this.toString());
	}
	
	/**
	 * Get the string representation of this node. Returns the label.
	 */
	@Override
	public String toString(){
		return this.getLabel();
	}
	
	/**
	 * Set the POS for the node.
	 * @param pos POS
	 */
	public void setPos(String pos){
		this.pos = pos;
	}
	
	/**
	 * Set the lemma for this node.
	 * @param lemma Lemma
	 */
	public void setLemma(String lemma){
		this.lemma = lemma;
	}
	
	/**
	 * Create a new independent node with the same lemma and POS.
	 */
	@Override
	public Node clone(){
		return new Node(this.getLemma(), this.getPos());
	}
}
