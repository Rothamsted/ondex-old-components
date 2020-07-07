package net.sourceforge.ondex.modules.integration;

import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import net.sourceforge.ondex.config.Config;
import net.sourceforge.ondex.init.PluginDescription;
import net.sourceforge.ondex.init.PluginRegistry;
import net.sourceforge.ondex.init.PluginType;
import net.sourceforge.ondex.workflow.engine.BasicJobImpl;
import net.sourceforge.ondex.workflow.engine.ResourcePool;
import net.sourceforge.ondex.workflow.model.BoundArgumentValue;
import net.sourceforge.ondex.workflow.model.WorkflowDescription;
import net.sourceforge.ondex.workflow.model.WorkflowTask;

import java.lang.String;

/**
 * Unit test for simple App.
 */
public class OndexLauncherAllTest
    extends TestCase
{
    /**
     * Create the test case
     *
     * @param testName name of the test case
     */
    public OndexLauncherAllTest( String testName )
    {
        super( testName );
    }

    /**
     * @return the suite of tests being tested
     */
    public static Test suite()
    {
        return new TestSuite( OndexLauncherAllTest.class );
    }

    public void testToOndexJob() throws Exception
    {
        PluginRegistry.init(true, new String[] { Config.pluginDir});
        PluginRegistry pr = PluginRegistry.getInstance();

        PluginDescription pb = pr.getPluginDescription(PluginType.PRODUCER, "memorygraph");
        assertNotNull("Plugin by the name of 'memorygraph' should be registered", pb);

        WorkflowTask conf = new WorkflowTask(pb, new BoundArgumentValue[0]); // fixme: varargs are not working for me
        final String graphId = "testgraph";
        conf.addArgument("GraphName", graphId);

        PluginDescription ob = pr.getPluginDescription(PluginType.PARSER, "oxl");
        assertNotNull("Plugin by the name of 'oxl' should be registered", ob);

        WorkflowTask oConf = new WorkflowTask(ob, new BoundArgumentValue[0]);
        oConf.addArgument("InputFile", "src/test/files/GrameneTraitOntology.xml.gz");
        oConf.addArgument("graphId", graphId);

        WorkflowDescription task = new WorkflowDescription();
        task.addPlugin(conf);
        task.addPlugin(oConf);

        BasicJobImpl job = new BasicJobImpl(new ResourcePool());
        task.toOndexJob(job);
        job.run();
    }

    public void testPoplarFilter() throws Exception
    {
        PluginRegistry.init(true, new String[] { Config.pluginDir});
        PluginRegistry pr = PluginRegistry.getInstance();

        PluginDescription pb = pr.getPluginDescription(PluginType.PRODUCER, "memorygraph");
        assertNotNull("Plugin by the name of 'memorygraph' should be registered", pb);

        WorkflowTask conf = new WorkflowTask(pb, new BoundArgumentValue[0]); // fixme: varargs are not working for me
        final String graphId = "testgraph";
        conf.addArgument("GraphName", graphId);

        PluginDescription ob = pr.getPluginDescription(PluginType.PARSER, "oxl");
        assertNotNull("Plugin by the name of 'oxl' should be registered", ob);

        WorkflowTask oConf = new WorkflowTask(ob, new BoundArgumentValue[0]);
        oConf.addArgument("InputFile", "src/test/files/candidates.xml.gz");
        oConf.addArgument("graphId", graphId);

        PluginDescription nb = pr.getPluginDescription(PluginType.FILTER, "nohits");
        assertNotNull("Plugin by the name of 'nohits' should be registered", nb);

        WorkflowTask pConf = new WorkflowTask(nb, new BoundArgumentValue[0]);
        pConf.addArgument("graphId", graphId);

        WorkflowDescription task = new WorkflowDescription();
        task.addPlugin(conf);
        task.addPlugin(oConf);
        task.addPlugin(pConf);

        BasicJobImpl job = new BasicJobImpl(new ResourcePool());
        task.toOndexJob(job);
        job.run();
    }
}
