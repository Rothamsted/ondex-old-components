package net.sourceforge.ondex.util.metadata.elements;

import java.awt.datatransfer.DataFlavor;
import java.awt.datatransfer.Transferable;
import java.awt.datatransfer.UnsupportedFlavorException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Vector;

import javax.swing.JComponent;
import javax.swing.JTree;
import javax.swing.TransferHandler;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.util.metadata.MetaDataEditor;
import net.sourceforge.ondex.util.metadata.model.OldNode;
import net.sourceforge.ondex.util.metadata.ops.MoveOperation;

/**
 * 
 * @author Craig Wood
 * @author small modifications: Jochen Weile
 *
 */
public class TreeTransferHandler extends TransferHandler {
	
    /**
	 * serial id.
	 */
	private static final long serialVersionUID = -1681036095783584744L;
	
	private DataFlavor nodesFlavor;
    
	private DataFlavor[] flavors = new DataFlavor[1];
    
	private DefaultMutableTreeNode[] nodesToRemove;
	
	private MoveOperation<MetaData> moveAction;
	
    public TreeTransferHandler() {
        try {
            String mimeType = DataFlavor.javaJVMLocalObjectMimeType +
                              ";class=\"" +
                javax.swing.tree.DefaultMutableTreeNode[].class.getName() +
                              "\"";
            nodesFlavor = new DataFlavor(mimeType);
            flavors[0] = nodesFlavor;
        } catch(ClassNotFoundException e) {
            System.out.println("ClassNotFound: " + e.getMessage());
        }
        
    }
    
    
 
    public boolean canImport(TransferHandler.TransferSupport support) {
        if(!support.isDrop()) {
            return false;
        }
        support.setShowDropLocation(true);
        if(!support.isDataFlavorSupported(nodesFlavor)) {
            return false;
        }
        // Do not allow a drop on the drag source selections.
        JTree.DropLocation dl =
                (JTree.DropLocation)support.getDropLocation();
        JTree tree = (JTree)support.getComponent();
        int dropRow = tree.getRowForPath(dl.getPath());
        int[] selRows = tree.getSelectionRows();
        for(int i = 0; i < selRows.length; i++) {
            if(selRows[i] == dropRow) {
                return false;
            }
        }
        // Do not allow MOVE-action drops if a non-leaf node is
        // selected unless all of its children are also selected.
//        int action = support.getDropAction();
//        if(action == MOVE) {
//            return haveCompleteNode(tree);
//        }
        // Do not allow a non-leaf node to be copied to a level
        // which is less than its source level.
//        TreePath dest = dl.getPath();
//        DefaultMutableTreeNode target =
//            (DefaultMutableTreeNode)dest.getLastPathComponent();
//        TreePath path = tree.getPathForRow(selRows[0]);
//        DefaultMutableTreeNode firstNode =
//            (DefaultMutableTreeNode)path.getLastPathComponent();
//        if(firstNode.getChildCount() > 0 &&
//               target.getLevel() < firstNode.getLevel()) {
//            return false;
//        }
        return true;
    }
 
//    private boolean haveCompleteNode(JTree tree) {
//        int[] selRows = tree.getSelectionRows();
//        TreePath path = tree.getPathForRow(selRows[0]);
//        DefaultMutableTreeNode first =
//            (DefaultMutableTreeNode)path.getLastPathComponent();
//        int childCount = first.getChildCount();
//        // first has children and no children are selected.
//        if(childCount > 0 && selRows.length == 1)
//            return false;
//        // first may have children.
//        for(int i = 1; i < selRows.length; i++) {
//            path = tree.getPathForRow(selRows[i]);
//            DefaultMutableTreeNode next =
//                (DefaultMutableTreeNode)path.getLastPathComponent();
//            if(first.isNodeChild(next)) {
//                // Found a child of first.
//                if(childCount > selRows.length-1) {
//                    // Not all children of first are selected.
//                    return false;
//                }
//            }
//        }
//        return true;
//    }
    
