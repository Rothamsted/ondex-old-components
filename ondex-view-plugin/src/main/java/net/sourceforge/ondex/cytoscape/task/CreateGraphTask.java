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

import java.awt.EventQueue;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;

import javax.swing.JOptionPane;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ConceptName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.cytoscape.OndexPlugin;
import net.sourceforge.ondex.cytoscape.mapping.EdgeMappingDescriptor;
import net.sourceforge.ondex.cytoscape.mapping.MDIndex;
import net.sourceforge.ondex.cytoscape.mapping.PathSearcher;
import net.sourceforge.ondex.cytoscape.mapping.PathSearcher.Path;
import net.sourceforge.ondex.cytoscape.ui.CreateViewDialog;
import cytoscape.CyEdge;
import cytoscape.CyNode;
import cytoscape.Cytoscape;
import cytoscape.data.Semantics;
import cytoscape.task.Task;
import cytoscape.task.TaskMonitor;

/**
 * worker thread for creating a new graph.
 * @author jweile
 *
 */
public class CreateGraphTask implements Task {
	
	public static final String ARG_DATASOURCES = "data sources";
	public static final String ARG_EVIDENCES = "evidence";
	public static final String ARG_XREFS = "XRefs";
	public static final String ARG_GDSS = "concept attribute";
	public static final String ARG_NAMES = "names";
	public static final String ARG_DESC = "description";
	public static final String ARG_ANNO = "annotation";
	public static final String ARG_PATH = "path attributes";
	public static final String ARG_PATH_NUM = "number of paths";
	
	private TaskMonitor m;
	
	/**
	 * arrays for node and edge ids.
	 */
	private int[] nodes, edges;
	
	
	/**
	 * maps concept ids to cytoscape nodes
	 */
	private Map<Integer, CyNode> cid2node = new HashMap<Integer, CyNode>();
	
	/**
	 * set of active mappings.
	 */
	private Collection<EdgeMappingDescriptor> mappings;
	
	//####CONSTRUCTOR####
	/**
	 * constructor
	 * @param mappings
	 */
	public CreateGraphTask(Collection<EdgeMappingDescriptor> mappings) {
		this.mappings = mappings;
	}
	
	
	//#####METHODS#####
	
	/**
	 * returns the name of this thread.
	 */
    @Override
	public String getTitle() {
		return "Creating network";
	}

	/**
	 * not implemented
	 */
    @Override
	public void halt() {
	}
	
	/**
	 * singleton method
	 * @param cc
	 * @param c
	 * @return
	 */
	public boolean instanceOf(ConceptClass cc, ONDEXConcept c) {
		ConceptClass cc_query = c.getOfType();
		while (!cc_query.equals(cc)) {
			if (cc_query.getSpecialisationOf() == null) {
				return false;
			}
			cc_query = cc_query.getSpecialisationOf();
		}
		return true;
	}
	
	/**
	 * searches all equivalent concepts to the given one.
	 * @param c
	 * @return
	 */
	public Collection<ONDEXConcept> getSetOfEquals(ONDEXConcept c) {
		ONDEXGraph og = OndexPlugin.getInstance().getOndexGraph();
		RelationType equ = og.getMetaData().getRelationType("equals");
		ArrayList<ONDEXConcept> list = new ArrayList<ONDEXConcept>();
		for (ONDEXRelation r : og.getRelationsOfConcept(c)) {
			if (r.getOfType().equals(equ)) {
				ONDEXConcept equivalent = r.getFromConcept().equals(c) ? r.getToConcept() : r.getFromConcept();
				list.add(equivalent);
			}
		}
		list.add(c);
		return list;
	}
	
	/**
	 * starts thread.
	 */
    @Override
	public void run() {
		m.setPercentCompleted(0);
		
		m.setStatus("Extracting nodes...");
		mapNodes();
		m.setPercentCompleted(33);
		
		m.setStatus("Extracting edges...");
		mapEdges();
		m.setPercentCompleted(90);
		
		m.setStatus("Creating network...");
                try {
                    Cytoscape.createNetwork(nodes, edges, compileNetworkName(), null, false);
                    Cytoscape.firePropertyChange(Cytoscape.ATTRIBUTES_CHANGED, null, null);

                    m.setStatus("Done!");
                    m.setPercentCompleted(100);
                } catch (Exception e) {
                    JOptionPane.showMessageDialog(Cytoscape.getDesktop(),
                            "An error occurred during network creation:\n"+e.getLocalizedMessage(),
                            "Error", JOptionPane.ERROR_MESSAGE);
                }
		
		
		Runnable r = new Runnable() {
			@Override
			public void run() {
				try {
					new CreateViewDialog();
				} catch (Throwable t) {
					t.printStackTrace();
				}
			}
		};
		EventQueue.invokeLater(r);
	}
	
