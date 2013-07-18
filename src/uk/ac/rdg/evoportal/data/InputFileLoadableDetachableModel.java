/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package uk.ac.rdg.evoportal.data;

import java.io.File;
import org.apache.wicket.model.LoadableDetachableModel;

/**
 *
 * @author david
 */
public class InputFileLoadableDetachableModel extends LoadableDetachableModel {

    File file;

    public InputFileLoadableDetachableModel(File f) {
        this.file = f;
    }

    @Override
    protected Object load() {
        InputFile inputFile = null;
        if (file.exists())
            inputFile = new InputFile(file);
        return inputFile;
    }

}

