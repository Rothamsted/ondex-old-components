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
package net.sourceforge.ondex.cytoscape.mapping;

import java.util.ArrayList;

/**
 * Describes a path through the metagraph that defines a cytoscape edge.
 * @author jweile
 *
 */
public class EdgeMappingDescriptor implements Comparable<EdgeMappingDescriptor> {

	/**
	 * name of the definition.
	 */
	private String name;
	
	/**
	 * text form of path descriptor. interchanging list of concept classes and relation types.
	 */
	private String path;
	
	/**
	 * concept class and relation type arrays parsed from the <code>path<code>.
	 */
	private String[] ccs, rts;
	
	/**
	 * unique id for this descriptor.
	 */
	private String id;
	
	/**
	 * constructor
	 * @param name
	 * @param path
	 */
	public EdgeMappingDescriptor(String name, String path) {
		this.name = name;
		this.path = path;
		parsePath(path);
	}
	
	/**
	 * gets the name.
	 * @return
	 */
	public String getName() {
		return name;
	}
	
	/**
	 * sets the name.
	 * @param n
	 */
	public void setName(String n) {
		name = n;
	}
	
	/**
	 * constructs the unique id, consisting of the central concept class and the descriptor name
	 * delimited by a colon. i.e: Protein:regulation.
	 * @return
	 */
	public String getId() {
		if (id == null) {
			if (ccs != null && ccs.length > 0) {
				id = ccs[0]+":"+name;
			} else {
				id = ":"+name;
			}
		}
		return id;
	}

	/**
	 * gets the array of concept classes in the descriptor
	 * @return array of strings containing the ids of concept classes.
	 */
	public String[] getConceptClasses() {
		return ccs;
	}

	/**
	 * gets the array of relation types in the descriptor
	 * @return array of strings containing the ids of relation types.
	 */
	public String[] getRelationTypes() {
		return rts;
	}
	
	/**
	 * parses a given path descriptor into concept class and relation type arrays.
	 * @param s
	 */
	private void parsePath(String s) {
		String[] fields = s.split(" ");
		ArrayList<String> ccVec = new ArrayList<String>(), 
						  rtVec = new ArrayList<String>();
		for (int i = 0; i < fields.length; i++) {
			boolean cc = (i % 2) == 0;
			if (cc) {
				ccVec.add(fields[i]);
			} else {
				rtVec.add(fields[i]);
			}
		}
		ccs = ccVec.toArray(new String[ccVec.size()]);
		rts = rtVec.toArray(new String[rtVec.size()]);
	}

	/**
	 * throws exception if current path is syntactically incorrect.
	 * @param og
	 * @return
	 */
	public void validate() throws MalformedPathException {
		MDIndex mdindex = MDIndex.getInstance();
		
		if (ccs.length != rts.length + 1) {
			throw new MalformedPathException("No valid path.");
		}
		if (!ccs[0].equals(ccs[ccs.length-1])) {
			throw new MalformedPathException("Path is not circular.");
		}
		StringBuilder b = new StringBuilder("");
		for (String cc : ccs) {
			if (mdindex.resolveConceptClass(cc) == null) {
				b.append(cc+" ");
			}
		}
		for (String rt : rts) {
			if (mdindex.resolveRelationType(rt) == null) {
				b.append(rt+" ");
			}
		}
		String unmatched = b.toString().trim();
		if (!unmatched.equals("")) {
			throw new MalformedPathException("Unmatched: "+unmatched);
		}
	}
	
	/**
	 * checks if this descriptor is equal to another descriptor, based on the unique id.
	 */
	@Override
	public boolean equals(Object o) {
		if (o instanceof EdgeMappingDescriptor) {
			EdgeMappingDescriptor e = (EdgeMappingDescriptor) o;
			if (e.getId().equals(id)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * alphanumerical comparison based on id.
	 */
	public int compareTo(EdgeMappingDescriptor e) {
		return getId().compareTo(e.getId());
	}

	/**
	 * returns the central concept class of this descriptor.
	 * @return
	 */
	public String getNodeType() {
		return ccs[0];
	}

	/**
	 * returns the path in string form.
	 * @return
	 */
	public String getPathString() {
//		StringBuilder b = new StringBuilder("");
//		for (int i = 0; i < rts.length; i++) {
//			b.append(ccs[i]+" "+rts[i]+" ");
//		}
//		b.append(ccs[ccs.length-1]);
//		return b.toString().trim();
		return path;
	}
}