    private TreePath[] removeChildren(TreePath[] paths, DefaultTreeModel model) {
    	HashSet<TreePath> set = new HashSet<TreePath>();
    	for (TreePath path : paths) {
    		set.add(path);
    	}
    	for (TreePath path : paths) {
    		DefaultMutableTreeNode node = (DefaultMutableTreeNode)path.getLastPathComponent();
    		if (containsAParent(set, node, model)) {
    			set.remove(node);
    		}
    	}
    	return set.toArray(new TreePath[set.size()]);
    }
    
    private boolean containsAParent(HashSet<TreePath> set, DefaultMutableTreeNode node, DefaultTreeModel model) {
    	if (node.getParent() == null) {
    		return false;
    	} else {
    		DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
    		if (set.contains(new TreePath(model.getPathToRoot(parent)))) {
    			return true;
    		} else {
    			return containsAParent(set, parent, model);
    		}
    	}
    }
 
    protected Transferable createTransferable(JComponent c) {
        JTree tree = (JTree)c;
        TreePath[] paths = tree.getSelectionPaths();
        if(paths != null) {
        	paths = removeChildren(paths, (DefaultTreeModel)tree.getModel());
        
            // Make up a node array of copies for transfer and
            // another for/of the nodes that will be removed in
            // exportDone after a successful drop.
            List<DefaultMutableTreeNode> copies =
                new ArrayList<DefaultMutableTreeNode>();
            List<DefaultMutableTreeNode> toRemove =
                new ArrayList<DefaultMutableTreeNode>();
            DefaultMutableTreeNode node =
                (DefaultMutableTreeNode)paths[0].getLastPathComponent();
            DefaultMutableTreeNode copy = copy(node);
            copies.add(copy);
            toRemove.add(node);
            for(int i = 1; i < paths.length; i++) {
                DefaultMutableTreeNode next =
                    (DefaultMutableTreeNode)paths[i].getLastPathComponent();
                // Do not allow higher level nodes to be added to list.
                if(next.getLevel() < node.getLevel()) {
                    break;
                } else if(next.getLevel() > node.getLevel()) {  // child node
                    copy.add(copy(next));
                    // node already contains child
                } else {                                        // sibling
                    copies.add(copy(next));
                    toRemove.add(next);
                }
            }
            DefaultMutableTreeNode[] nodes =
                copies.toArray(new DefaultMutableTreeNode[copies.size()]);
            nodesToRemove =
                toRemove.toArray(new DefaultMutableTreeNode[toRemove.size()]);
            System.out.println("Transferable created");
            return new NodesTransferable(nodes, nodesToRemove);
        }
        return null;
    }
 
    /** Defensive copy used in createTransferable. */
    private DefaultMutableTreeNode copy(TreeNode node) {
        return new DefaultMutableTreeNode(node);
    }
 
    protected void exportDone(JComponent source, Transferable data, int action) {
    	System.out.println("Export done called");
//        if((action & MOVE) == MOVE) {
//            JTree tree = (JTree)source;
//            DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
//            // Remove nodes saved in nodesToRemove in createTransferable.
//            for(int i = 0; i < nodesToRemove.length; i++) {
//                model.removeNodeFromParent(nodesToRemove[i]);
//            }
//        }
    }
 
    public int getSourceActions(JComponent c) {
        return MOVE;
    }
 
