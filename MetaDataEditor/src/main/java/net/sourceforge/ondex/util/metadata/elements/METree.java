package net.sourceforge.ondex.util.metadata.elements;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import javax.swing.DropMode;
import javax.swing.Icon;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTree;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeCellRenderer;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.util.metadata.model.MetaDataType;

/**
 * 
 * @author Craig Wood
 *
 */
public class METree extends JTree {
     /**
	 * 
	 */
	private static final long serialVersionUID = 8921498045717220253L;

 	 private METree() {
    	 super();
    	 init();
     }
     
     private METree(TreeNode n) {
    	 super(n);
    	 init();
     }
     
     private void init() {
    	 setDragEnabled(true);
         setDropMode(DropMode.ON_OR_INSERT);
         TreeTransferHandler h = new TreeTransferHandler();
         setTransferHandler(h);
         getSelectionModel().setSelectionMode(
                 TreeSelectionModel.CONTIGUOUS_TREE_SELECTION);
         expandTree();
         setSelectionRow(0);
         DefaultTreeCellRenderer r = ((DefaultTreeCellRenderer)getCellRenderer());
         Icon icon = r.getLeafIcon();
         r.setOpenIcon(icon);
         r.setClosedIcon(icon);
     }
     
     public static METree forMetaData(ONDEXGraphMetaData md, MetaDataType mdt) {
    	 TreeNode root = null;
    	 switch (mdt) {
    	 case CONCEPT_CLASS:
    		 root = buildTreeForConceptClasses(md);
    		 break;
    	 case RELATION_TYPE:
    		 root = buildTreeForRelationTypes(md);
    		 break;
    	 case ATTRIBUTE_NAME:
    		 root = buildTreeForAttributeNames(md);
    		 break;
    	 }
    	 return new METree(root);
     }
     
     public void updateAll() {
    	 DefaultMutableTreeNode root = (DefaultMutableTreeNode) getModel().getRoot();
    	 TreePath p = new TreePath(root.getPath());
    	 getModel().valueForPathChanged(p, root.getUserObject());
    	 updateChildren(root);
     }
     
     private void updateChildren(DefaultMutableTreeNode node) {
    	 if (node.getChildCount() > 0) {
	    	 DefaultMutableTreeNode child = (DefaultMutableTreeNode) node.getFirstChild();
	    	 while (child != null) {
	        	 TreePath p = new TreePath(child.getPath());
	        	 getModel().valueForPathChanged(p, child.getUserObject());
	    		 updateChildren(child);
	    		 child = (DefaultMutableTreeNode) node.getChildAfter(child);
	    	 }
    	 }
     }
     
     private static TreeNode buildTreeForConceptClasses(ONDEXGraphMetaData md) {
    	 Map<String,DefaultMutableTreeNode> map = new HashMap<String,DefaultMutableTreeNode>();
    	 Iterator<ConceptClass> ccs = md.getConceptClasses().iterator();
    	 while (ccs.hasNext()) {
    		 ConceptClass cc = ccs.next();
    		 DefaultMutableTreeNode node = new DefaultMutableTreeNode(cc);
    		 map.put(cc.getId(),node);
    	 }    	 
    	 
    	 for (DefaultMutableTreeNode node : map.values()) {
    		 ConceptClass cc = (ConceptClass)node.getUserObject();
    		 if (!cc.getId().equals("Thing")) {
	    		 ConceptClass cc_parent = cc.getSpecialisationOf();
	    		 String id_parent = (cc_parent == null) ? "Thing" : cc_parent.getId();
	    		 DefaultMutableTreeNode parent = map.get(id_parent);
				 if (parent != null) {
					 parent.add(node);
				 } else {
					 System.err.println("Undefined ConceptClass: "+id_parent);
				 }
    		 }
    	 }
    	 return map.get("Thing");
     }
     
