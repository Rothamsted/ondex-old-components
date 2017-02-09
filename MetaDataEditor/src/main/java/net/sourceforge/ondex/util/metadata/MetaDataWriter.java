package net.sourceforge.ondex.util.metadata;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.MetaData;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;

public class MetaDataWriter implements Monitorable{
	
	private File file;
	
	private BufferedWriter w;
	
	private ONDEXGraphMetaData md;
	
	private int maxProgress, progress;
	
	private String state;
	
	private Throwable caught;
	
	private int indent = 0;
	
	public MetaDataWriter(File file, ONDEXGraphMetaData md) {
		this.file = file;
		this.md = md;
		maxProgress = getNum(md.getConceptClasses()) +
					getNum(md.getDataSources()) +
					getNum(md.getAttributeNames()) +
					getNum(md.getEvidenceTypes()) +
					getNum(md.getRelationTypes()) +
					getNum(md.getUnits())+ 100;
		progress = 0;
		state = "Writing file...";
	}
	
	private int getNum(Set<?> set) {
		return set.size();
	}
	
	public void write() throws Exception {
		w = new BufferedWriter(new FileWriter(file));
		w.write("<?xml version=\"1.0\" encoding=\"UTF-8\" ?>\n " +
				"<ondexdata xmlns:ondex=\"http://ondex.sourceforge.net/\" "+
				"xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" "+
				"xsi:schemaLocation=\"http://ondex.sourceforge.net/ ondex.xsd\">\n" +
				"  <version>1.3</version>\n"
		);
		indent++;
		writeOndexmetadata();
		indent--;
		w.write("</ondexdata>");
		w.flush();
		w.close();
		
		progress = maxProgress;
		state = Monitorable.STATE_TERMINAL;
	}

	private void writeOndexmetadata() throws Exception {
		wrtln("<ondexmetadata>");
		indent++;
		writeCVs();
		writeUnits();
		writeAttributeNames();
		writeEvidences();
		writeConceptClasses();
		writeRelationTypes();
		indent--;
		wrtln("</ondexmetadata>");
	}
	
	private void writeCVs() throws Exception {
		wrtln("<cvs>");
		indent++;
		Iterator<DataSource> cvs = md.getDataSources().iterator();
		while (cvs.hasNext()) {
			DataSource dataSource = cvs.next();
			writeDataSource(dataSource);
			progress++;
		}
		indent--;
		wrtln("</cvs>");
	}
	
	private void writeDataSource(DataSource dataSource) throws Exception {
		wrtln("<cv>");
		indent++;
		writeMetaDataContent(dataSource);
		indent--;
		wrtln("</cv>");
	}
	
	private void writeMetaDataContent(MetaData m) throws Exception{
		wrtln("<id>"+m.getId()+"</id>");
		
		wrtln("<fullname>"+m.getFullname()+"</fullname>");
		
		wrtln("<description>"+m.getDescription()+"</description>");
	}
	
	private void writeMetaDataContent(AttributeName an) throws Exception{
		wrtln("<id>"+an.getId()+"</id>");
		
		wrtln("<fullname>"+an.getFullname()+"</fullname>");
		
		wrtln("<description>"+an.getDescription()+"</description>");
		
		// optional unit
		if (an.getUnit() != null) {
			wrtln("<unit>");
			indent++;
			writeMetaDataContent(an.getUnit());
			indent--;
			wrtln("</unit>");
		}
		
		wrtln("<datatype>"+an.getDataTypeAsString()+"</datatype>");
	}


        private void writeIdRef(MetaData md) throws Exception {
                wrtln("<idRef>"+md.getId()+"</idRef>");
        }
	
	private void writeUnits() throws Exception {
		wrtln("<units>");
		indent++;
		Iterator<Unit> us = md.getUnits().iterator();
		while (us.hasNext()) {
			Unit u = us.next();
			writeUnit(u);
			progress++;
		}
		indent--;
		wrtln("</units>");
	}
	
	private void writeUnit(Unit u) throws Exception {
		wrtln("<unit>");
		indent++;
		writeMetaDataContent(u);
		indent--;
		wrtln("</unit>");
	}
	
	private void writeAttributeNames() throws Exception {
		wrtln("<attrnames>");
		indent++;
		Iterator<AttributeName> us = md.getAttributeNames().iterator();
		while (us.hasNext()) {
			AttributeName u = us.next();
			writeAttributeName(u);
			progress++;
		}
		indent--;
		wrtln("</attrnames>");
	}
	