	/**
	 * creates a name for the network
	 * @return
	 */
	private String compileNetworkName() {
		String type = mappings.iterator().next().getNodeType();
		StringBuilder b = new StringBuilder(type+": ");
		for (EdgeMappingDescriptor d : mappings) {
			b.append(d.getName()+" ");
		}
		return b.toString();
	}

	/**
	 * performs mapping for the edges.
	 */
	private void mapEdges() {
		ONDEXGraph og = OndexPlugin.getInstance().getOndexGraph();
		ONDEXGraphMetaData md = og.getMetaData();
		
		AttributeName anDataSources = md.getAttributeName("DataSources");
		
		//edge mapping
		MDIndex mdindex = MDIndex.getInstance();
		
		List<Integer> edgeIdVec = new ArrayList<Integer>();
//		ONDEXGraph og = OndexPlugin.getInstance().getOndexGraph();
//		ONDEXGraphMetaData md = og.getMetaData();
		for (EdgeMappingDescriptor currentDescriptor : mappings) {
			String[] ccSs = currentDescriptor.getConceptClasses(),
					 rtSs = currentDescriptor.getRelationTypes();
			ConceptClass[] ccs = new ConceptClass[ccSs.length];
			for (int i = 0; i < ccs.length; i++) {
//				ccs[i] = md.getConceptClass(ccSs[i]);
				ccs[i] = mdindex.resolveConceptClass(ccSs[i]);
			}
			RelationType[] rts = new RelationType[rtSs.length];
			for (int i = 0; i < rts.length; i++) {
//				rts[i] = md.getRelationType(rtSs[i]);
				rts[i] = mdindex.resolveRelationType(rtSs[i]);
			}
			
			Map<Association,List<Path>> asso2path = new HashMap<Association, List<Path>>();
			
			PathSearcher searcher = new PathSearcher(ccs,rts);
			searcher.search();
			Path path;
			while ((path = searcher.nextPath()) != null) {
				CyNode from = cid2node.get(path.head().getId());
				CyNode to = cid2node.get(path.tail().getId());
				
				if (from != null && to != null) {
					Association asso = new Association(from.getIdentifier(),to.getIdentifier());
					
					List<Path> paths = asso2path.get(asso);
					if (paths == null) {
						paths = new ArrayList<Path>();
						asso2path.put(asso, paths);
					}
					paths.add(path);
				} else {
					System.err.println("nodes could not be retrieved...");
				}
			}
						
			for (Association asso : asso2path.keySet()) {
				CyNode from = Cytoscape.getCyNode(asso.getFromID(), false);
				CyNode to = Cytoscape.getCyNode(asso.getToID(), false);
				CyEdge edge = null;
				
				if (from != null && to != null) {
					edge = Cytoscape.getCyEdge(from, to, Semantics.INTERACTION, currentDescriptor.getName(), true);
					edgeIdVec.add(edge.getRootGraphIndex());
				} else {
					System.err.println("nodes could not be retrieved...");
				}
				
				if (edge == null) {
					continue;
				}
				
				StringBuilder b = new StringBuilder();
				List<Path> paths = asso2path.get(asso);
				for (Path p : paths) {
					ONDEXEntity[] elements = p.getElements();
					for (int i=0; i < elements.length; i++) {
						if (i%2==0) {
							ONDEXConcept c = (ONDEXConcept) elements[i];
							String cname = extractName(c);
							for (Attribute attribute : c.getAttributes()) {
								if (attribute.getOfType().equals(anDataSources)) {
									continue;
								}
								b.append(c.getOfType()+":"+cname+"."+attribute.getOfType().getId()+"="+attribute.getValue()+"\n");
							}
						} else {
							ONDEXRelation r = (ONDEXRelation) elements[i];
							for (Attribute attribute : r.getAttributes()) {
								b.append(r.getOfType()+"."+attribute.getOfType().getId()+"="+attribute.getValue()+"\n");
							}
						}
					}
					b.append("\n\n");
				}
				Cytoscape.getEdgeAttributes().setAttribute(edge.getIdentifier(), ARG_PATH, b.toString());
				Cytoscape.getEdgeAttributes().setAttribute(edge.getIdentifier(), ARG_PATH_NUM, paths.size());
			}
		}
		edges = new int[edgeIdVec.size()];
		for (int i = 0; i < edgeIdVec.size(); i++) {
			edges[i] = edgeIdVec.get(i);
		}
	}

