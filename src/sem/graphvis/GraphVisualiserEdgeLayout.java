package sem.graphvis;

import java.awt.geom.Rectangle2D;
import java.util.Iterator;

import prefuse.action.layout.Layout;
import prefuse.visual.DecoratorItem;
import prefuse.visual.VisualItem;

public class GraphVisualiserEdgeLayout extends Layout {
	
	public GraphVisualiserEdgeLayout(String group) {
	    super(group);
	}
	
	public void run(double frac) {
	    Iterator iter = m_vis.items(m_group);
	    while ( iter.hasNext() ) {
		DecoratorItem decorator = (DecoratorItem)iter.next();
		VisualItem decoratedItem = decorator.getDecoratedItem();

		if (decoratedItem.isVisible()) {
		    
		    Rectangle2D bounds = decoratedItem.getBounds();

		    double x = bounds.getCenterX();
		    double y = bounds.getCenterY();

		    setX(decorator, null, x);
		    setY(decorator, null, y);
		} 
		decorator.setVisible(decoratedItem.isVisible());
	    }
	}
}
