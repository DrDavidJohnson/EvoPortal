package uk.ac.rdg.evoportal.data;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import uk.ac.rdg.evoportal.GlobalConstants;

/**
 *
 * @author david
 */
public class InputFilesDataProvider implements IDataProvider {

    // jdbc Connection
    List<InputFile> l = new Vector<InputFile>();

    static String LOCAL_FILE_ROOT = GlobalConstants.getProperty("local.fileroot");

    public InputFilesDataProvider(String owner) {
        File f = new File(LOCAL_FILE_ROOT + owner + "/mynexusfiles/");
        if (f.isDirectory()) {
            File[] fileList = f.listFiles();
            for (int i=0;i<fileList.length;i++) {
                l.add(new InputFile(fileList[i]));
            }
        }
    }

    public List<String> listFiles() {
        List<String> filesList = new Vector();
        for(Iterator<InputFile> i = l.iterator();i.hasNext();) {
            filesList.add(i.next().getFile().getName());
        }
        return filesList;
    }

    public Iterator iterator(int first, int count) {
        return l.iterator();
    }

    public IModel model(Object object) {
        InputFile jobFile = (InputFile)object;
        return new InputFileLoadableDetachableModel(jobFile.getFile());
    }

    public int size() {
        return l.size();
    }

    public void detach() {
        //l = null;
    }

}
