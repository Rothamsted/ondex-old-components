/*
 * OndexView plug-in for Cytoscape
 * Copyright (C) 2010  University of Newcastle upon Tyne
 * 
 * This file is part of OndexView.
 * 
 * OndexView is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 * 
 * OndexView is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public License
 * along with OndexView.  If not, see <http://www.gnu.org/licenses/>.
 */
package net.sourceforge.ondex.cytoscape.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

/**
 * button that shows a popup menu underneath when pressed.
 * @author jweile
 *
 */
public class PopupButton extends JButton {
	/**
	 * serial id.
	 */
	private static final long serialVersionUID = 1L;
	
	/**
	 * popup menu
	 */
	private JPopupMenu menu;
	
	/**
	 * constructor
	 * @param title
	 */
	public PopupButton(String title) {
		super(title);
		menu = new JPopupMenu();
		final PopupButton superthis = this;
		addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				int y_offset = getHeight();
				menu.show(superthis, 0, y_offset);
			}
		});
	}
	
	/**
	 * adds an entry to the popup menu
	 * @param a
	 */
	public void addAction(Action a) {
		menu.add(a);
	}
	
	/**
	 * adds an entry to the popup menu.
	 * @param title
	 * @param l
	 */
	public void addAction(String title, ActionListener l) {
		JMenuItem i = new JMenuItem(title);
		i.addActionListener(l);
		menu.add(i);
	}
}
