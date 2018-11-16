package net.sourceforge.ondex.modules.integration;

import java.io.File;
import java.io.FilenameFilter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLClassLoader;

/**
 * Test class-loading strategies for plugins.
 *
 * @author Matthew Pocock
 */
public class PluginClassLoadingTest
{
    public void testRawClassLoader() throws MalformedURLException, ClassNotFoundException
    {
        ClassLoader parent = Thread.currentThread().getContextClassLoader();
        if (parent == null) {
            parent = ClassLoader.getSystemClassLoader();
        }

        File[] plugins = new File("plugins/").listFiles(new FilenameFilter()
        {
            public boolean accept(File file, String s)
            {
                return s.endsWith(".jar");
            }
        });
        URL[] urls = new URL[plugins.length];
        for (int i = 0, pluginsLength = plugins.length; i < pluginsLength; i++) {
            urls[i] = plugins[i].toURI().toURL();
        }

        ClassLoader ucl = new URLClassLoader(urls, parent);

        Class oxlC = ucl.loadClass("net.sourceforge.ondex.parser.oxl.Parser");
    }
}