	/**
	 * gets a name for the given concept.
	 * @param c
	 * @return
	 */
	private String extractName(ONDEXConcept c) {
		if (c.getConceptName() != null) {
			return c.getConceptName().getName();
		} else {
			return c.getId()+"";
		}
	}

	/**
	 * performs mapping for nodes.
	 */
	private void mapNodes() {
		ONDEXGraph og = OndexPlugin.getInstance().getOndexGraph();
		ONDEXGraphMetaData md = og.getMetaData();
		
		AttributeName anDataSources = md.getAttributeName("DataSources");
		DataSource dataSourceUnknown = md.getDataSource("unknown");
		
		List<Integer> nodeIdVec = new ArrayList<Integer>();
		String nodeType = mappings.iterator().next().getNodeType();
		ConceptClass cc = md.getConceptClass(nodeType);
		Set<Integer> antiDup = new HashSet<Integer>();
		for (ONDEXConcept c_proto : og.getConcepts()) {
			if (antiDup.contains(c_proto.getId())) {
				continue;
			}
			if (instanceOf(cc,c_proto)) {
				Collection<ONDEXConcept> eqs = getSetOfEquals(c_proto);
				for (ONDEXConcept eq : eqs) {//register dups
					antiDup.add(eq.getId());
				}
				
				String id = complileId(cc, eqs);
				CyNode node = Cytoscape.getCyNode(id, false);
				if (node == null) {//then create
					node = Cytoscape.getCyNode(id, true);

					//==extract relevant data from concept set==
					TreeSet<String> datasources = new TreeSet<String>();//to ensure alphabetical order
					TreeSet<String> evidences = new TreeSet<String>();
					HashMap<String,TreeSet<String>> dbid2xrefs = new HashMap<String, TreeSet<String>>();
					HashMap<String,TreeSet<String>> an2gds = new HashMap<String, TreeSet<String>>();
					TreeSet<String> names = new TreeSet<String>();
					TreeSet<String> descriptions = new TreeSet<String>();
					TreeSet<String> annotations = new TreeSet<String>();
					for (ONDEXConcept c : eqs) {
						//datasources
						if (c.getElementOf().equals(dataSourceUnknown)) {
							Attribute dsAttribute = c.getAttribute(anDataSources);
							if (dsAttribute != null) {
								List<?> dss = (List<?>) dsAttribute.getValue();
								for (Object o : dss) {
									datasources.add(o.toString());
								}
							} else {
								datasources.add(dataSourceUnknown.toString());
							}
						} else {
							datasources.add(c.getElementOf().toString());
						}
						//evidences
						for (EvidenceType e : c.getEvidence())  {
							evidences.add(e.getId());
						}
						//xrefs
						for (ConceptAccession acc : c.getConceptAccessions()) {
							String type = acc.getElementOf().getId();
							TreeSet<String> xrefList = dbid2xrefs.get(type);
							if (xrefList == null) {
								xrefList = new TreeSet<String>();
								dbid2xrefs.put(type,xrefList);
							}
							String xref = acc.getAccession();
							xrefList.add(xref);
						}
						//gdss
						for (Attribute attribute : c.getAttributes())  {
							String an = attribute.getOfType().getId();
							if (an.equals(anDataSources.getId())) {
								continue;
							}
							TreeSet<String> valList = an2gds.get(an);
							if (valList == null) {
								valList = new TreeSet<String>();
								an2gds.put(an, valList);
							}
							valList.add(attribute.getValue().toString());
						}
						//names
						for (ConceptName name : c.getConceptNames()) {
							if (name.isPreferred()) {
								names.add(name.getName());
							}
						}
						//descriptions
						if (c.getDescription() != null && !c.getDescription().trim().equals("")) {
							descriptions.add(c.getDescription());
						}
						//annotations
						if (c.getAnnotation() != null && !c.getAnnotation().trim().equals("")) {
							annotations.add(c.getAnnotation());
						}
					}
					
					//==add extracted data as node attributes==
					//add datasources
					if (datasources.size() == 1) {
						Cytoscape.getNodeAttributes().setAttribute(id, 
								ARG_DATASOURCES, datasources.iterator().next());
					} else if (datasources.size() > 1) {
						Cytoscape.getNodeAttributes().setAttribute(id, 
								ARG_DATASOURCES, datasources.toString());//see AbstractCollection.toString()
					}
					//add evidences
					if (evidences.size() == 1) {
						Cytoscape.getNodeAttributes().setAttribute(id, 
								ARG_EVIDENCES, evidences.iterator().next());
					} else if (evidences.size() > 1) {
						Cytoscape.getNodeAttributes().setAttribute(id, 
								ARG_EVIDENCES, evidences.toString());
					}
					//add xrefs
					for (String type : dbid2xrefs.keySet()) {
						Collection<String> xrefs = dbid2xrefs.get(type);
						if (xrefs.size() == 1) {
							Cytoscape.getNodeAttributes().setAttribute(id, 
									ARG_XREFS+":"+type, xrefs.iterator().next());
						} else if (xrefs.size() > 1) {
							Cytoscape.getNodeAttributes().setAttribute(id, 
									ARG_XREFS+":"+type, xrefs.toString());
						}
					}
					//add gdss
					for (String an : an2gds.keySet()) {
						Collection<String> vals = an2gds.get(an);
						if (vals.size() == 1) {
							Cytoscape.getNodeAttributes().setAttribute(id,
									ARG_GDSS+":"+an, vals.iterator().next());
						} else if (vals.size() > 1) {
							Cytoscape.getNodeAttributes().setAttribute(id,
									ARG_GDSS+":"+an, vals.toString());
						}
					}
					//add names
					if (names.size() == 1) {
						Cytoscape.getNodeAttributes().setAttribute(id,
								ARG_NAMES, names.iterator().next());
					} else if (names.size() > 1) {
						Cytoscape.getNodeAttributes().setAttribute(id,
								ARG_NAMES, names.toString());
					}
					
					//add descriptions
					if (descriptions.size() == 1) {
						Cytoscape.getNodeAttributes().setAttribute(id, 
								ARG_DESC, descriptions.iterator().next());
					} else if (names.size() > 1) {
						Cytoscape.getNodeAttributes().setAttribute(id,
								ARG_DESC, descriptions.toString());
					}
					
					//add annotations
					if (annotations.size() == 1) {
						Cytoscape.getNodeAttributes().setAttribute(id, 
								ARG_ANNO, annotations.iterator().next());
					} else if (annotations.size() > 1) {
						Cytoscape.getNodeAttributes().setAttribute(id,
								ARG_ANNO, annotations.toString());
					}
				}
				for (ONDEXConcept eq : eqs) {//make hashmap entry for all equivalents
					cid2node.put(eq.getId(),node);
				}
				nodeIdVec.add(node.getRootGraphIndex());
			}
		}
		nodes = new int[nodeIdVec.size()];
		for (int i = 0; i < nodeIdVec.size(); i++) {
			nodes[i] = nodeIdVec.get(i);
		}
	}