	private void writeAttributeName(AttributeName u) throws Exception {
		wrtln("<attrname>");
		indent++;
		writeMetaDataContent(u);
		
		// optional unit
		if (u.getUnit() != null) {
			wrtln("<unit>");
			indent++;
			writeMetaDataContent(u.getUnit());
			indent--;
			wrtln("</unit>");
		}
		
		wrtln("<datatype>"+u.getDataTypeAsString()+"</datatype>");
		
		// optional specialisation
		if (u.getSpecialisationOf() != null) {
			wrtln("<specialisationOf>");
			indent++;
//			writeMetaDataContent(u.getSpecialisationOf());
                        writeIdRef(u.getSpecialisationOf());
			indent--;
			wrtln("</specialisationOf>");
		}
		indent--;
		wrtln("</attrname>");
	}
	
	
	private void writeEvidences() throws Exception {
		wrtln("<evidences>");
		indent++;
		Iterator<EvidenceType> us = md.getEvidenceTypes().iterator();
		while (us.hasNext()) {
			EvidenceType u = us.next();
			writeEvidence(u);
			progress++;
		}
		indent--;
		wrtln("</evidences>");
	}
	
	private void writeEvidence(EvidenceType u) throws Exception {
		wrtln("<evidence>");
		indent++;
		writeMetaDataContent(u);
		indent--;
		wrtln("</evidence>");
	}
	
	
	private void writeConceptClasses() throws Exception {
		wrtln("<conceptclasses>");
		indent++;
		Iterator<ConceptClass> us = md.getConceptClasses().iterator();
		while (us.hasNext()) {
			ConceptClass u = us.next();
			writeConceptClass(u);
			progress++;
		}
		indent--;
		wrtln("</conceptclasses>");
	}
	
	private void writeConceptClass(ConceptClass u) throws Exception {
		wrtln("<cc>");
		indent++;
		writeMetaDataContent(u);
		if (u.getSpecialisationOf() != null) {
			wrtln("<specialisationOf>");
			indent++;
//			writeMetaDataContent(u.getSpecialisationOf());
                        writeIdRef(u.getSpecialisationOf());
			indent--;
			wrtln("</specialisationOf>");
		}
		indent--;
		wrtln("</cc>");
	}
	
	
	private void writeRelationTypes() throws Exception {
		wrtln("<relationtypes>");
		indent++;
		Iterator<RelationType> us = md.getRelationTypes().iterator();
		while (us.hasNext()) {
			RelationType u = us.next();
			writeRelationType(u);
			progress++;
		}
		indent--;
		wrtln("</relationtypes>");
	}
	
	private void writeRelationType(RelationType u) throws Exception {
		wrtln("<relation_type>");
		indent++;
		writeMetaDataContent(u);
		writeRelationTypeAtts(u);
		if (u.getSpecialisationOf() != null) {
			wrtln("<specialisationOf>");
			indent++;
//			writeMetaDataContent(u.getSpecialisationOf());
//			writeRelationTypeAtts(u.getSpecialisationOf());
                        writeIdRef(u.getSpecialisationOf());
			indent--;
			wrtln("</specialisationOf>");
		}
		indent--;
		wrtln("</relation_type>");
	}
	
	private void writeRelationTypeAtts(RelationType r) throws Exception {
		wrtln("<inverseName>"+clean(r.getInverseName())+"</inverseName>");
		
		wrtln("<isAntisymmetric>"+r.isAntisymmetric()+"</isAntisymmetric>");
		
		wrtln("<isReflexive>"+r.isReflexive()+"</isReflexive>");
		
		wrtln("<isSymmetric>"+r.isSymmetric()+"</isSymmetric>");
		
		wrtln("<isTransitive>"+r.isTransitiv()+"</isTransitive>");
	}
	
	private String clean(String s) {
		if (s == null || s.trim().equals("")) {
			return "none";
		} else {
			return s.trim();
		}
	}
	
	private void wrtln(String s) throws Exception {
		StringBuilder sb = new StringBuilder();
		for (int i = 0; i < indent; i++) {
			sb.append("  ");
		}
		w.write(sb.toString() + s + "\n");
	}
	
//	private void wrt(String s) throws Exception {
//		StringBuilder sb = new StringBuilder();
//		for (int i = 0; i < indent; i++) {
//			sb.append("  ");
//		}
//		w.write(sb.toString() + s);
//	}

	@Override
	public int getMaxProgress() {
		return maxProgress;
	}

	@Override
	public int getMinProgress() {
		return 0;
	}

	@Override
	public int getProgress() {
		return progress;
	}

	@Override
	public String getState() {
		return state;
	}

	@Override
	public Throwable getUncaughtException() {
		return caught;
	}

	@Override
	public boolean isAbortable() {
		return false;
	}

	@Override
	public boolean isIndeterminate() {
		return false;
	}

	@Override
	public void setCancelled(boolean c) {
		//do nothing
	}
}
