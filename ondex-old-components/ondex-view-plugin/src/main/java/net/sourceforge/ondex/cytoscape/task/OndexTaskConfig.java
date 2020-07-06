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
package net.sourceforge.ondex.cytoscape.task;

import cytoscape.Cytoscape;
import cytoscape.task.ui.JTaskConfig;

/**
 * helper class for configuring tasks.
 * @author jweile
 *
 */
public class OndexTaskConfig extends JTaskConfig {
	/**
	 * singleton.
	 */
	private static OndexTaskConfig instance;
	/**
	 * singleton method.
	 * @return
	 */
	public static OndexTaskConfig getInstance() {
		if (instance == null) {
			instance = new OndexTaskConfig();
			instance.setOwner(Cytoscape.getDesktop());
			instance.displayCloseButton(false);
			instance.displayCancelButton(false);
			instance.displayStatus(true);
			instance.setAutoDispose(true);
		}
		return instance;
	}
}
