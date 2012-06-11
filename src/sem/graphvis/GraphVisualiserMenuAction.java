package sem.graphvis;

import java.awt.event.ActionEvent;

import javax.swing.AbstractAction;
import javax.swing.KeyStroke;

public class GraphVisualiserMenuAction extends AbstractAction{
	String accel;
	GraphVisualiser graphVisualiser;

	public GraphVisualiserMenuAction(String name, String accel, GraphVisualiser graphVisualiser) {
		this.accel = accel;
		this.graphVisualiser = graphVisualiser;
		this.putValue(AbstractAction.NAME, name);
		this.putValue(AbstractAction.ACCELERATOR_KEY, KeyStroke.getKeyStroke(accel));
	}

	public void actionPerformed(ActionEvent e) {
		if(graphVisualiser.commandArea == null || !graphVisualiser.commandArea.isFocusOwner()){
			if (accel.equalsIgnoreCase("A"))
				this.graphVisualiser.prevGraph();
			else if (accel.equalsIgnoreCase("S"))
				this.graphVisualiser.nextGraph();
			else if (accel.equalsIgnoreCase("Z"))
				this.graphVisualiser.prevSentence();
			else if(accel.equalsIgnoreCase("X"))
				this.graphVisualiser.nextSentence();
		}
	}
}
