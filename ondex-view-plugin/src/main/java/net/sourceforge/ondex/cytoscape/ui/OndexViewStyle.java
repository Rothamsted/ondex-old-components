/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.ondex.cytoscape.ui;

import cytoscape.Cytoscape;
import cytoscape.view.CyNetworkView;
import cytoscape.visual.EdgeAppearanceCalculator;
import cytoscape.visual.GlobalAppearanceCalculator;
import cytoscape.visual.NodeAppearance;
import cytoscape.visual.NodeAppearanceCalculator;
import cytoscape.visual.NodeShape;
import cytoscape.visual.VisualMappingManager;
import cytoscape.visual.VisualPropertyType;
import cytoscape.visual.VisualStyle;
import cytoscape.visual.calculators.BasicCalculator;
import cytoscape.visual.calculators.Calculator;
import cytoscape.visual.mappings.DiscreteMapping;
import cytoscape.visual.mappings.ObjectMapping;
import cytoscape.visual.mappings.PassThroughMapping;
import java.awt.Color;
import java.util.HashSet;
import java.util.Set;

/**
 *
 * This class creates a visualisation style for a view.
 * It assigns a unique color to each type of edge.
 *
 * @author jweile
 */
public class OndexViewStyle {

    /**
     * The view to which the style will be applied
     */
    private CyNetworkView view;

    /**
     * the name of the attribute carrying the associatinon type information.
     */
    private static final String INTERACTION_AN = "interaction";

    /**
     * Prefix for style names
     */
    private static final String ID_PREFIX = "OndexView";
    /**
     * Id part of the last style name. Style name = prefix + id
     */
    private static int LAST_ID = 0;

    /**
     * Constructor over a network view.
     * @param view
     */
    public OndexViewStyle(CyNetworkView view) {
        this.view = view;
        setup();
    }

    /**
     * creates the style objects
     */
    private void setup() {
        //define node label mapping
        PassThroughMapping nodeLabelMapping = new PassThroughMapping("", "names");
        Calculator nodeLabelCalc = new BasicCalculator("Names as labels", nodeLabelMapping, VisualPropertyType.NODE_LABEL);

        //define edge color mapping
        DiscreteMapping edgeColorMapping =
                new DiscreteMapping(Color.blue,ObjectMapping.EDGE_MAPPING);
        edgeColorMapping.setControllingAttributeName(INTERACTION_AN, view.getNetwork(), false);
        Set<String> itypes = getInteractionTypes();
        ColorIterator colors = new ColorIterator(itypes.size());
        for (String itype : itypes) {
            edgeColorMapping.putMapValue(itype, colors.nextColor());
        }
        Calculator edgeColorCalc = new BasicCalculator("Edge color by type",
                edgeColorMapping, VisualPropertyType.EDGE_COLOR);

        //define standard node appearance
        NodeAppearance defaultNodeAppearance = new NodeAppearance();
        defaultNodeAppearance.set(VisualPropertyType.NODE_SHAPE, NodeShape.ELLIPSE);
        defaultNodeAppearance.set(VisualPropertyType.NODE_FILL_COLOR, Color.LIGHT_GRAY);
        defaultNodeAppearance.set(VisualPropertyType.NODE_LINE_WIDTH, 2.0);
        defaultNodeAppearance.set(VisualPropertyType.NODE_BORDER_COLOR, Color.GRAY);


        //create style and apply definitions
        VisualStyle vs = new VisualStyle(ID_PREFIX + (++LAST_ID));

        NodeAppearanceCalculator nac = vs.getNodeAppearanceCalculator();
        nac.setCalculator(nodeLabelCalc);
        nac.setDefaultAppearance(defaultNodeAppearance);

        EdgeAppearanceCalculator eac = vs.getEdgeAppearanceCalculator();
        eac.setCalculator(edgeColorCalc);

        GlobalAppearanceCalculator gac = vs.getGlobalAppearanceCalculator();

        //apply
        VisualMappingManager manager = Cytoscape.getVisualMappingManager();
        if (manager.getCalculatorCatalog().getVisualStyle(vs.getName()) != null) {
            manager.getCalculatorCatalog().removeVisualStyle(vs.getName());
        }
        manager.getCalculatorCatalog().addVisualStyle(vs);
        view.setVisualStyle(vs.getName());
        manager.setVisualStyle(vs);
        view.redrawGraph(true, true);
    }

    /**
     * Extracts the set of all usedassociation types.
     * @return
     */
    private Set<String> getInteractionTypes() {
        Set<String> interactionTypes = new HashSet<String>();

        int[] edgeIndices = view.getNetwork().getEdgeIndicesArray();
        for (int i = 0; i < edgeIndices.length; i++) {
            String edgeId = view.getNetwork().getEdge(edgeIndices[i]).getIdentifier();
            String itype = (String) Cytoscape.getEdgeAttributes().getAttribute(edgeId, INTERACTION_AN);
            interactionTypes.add(itype);
        }

        return interactionTypes;
    }

    /**
     * Little helper class that can be used to iterate over a set of n unique colors.
     */
    private class ColorIterator {

        /**
         * array holding the colors
         */
        private Color[] colors;
        
        /**
         * points at the next color to iterate over.
         */
        private int __nextcolor = 0;

        /**
         * constructor given the number of colors you need.
         * @param numColors the number of unique colors you want.
         */
        public ColorIterator(int numColors) {
            colors = new Color[numColors];

            float interval = 1.0f / (float)numColors;
            float currHue = 0.0f;
            for (int i= 0; i < numColors; i++) {
                colors[i] = new Color(Color.HSBtoRGB(currHue, .9f, .7f));
                currHue += interval;
            }
        }

        /**
         * returns the next color
         * @return
         */
        public Color nextColor() {
            return colors[__nextcolor++];
        }

    }
    
}