	/**
	 * CyNode ID consisting of the concept class id, a hash character,
	 * and a colon-separated, ascending list of concept ids.
	 * 
	 * Example:
	 * 
	 * Gene#1:3:45
	 * @param cc
	 * @param eqs
	 * @return
	 */
	private String complileId(ConceptClass cc, Collection<ONDEXConcept> eqs) {
		TreeSet<Integer> sortedSet = new TreeSet<Integer>();
		for (ONDEXConcept c : eqs) {
			sortedSet.add(c.getId());
		}
		StringBuilder b = new StringBuilder(cc.getId()+"#");
		for (int i : sortedSet) {
			b.append(i+":");
		}
		b.deleteCharAt(b.length()-1);
		return b.toString();
	}

	/**
	 * returns monitor.
	 */
    @Override
	public void setTaskMonitor(TaskMonitor m)
			throws IllegalThreadStateException {
		this.m = m;
	}
	
	/**
	 * helper class for caching edges to be created.
	 * @author jweile
	 *
	 */
	private class Association {
		private String fromID, toID;
		
		public Association(String from, String to) {
			fromID = from;
			toID = to;
		}

		public String getFromID() {
			return fromID;
		}

		public String getToID() {
			return toID;
		}
		
        @Override
		public int hashCode() {
			return new String(fromID+"-"+toID).hashCode();
		}
		
        @Override
		public boolean equals(Object o) {
			if (o instanceof Association) {
				Association a = (Association) o;
				if (a.getFromID().equals(fromID) && a.getToID().equals(toID)) {
					return true;
				}
			}
			return false;
		}
	}

}
