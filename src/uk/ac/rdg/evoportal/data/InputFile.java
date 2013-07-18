package uk.ac.rdg.evoportal.data;

import java.io.File;
import org.apache.wicket.IClusterable;

/**
 *
 * @author david
 */
public class InputFile implements IClusterable {
    File file;

    public InputFile(File f) {
        this.file = f;
    }

    public File getFile() {
        return file;
    }

}