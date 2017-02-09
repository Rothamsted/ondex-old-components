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
package net.sourceforge.ondex.cytoscape;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.List;

import javax.swing.JOptionPane;
import javax.swing.SwingConstants;

import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.cytoscape.ui.AttributeMappingDialog;
import net.sourceforge.ondex.cytoscape.ui.OndexMappingPanel;
import cytoscape.Cytoscape;
import cytoscape.plugin.CytoscapePlugin;

/**
 * Main plugin class
 *
 * @author Matthew Pocock, Jochen Weile
 */
public class OndexPlugin extends CytoscapePlugin {
	
	/**
	 * singleton
	 */
	private static OndexPlugin instance;
	
	/**
	 * currently open ondex graph
	 */
	private ONDEXGraph og;

	/**
	 * currently used graph file.
	 */
	private File graphFile;

	/**
	 * constructor.
	 */
	public OndexPlugin() {
		activate();
	}
	
	/**
	 * not used
	 */
    @Override
    public boolean isScriptable() {
        return false;
    }
    

    /**
     * starts the plugin
     */
    @Override
    public void activate() {
    	instance = this;
    	
    	ensureConfigFilePresence();
    	
        Cytoscape.getDesktop().getCytoPanel(SwingConstants.WEST).add("Ondex",new OndexMappingPanel());
        
        AttributeMappingDialog.registerInMenuBar();
    }

    /**
     * not yet implemented
     */
	public void restoreSessionState(List<File> pStateFileList) {
		//TODO
	}

	/**
	 * not yet implemented
	 */
	public void saveSessionStateFiles(List<File> pFileList) {
		//TODO
	}

	/**
	 * not yet implemented
	 */
    @Override
    public void deactivate() {
    	//TODO low priority as functionality not implemented in Cytoscape anyway.
    }
    
    /**
     * singleton method
     * @return
     */
    public static OndexPlugin getInstance() {
    	return instance;
    }
    
    /**
     * gets the current ondex graph
     * @return
     */
	public ONDEXGraph getOndexGraph() {
		return og;
	}

	/**
	 * sets the current ondex graph
	 * @param og
	 */
	public void setOndexGraph(ONDEXGraph og) {
		this.og = og;
	}
	
	/**
	 * closes the ondex graph
	 */
	public void closeOndexDataSet() {
		og = null;
	}

	/**
	 * gets the current ondex graph file
	 * @return
	 */
	public File getOndexGraphFile() {
		return graphFile;
	}
	
	/**
	 * sets the current ondex graph file.
	 * @param file
	 */
	public void setOndexGraphFile(File file) {
		graphFile = file;
	}
	
	/**
	 * checks if ondex config file is present. if not extracts it to required location.
	 */
	private void ensureConfigFilePresence() {
		File configTarget = new File("ondex_config"+File.separator+"config.xml");
		if (!configTarget.exists()) {
			configTarget.getParentFile().mkdirs();

			try {
                            InputStream in = getClass().getClassLoader().getResourceAsStream("config.xml");
                            OutputStream out = new FileOutputStream(configTarget);
                            byte[] buf = new byte[128]; int len;
                            while ((len = in.read(buf)) > -1) {
                                    out.write(buf, 0, len);
                            }
			} catch (IOException e) {
                            JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
                                            "Cannot access Ondex config file:\n"+configTarget.getAbsolutePath(),
                                            "Error", JOptionPane.ERROR_MESSAGE);
			}
		}
		
		System.setProperty("ondex.dir",configTarget.getParentFile().getAbsolutePath());
	}
	
}
