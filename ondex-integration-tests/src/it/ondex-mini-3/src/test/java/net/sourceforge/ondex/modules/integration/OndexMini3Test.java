package net.sourceforge.ondex.modules.integration;

import junit.framework.AssertionFailedError;
import junit.framework.TestCase;
import org.apache.commons.exec.*;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

/**
 * Test workflow execution for iah and integration modules.
 * 
 * @author Matthew Pocock
 */
public class OndexMini3Test extends TestCase {
	//TODO this has been obsolete for several years and needs to be updated 
	
	private Executor exec;
	private File ondexMini;
	private ByteArrayOutputStream baos;

	protected void setUp() throws Exception {
		/**
		baos = new ByteArrayOutputStream();
		exec = new DefaultExecutor();
		exec.setStreamHandler(new PumpStreamHandler(System.out, System.err));

		File target = new File("target/");
		for (File f : target.listFiles()) {
			if (f.getName().startsWith("ondex-mini") && f.isDirectory()) {
				ondexMini = f;
				System.out.println("Using ondex-mini installation directory: "
						+ ondexMini.getAbsolutePath());
			}
		}

		if (ondexMini == null)
			throw new RuntimeException(
					"Could not initialise ondex-mini directory, starting from: "
							+ target.getAbsolutePath());
		*/
		}
	
	

	public void testWorkflowTest3() {
		/**
		CommandLine cl = new CommandLine(
				resolveScriptNameForOS(ondexMini.getAbsolutePath()
						+ File.separator + "runme"));
		cl.addArgument("workflow_test_3.xml");

		exec.setWorkingDirectory(ondexMini);
		exec.setExitValues(new int[] { 0, 1, 2 });

		int exitCode = exec.execute(cl);

		assertEquals("Exit code should be zero", 0, exitCode);
		*/
	}

	// adapted from TestUtil in commons-exec
	public static String resolveScriptNameForOS(String script) {
		if (OS.isFamilyWindows()) {
			return script + ".bat";
		} else if (OS.isFamilyUnix()) {
			return script + ".sh";
		} else if (OS.isFamilyOpenVms()) {
			return script + ".dcl";
		} else {
			throw new AssertionFailedError("Test not supported for this OS");
		}
	}
}