     private static TreeNode buildTreeForAttributeNames(ONDEXGraphMetaData md) {
    	 Map<String,DefaultMutableTreeNode> map = new HashMap<String,DefaultMutableTreeNode>();
    	 Iterator<AttributeName> ans = md.getAttributeNames().iterator();
    	 while (ans.hasNext()) {
    		 AttributeName an = ans.next();
    		 DefaultMutableTreeNode node = new DefaultMutableTreeNode(an);
    		 map.put(an.getId(),node);
    	 }
    	 
    	 for (DefaultMutableTreeNode node : map.values()) {
    		 AttributeName an = (AttributeName)node.getUserObject();
    		 if (!an.getId().equals("Attribute")) {
    			 AttributeName an_parent = an.getSpecialisationOf();
    			 String id_parent = (an_parent == null) ? "Attribute" : an_parent.getId();
    			 DefaultMutableTreeNode parent = map.get(id_parent);
    			 if (parent != null) {
    				 parent.add(node);
    			 } else {
    				 System.err.println("Undefined AttributeName: "+id_parent);
    			 }
    		 }
    	 }
    	 return map.get("Attribute");
     }
     
     
     private static TreeNode buildTreeForRelationTypes(ONDEXGraphMetaData md) {
    	 Map<String,DefaultMutableTreeNode> map = new HashMap<String,DefaultMutableTreeNode>();
    	 Iterator<RelationType> rts = md.getRelationTypes().iterator();
    	 while (rts.hasNext()) {
    		 RelationType rt = rts.next();
    		 DefaultMutableTreeNode node = new DefaultMutableTreeNode(rt);
    		 map.put(rt.getId(),node);
    	 }

    	 DefaultMutableTreeNode root = map.get("r");
    	 for (DefaultMutableTreeNode node : map.values()) {
    		 RelationType rt = (RelationType)node.getUserObject();
    		 if (!rt.getId().equals("r")) {
    			 RelationType rt_parent = rt.getSpecialisationOf();
    			 String id_parent = (rt_parent == null) ? "r" : rt_parent.getId();
    			 DefaultMutableTreeNode parent = map.get(id_parent);
    			 if (parent != null) {
    				 parent.add(node);
    			 } else {
    				 System.err.println("Undefined RelationType: "+id_parent);
    			 }
    		 }
    	 }
    	 return root;
     }
     
  
     private void expandTree() {
         DefaultMutableTreeNode root =
             (DefaultMutableTreeNode)getModel().getRoot();
         Enumeration<?> e = root.breadthFirstEnumeration();
         while(e.hasMoreElements()) {
             DefaultMutableTreeNode node =
                 (DefaultMutableTreeNode)e.nextElement();
             if(node.isLeaf()) continue;
             int row = getRowForPath(new TreePath(node.getPath()));
             expandRow(row);
         }
     }
  
     public static void main(String[] args) {
         JFrame f = new JFrame();
         f.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         METree tree = new METree();
         tree.getModel().addTreeModelListener(new TreeModelListener() {

			@Override
			public void treeNodesChanged(TreeModelEvent e) {
				System.out.println("node changed");
			}

			@Override
			public void treeNodesInserted(TreeModelEvent e) {
				System.out.println("node inserted:");
				for (Object o : e.getPath()) {
					System.out.println(o);
				}
				for (Object o : e.getChildren()) {
					System.out.println("--> "+o);
				}
			}

			@Override
			public void treeNodesRemoved(TreeModelEvent e) {
				System.out.println("node removed:");
				for (Object o : e.getPath()) {
					System.out.println(o);
				}
				for (Object o : e.getChildren()) {
					System.out.println("--> "+o);
				}
			}

			@Override
			public void treeStructureChanged(TreeModelEvent e) {
				System.out.println("structure changed");
			}
        	 
         });
         f.add(new JScrollPane(tree));
         f.setSize(400,400);
         f.setLocation(200,200);
         f.setVisible(true);
     }
     
     
 }