    public boolean importData(TransferHandler.TransferSupport support) {
    	System.out.println("importData called");
        if(!canImport(support)) {
        	System.out.println("Cannot import!");
            return false;
        }
        // Extract transfer data.
        DefaultMutableTreeNode[]  oldNodes = null;
        try {
        	Transferable t = support.getTransferable();
        	NodesTransferable nt = (NodesTransferable) t.getTransferData(nodesFlavor);
//            nodes = nt.getNewNodes();
            oldNodes = nt.getOldNodes();
        } catch(UnsupportedFlavorException ufe) {
            System.out.println("UnsupportedFlavor: " + ufe.getMessage());
        } catch(java.io.IOException ioe) {
            System.out.println("I/O error: " + ioe.getMessage());
        }
        
        //get source location info
        Vector<OldNode> oldNodeVec = new Vector<OldNode>();
        for (DefaultMutableTreeNode oldNode : oldNodes) {
        	OldNode o = new OldNode(oldNode);
        	oldNodeVec.add(o);
        }
        
        // Get drop location info.
        JTree.DropLocation dl =
                (JTree.DropLocation)support.getDropLocation();
        int childIndex = dl.getChildIndex();
        TreePath dest = dl.getPath();
        DefaultMutableTreeNode parent =
            (DefaultMutableTreeNode)dest.getLastPathComponent();
        METree tree = (METree)support.getComponent();
//        DefaultTreeModel model = (DefaultTreeModel)tree.getModel();
        // Configure for drop mode.
        int index = childIndex;    // DropMode.INSERT
        if(childIndex == -1) {     // DropMode.ON
            index = parent.getChildCount();
        }
        // Add data to model.
//        for(int i = 0; i < nodes.length; i++) {
//            model.insertNodeInto(nodes[i], parent, index++);
//            DefaultMutableTreeNode container = (DefaultMutableTreeNode) nodes[i].getUserObject();
//            nodes[i].setUserObject(container.getUserObject());
//            adaptMetaDataHierarchy(nodes[i], parent);
//        }
        System.out.println("firing move action");
        moveAction = new MoveOperation<MetaData>(tree, 
        			oldNodeVec.toArray(new OldNode[oldNodeVec.size()]),
        			parent, 
        			index);//, 
//        			nodes);

    	MetaDataEditor.getInstance().getCurrentMetaDataWindow().performOperation(moveAction);
        return true;
    }
    
//    private void adaptMetaDataHierarchy(DefaultMutableTreeNode node, DefaultMutableTreeNode parent) {
//    	MetaData md = (MetaData) node.getUserObject();
//    	switch (MetaDataType.fromClass(md)) {
//    	case CONCEPT_CLASS:
//    		ConceptClass cc_curr = (ConceptClass) md;
//    		ConceptClass cc_parent = (ConceptClass) parent.getUserObject();
//    		cc_curr.setSpecialisationOf(cc_parent);
//    		System.out.println("new parent: "+cc_curr.getSpecialisationOf());
//    		break;
//    	case RELATION_TYPE: 
//    		RelationType rt_curr = (RelationType) md;
//    		RelationType rt_parent = (RelationType) parent.getUserObject();
//    		rt_curr.setSpecialisationOf(rt_parent);
//    		break;
//    	}
//    }
 
    public String toString() {
        return getClass().getName();
    }
 
    public class NodesTransferable implements Transferable {
        DefaultMutableTreeNode[] newNodes, oldNodes;
 
        public NodesTransferable(DefaultMutableTreeNode[] newNodes, DefaultMutableTreeNode[] oldNodes) {
            this.newNodes = newNodes;
            this.oldNodes = oldNodes;
         }
 
        public Object getTransferData(DataFlavor flavor)
                                 throws UnsupportedFlavorException {
            if(!isDataFlavorSupported(flavor))
                throw new UnsupportedFlavorException(flavor);
            return this;
        }
        
        public DefaultMutableTreeNode[] getOldNodes() {
        	return oldNodes;
        }
        
        public DefaultMutableTreeNode[] getNewNodes() {
        	return newNodes;
        }
 
        public DataFlavor[] getTransferDataFlavors() {
            return flavors;
        }
 
        public boolean isDataFlavorSupported(DataFlavor flavor) {
            return nodesFlavor.equals(flavor);
        }
    }
}
