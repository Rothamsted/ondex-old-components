package net.sourceforge.ondex.modules.integration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.core.*;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.init.Initialisation;
import net.sourceforge.ondex.init.PluginRegistry;

import java.io.File;

/**
 * Unit test for simple App.
 */
public class MetaDataPresentTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public MetaDataPresentTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( MetaDataPresentTest.class );
    }

    private ONDEXGraphMetaData constructMetaData() throws Exception
    {
        PluginRegistry.init(true, new String[] { Config.pluginDir});

        File metadata = new File(Config.ondexDir + File.separator + "xml" + File.separator + "ondex_metadata.xml");
        File xsd = new File(Config.ondexDir + File.separator + "xml" + File.separator + "ondex.xsd");
        ONDEXGraph g = new MemoryONDEXGraph("mdTest");
        Initialisation init = new Initialisation(metadata, xsd);
        init.initMetaData(g);
        return g.getMetaData();
    }

    /**
     * Trigger PLUGINS-46 regression by searching for key meta-data items.
     *
     * @throws Exception if anything goes wrong initialising the metadata
     */
    public void testSpecificMetadataPresent() throws Exception
    {
        ONDEXGraphMetaData md = constructMetaData();

        DataSource dataSource = md.getDataSource("unknown");
        assertNotNull("DataSource for 'unknown' must not be null", dataSource);

        Unit unit = md.getUnit("second");
        assertNotNull("DataSource for 'second' must not be null", unit);

        AttributeName attributeName = md.getAttributeName("GDS");
        assertNotNull("AttributeName for 'GDS' must not be null", attributeName);

        EvidenceType evidenceType = md.getEvidenceType("IMPD");
        assertNotNull("EvidenceType for 'IMPD' must not be null", evidenceType);

        ConceptClass conceptClass = md.getConceptClass("Thing");
        assertNotNull("ConceptClass for 'Thing' must not be null", conceptClass);

        RelationType relationType = md.getRelationType("r");
        assertNotNull("RelationType for 'R' must not be null", relationType);
    }

    /**
     * Trigger PLUGINS-46 regression by seeing if any meta-data items are present.
     *
     * @throws Exception if anything goes wrong initialising the metadata
     */
    public void testAnyMetadataPresent() throws Exception
    {
        ONDEXGraphMetaData md = constructMetaData();

        assertFalse("DataSources must be present", md.getDataSources().isEmpty());
        assertFalse("Units must be present", md.getUnits().isEmpty());
        assertFalse("AttributeNames must be present", md.getAttributeNames().isEmpty());
        assertFalse("EvidenceTYpes must be present", md.getEvidenceTypes().isEmpty());
        assertFalse("ConceptClasses must be present", md.getConceptClasses().isEmpty());
        assertFalse("RelationTypes must be present", md.getRelationTypes().isEmpty());
    }
}
