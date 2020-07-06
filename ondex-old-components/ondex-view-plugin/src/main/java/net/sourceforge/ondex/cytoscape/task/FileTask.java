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

import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

/**
 * Abstract task for processing files
 * @author jweile
 *
 */
public abstract class FileTask implements Task {

	/**
	 * whether the task is currently running.
	 */
	private boolean running = false;
	
	/**
	 * whether the user wishes to abort.
	 */
	protected boolean abort = false;
	
	/**
	 * monitor.
	 */
	protected TaskMonitor monitor;
	
	/**
	 * name of the file operation.
	 */
	private String title;
	
	/**
	 * potential output of this file operation
	 */
	protected Object output;
	
	/**
	 * potential exception resulting from execution of this task
	 */
	protected Exception exception;

	//####CONSTRUCTOR####
	
	/**
	 * constructor
	 */
	public FileTask(String title) {
		this.title = title;
	}
	
	/**
	 * gets the title
	 */
	public String getTitle() {
		return title;
	}

	/**
	 * stops this file operation
	 */
	public void halt() {
		abort = true;
	}

	/**
	 * runs this file operation
	 */
	public void run() {
		prepare();
		
		processFile();
		
		done();
	}
	
	/**
	 * gets the output
	 * @return
	 */
	public Object getOutput() {
		return output;
	}
	
	/**
	 * gets thrown exception if any
	 * @return
	 */
	public Exception getException() {
		return exception;
	}
	
	/**
	 * processes the given file
	 */
	public abstract void processFile();
	
	/**
	 * prepares for file processing
	 */
	private void prepare() {
		running = true;
		if (monitor == null) {
			throw new RuntimeException("No task monitor set!");
		}
		monitor.setStatus("preparing...");
	}
	
	/**
	 * finalizes this task
	 */
	private void done() {
		running = false;
		monitor.setStatus("done");
		monitor.setEstimatedTimeRemaining(0);
		monitor.setPercentCompleted(100);
	}

	/**
	 * sets the monitor.
	 */
	public void setTaskMonitor(TaskMonitor m)
			throws IllegalThreadStateException {
		if (!running) {
			monitor = m;
		} else {
			throw new IllegalThreadStateException("Cannot reset monitor while running!");
		}
	}

}
