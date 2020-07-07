package net.sourceforge.ondex.util.metadata.elements;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.table.AbstractTableModel;

import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.util.metadata.model.MetaDataType;

public class MEList<M extends MetaData> extends JTable {

	/**
	 * 
	 */
	private static final long serialVersionUID = -3996606388160464065L;

	public MEList(MetaDataType mdt, ONDEXGraphMetaData omd, String name) {
		setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
		setModel(new MDListModel<M>(mdt, omd, name));
		setAutoCreateRowSorter(true);
	}

	@SuppressWarnings("unchecked")
	public MDListModel<M> getMDListModel() {
		return (MDListModel<M>) getModel();
	}

	@SuppressWarnings("hiding")
	public class MDListModel<M extends MetaData> extends AbstractTableModel {

		/**
		 * 
		 */
		private static final long serialVersionUID = 9025794139261054964L;

		private ArrayList<M> list;

		private String name;

		private Comparator<M> nameComparator = new Comparator<M>() {
			@Override
			public int compare(M o1, M o2) {
				return o1.getFullname().compareTo(o2.getFullname());
			}
		};

		private Comparator<M> idComparator = new Comparator<M>() {
			@Override
			public int compare(M o1, M o2) {
				return o1.getId().compareTo(o2.getId());
			}
		};

		@SuppressWarnings("unchecked")
		public MDListModel(MetaDataType mdt, ONDEXGraphMetaData omd, String name) {
			this.name = name;
			list = new ArrayList<M>();
			Iterator<? extends MetaData> it = null;
			switch (mdt) {
			case CV:
				it = omd.getDataSources().iterator();
				break;
			case EVIDENCE_TYPE:
				it = omd.getEvidenceTypes().iterator();
				break;
			case UNIT:
				it = omd.getUnits().iterator();
				break;
			}
			if (it != null) {
				while (it.hasNext()) {
					M m = (M) it.next();
					list.add(m);
				}
				sortByName();
			}
		}

		public M getMetaDataAt(int index) {
			return list.get(index);
		}

		public void sortByName() {
			Collections.sort(list, nameComparator);
			this.fireTableDataChanged();
		}

		public void sortById() {
			Collections.sort(list, idComparator);
			this.fireTableDataChanged();
		}

		public void remove(int index) {
			list.remove(index);
			this.fireTableRowsDeleted(index, index);
		}

		public void replace(int index, M md) {
			list.remove(index);
			list.add(index, md);
			this.fireTableDataChanged();
		}

		public void add(M m, int index) {
			list.add(index, m);
			this.fireTableRowsInserted(index, index);
		}

		public void update(int index) {
			this.fireTableCellUpdated(index, 0);
		}

		public void update() {
			this.fireTableStructureChanged();
		}

		@Override
		public String getColumnName(int column) {
			return name;
		}

		@Override
		public int getRowCount() {
			return list.size();
		}

		@Override
		public int getColumnCount() {
			return 1;
		}

		@Override
		public Object getValueAt(int rowIndex, int columnIndex) {
			return list.get(rowIndex);
		}
	}

}
