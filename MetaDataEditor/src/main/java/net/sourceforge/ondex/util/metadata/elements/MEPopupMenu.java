package net.sourceforge.ondex.util.metadata.elements;

import java.awt.BorderLayout;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.SpringLayout;
import javax.swing.tree.DefaultMutableTreeNode;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.util.metadata.EditorPanel;
import net.sourceforge.ondex.util.metadata.MetaDataEditor;
import net.sourceforge.ondex.util.metadata.model.MetaDataType;
import net.sourceforge.ondex.util.metadata.ops.InsertOperation;
import net.sourceforge.ondex.util.metadata.ops.RemovalOperation;
import net.sourceforge.ondex.util.metadata.ops.RenameOperation;

public class MEPopupMenu<M extends MetaData> extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = 6670745297962771598L;
	
	private MetaDataType mdt;
	
	private METree tree;
	
	private MEList<M> list;
	
	private EditorPanel<M> editorPanel;
	
	private DefaultMutableTreeNode node;
	
	public MEPopupMenu(MetaDataType mdt, METree tree, DefaultMutableTreeNode node, EditorPanel<M> rtEditorPanel, boolean root) {
		this.mdt = mdt;
		this.tree = tree;
		this.node = node;
		this.editorPanel = rtEditorPanel;
		
		setupTreeMenu(root);
	}
	
	public MEPopupMenu(MetaDataType mdt, MEList<M> list, int index, EditorPanel<M> ep) {
		this.mdt = mdt;
		this.list = list;
		this.editorPanel = ep;
		
		setupListMenu(index);
	}
	
	private void setupListMenu(final int index) {
		JMenuItem item = new JMenuItem("Change ID");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				String newId = askUserForString();
				if (newId != null) {
					M md = (M)list.getMDListModel().getMetaDataAt(index);
					RenameOperation<M> a = new RenameOperation<M>(md, md.getId(), newId, list, index, editorPanel);
					MetaDataEditor.getInstance().getCurrentMetaDataWindow().performOperation(a);
				}
			}
		});
		item.setEnabled(index > -1);
		add(item);
		
		item = new JMenuItem("Add element");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				M md = askUser(null);
				if (md != null) {
					InsertOperation<M> a = new InsertOperation<M>(md, list, index+1);
					MetaDataEditor.getInstance().getCurrentMetaDataWindow().performOperation(a);
				}
			}
		});
		add(item);
		
		item = new JMenuItem("Remove");
		item.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				RemovalOperation<M> a = new RemovalOperation<M>((M)list.getMDListModel().getMetaDataAt(index), list, index);
				MetaDataEditor.getInstance().getCurrentMetaDataWindow().performOperation(a);
			}
		});
		item.setEnabled(index > -1);
		if (mdt == MetaDataType.UNIT) {
			Unit u = (Unit)list.getMDListModel().getMetaDataAt(index);
			Iterator<AttributeName> ans = MetaDataEditor.getInstance()
				.getCurrentMetaDataWindow().getMetaData().getAttributeNames().iterator();
			boolean found = false;
			while (ans.hasNext()) {
				AttributeName an = ans.next();
				if (u.equals(an.getUnit())) {
					found = true;
					break;
				}
			}
			if (found) {
				item.setEnabled(false);
				item.setToolTipText("Cannot be deleted: Used in Attribute name!");
			}
		}
		add(item);
	}
	
	private void setupTreeMenu(boolean root) {
		JMenuItem item = new JMenuItem("Change ID");
		item.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				String newId = askUserForString();
				if (newId != null) {
					M md = (M)node.getUserObject();
					RenameOperation<M> a = new RenameOperation<M>(md, md.getId(), newId, node, editorPanel);
					MetaDataEditor.getInstance().getCurrentMetaDataWindow().performOperation(a);
				}
			}
		});
		item.setEnabled(!root);
		add(item);
		
		item = new JMenuItem("Add subclass...");
		item.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				M md = askUser((M)node.getUserObject());
				if (md != null) {
					InsertOperation<M> a = new InsertOperation<M>(md, tree, node, 0);
					MetaDataEditor.getInstance().getCurrentMetaDataWindow().performOperation(a);
				}
			}
		});
		add(item);
		
		item = new JMenuItem("Add sibling...");
		item.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
				M md = askUser((M)parent.getUserObject());
				if (md != null) {
					int index = parent.getIndex(node) + 1;
					InsertOperation<M> a = new InsertOperation<M>(md, tree, parent, index);
					MetaDataEditor.getInstance().getCurrentMetaDataWindow().performOperation(a);
				}
			}
		});
		item.setEnabled(!root);
		add(item);
		
		item = new JMenuItem("Remove");
		item.addActionListener(new ActionListener() {
			@SuppressWarnings("unchecked")
			@Override
			public void actionPerformed(ActionEvent e) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) node.getParent();
				int index = parent.getIndex(node);
				RemovalOperation<M> a = new RemovalOperation<M>((M)node.getUserObject(),tree, node, index);
				MetaDataEditor.getInstance().getCurrentMetaDataWindow().performOperation(a);
			}
		});
		item.setEnabled(!root);
		add(item);
	}
	
	private String askUserForString() {
		String id = JOptionPane.showInputDialog("Please enter ID");
		if (id != null) {
			ONDEXGraphMetaData omd = MetaDataEditor.getInstance().getCurrentMetaDataWindow().getMetaData();
			switch (mdt) {
			case CONCEPT_CLASS:
				if (omd.checkConceptClass(id)) {
					complain();
					return null;
				}
				break;
			case RELATION_TYPE:
				if (omd.checkRelationType(id)) {
					complain();
					return null;
				}
				break;
			case CV:
				if (omd.checkDataSource(id)) {
					complain();
					return null;
				}
			case EVIDENCE_TYPE:
				if (omd.checkEvidenceType(id)) {
					complain();
					return null;
				}
				break;
			case UNIT:
				if (omd.checkUnit(id)) {
					complain();
					return null;
				}
				break;
			case ATTRIBUTE_NAME:
				if (omd.checkAttributeName(id)) {
					complain();
					return null;
				}
			}
			return id;
		} else {
			return null;
		}
	}
	
	@SuppressWarnings("unchecked")
	private M askUser(M parentMD) {
		String id = null; 
		Class<?> type = null;
		if (mdt == MetaDataType.ATTRIBUTE_NAME) {
			String[] retVal = MEInputDialog.showDialog(MetaDataEditor.getInstance());
			if (retVal != null && retVal.length == 2) {
				id = retVal[0];
				try {
					type = getClass().getClassLoader().loadClass(retVal[1]);
				} catch (ClassNotFoundException e) {
					JOptionPane.showMessageDialog(MetaDataEditor.getInstance(), 
							"Not a valid Java class!", "Error", JOptionPane.ERROR_MESSAGE);
					return null;
				}
			}
		} else {
			id = JOptionPane.showInputDialog("Please enter ID");
		}
		if (id != null) {
			ONDEXGraphMetaData omd = MetaDataEditor.getInstance().getCurrentMetaDataWindow().getMetaData();
			M md = null;
			switch (mdt) {
			case CONCEPT_CLASS:
				if (omd.checkConceptClass(id)) {
					complain();
					return null;
				}
				ConceptClass parentCC = (ConceptClass) parentMD;
				md = (M)omd.createConceptClass(id, id, "", parentCC);
				break;
			case RELATION_TYPE:
				if (omd.checkRelationType(id)) {
					complain();
					return null;
				}
				RelationType parentRT = (RelationType) parentMD;
				md = (M)omd.createRelationType(id, id, "", "", false, false, false, false, parentRT);
				break;
			case CV:
				if (omd.checkDataSource(id)) {
					complain();
					return null;
				}
				md = (M) omd.createDataSource(id, id, "");
				break;
			case EVIDENCE_TYPE:
				if (omd.checkEvidenceType(id)) {
					complain();
					return null;
				}
				md = (M) omd.createEvidenceType(id, id, "");
				break;
			case UNIT:
				if (omd.checkUnit(id)) {
					complain();
					return null;
				}
				md = (M) omd.createUnit(id, id, "");
				break;
			case ATTRIBUTE_NAME:
				if (omd.checkAttributeName(id)) {
					complain();
					return null;
				}
				md = (M) omd.createAttributeName(id, id, "", null, type, null);
				break;
			}
			return md;
		} else {
			return null;
		}
	}
	
	private void complain() {
		JOptionPane.showMessageDialog(MetaDataEditor.getInstance(), 
				"ID already in use! Try again.", 
				"Error", 
				JOptionPane.ERROR_MESSAGE);
	}
	
	private static class MEInputDialog extends JDialog {
		
		/**
		 * 
		 */
		private static final long serialVersionUID = -7325271505759125835L;

		private JTextField id, type;
		
		private boolean ok = false;
		
		private MEInputDialog(Frame parent) {
			super(parent,"Input",true);
			getContentPane().setLayout(new BorderLayout());
			createFormPanel();
			createButtonPanel();
			pack();
			
			Rectangle r = parent.getBounds();
			int x = r.x + (r.width - getSize().width)/2;
			int y = r.y + (r.height - getSize().height)/2;
			setLocation(x, y);
			
			setVisible(true);
		}
		
		private void createFormPanel() {
			JPanel p = new JPanel();
			p.setLayout(new SpringLayout());
			
			id = new JTextField(10);
			JLabel idl = new JLabel("ID", JLabel.TRAILING);
			idl.setLabelFor(id);
			p.add(idl);
			p.add(id);
			
			type = new JTextField(10);
			type.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ok = true;
					dispose();
				}
			});
			JLabel typel = new JLabel("Data type", JLabel.TRAILING);
			typel.setLabelFor(type);
			p.add(typel);
			p.add(type);
			
			SpringUtilities.makeCompactGrid(p, 2, 2, 6, 6, 6, 6);
			
			getContentPane().add(p, BorderLayout.CENTER);
		}
		
		private void createButtonPanel() {
			JPanel p = new JPanel();
			p.setLayout(new FlowLayout(FlowLayout.CENTER));
			
			JButton b = new JButton("OK");
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					ok = true;
					dispose();
				}
			});
			p.add(b);
			
			b = new JButton("Cancel");
			b.addActionListener(new ActionListener() {
				@Override
				public void actionPerformed(ActionEvent e) {
					dispose();
				}
			});
			p.add(b);
			
			getContentPane().add(p, BorderLayout.SOUTH);
		}
		
		public static String[] showDialog(Frame parent) {
			MEInputDialog d = new MEInputDialog(parent);
			if (d.ok) {
				return new String[]{d.id.getText(), d.type.getText()};
			} else {
				return new String[0];
			}
		}
	}
	
}
