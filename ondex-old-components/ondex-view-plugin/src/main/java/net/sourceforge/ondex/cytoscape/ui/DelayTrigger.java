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
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.util.Vector;

/**
 * Daemon that triggers an action event after the user stops typing.
 * Can be used to plug action listeners into a keylistener interface.
 * @author jweile
 *
 */
public class DelayTrigger extends Thread implements KeyListener{
	
	/**
	 * timestamp of last keypress.
	 */
	private long lastChange = 0L;
	
	/**
	 * excitement state
	 */
	private boolean excited = false;
	
	/**
	 * termination trigger.
	 */
	private boolean terminate = false;
	
	/**
	 * currently registered listeners.
	 */
	private Vector<ActionListener> listeners = new Vector<ActionListener>();
	
	/**
	 * constructor.
	 */
	public DelayTrigger() {
		setDaemon(true);
		setPriority(1);
	}
	
	/**
	 * starts the daemon.
	 */
	public void run() {
		while (!terminate) {
			if (excited) {
				if (System.currentTimeMillis() - lastChange > 700) {
					fire();
					excited = false;
				}
			}
			try {
				Thread.sleep(300);
			} catch (InterruptedException e) {}
		}
	}
	
	/**
	 * terminates the daemon.
	 */
	public void terminate() {
		terminate = true;
	}
	
	/**
	 * registers another actionlistener
	 * @param l
	 */
	public void addActionListener(ActionListener l) {
		listeners.add(l);
	}
	
	/**
	 * simulate keypress
	 */
	public void hitSensor() {
		lastChange = System.currentTimeMillis();
		excited = true;
	}
	
	/**
	 * fires an action event to the listeners
	 */
	private void fire() {
		for (ActionListener l: listeners) {
			l.actionPerformed(new ActionEvent(this,0,"KeyWatcher"));
		}
	}

	/**
	 * not used
	 */
	public void keyPressed(KeyEvent e) {}

	/**
	 * not used.
	 */
	public void keyReleased(KeyEvent e) {}

	/**
	 * triggered when key is typed.
	 */
	public void keyTyped(KeyEvent e) {
		hitSensor();
	}

}
