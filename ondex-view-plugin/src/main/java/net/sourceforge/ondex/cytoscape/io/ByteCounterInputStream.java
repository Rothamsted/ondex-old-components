/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sourceforge.ondex.cytoscape.io;

import java.io.IOException;
import java.io.InputStream;

/**
 *
 * @author jweile
 */
public class ByteCounterInputStream extends InputStream {

    private long bytesRead = 0L;

    private InputStream in;

    public ByteCounterInputStream(InputStream in) {
        this.in = in;
    }

    @Override
    public int read() throws IOException {
        int val = in.read();
        bytesRead++;
        return val;
    }

    public long getBytesRead() {
        return bytesRead;
    }

}
