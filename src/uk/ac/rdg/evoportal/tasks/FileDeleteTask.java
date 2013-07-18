package uk.ac.rdg.evoportal.tasks;

import java.io.IOException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.rdg.evoportal.GlobalConstants;

/**
 *
 * @author david
 */
public class FileDeleteTask extends TimerTask {

    private String USER = GlobalConstants.getProperty("pbsnode.username");
    private String HOST  = GlobalConstants.getProperty("pbsnode.host");
    private String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");
    private String owner;
    private String fileName;
    private transient Logger LOG = Logger.getLogger(FileDeleteTask.class.getName());

    public FileDeleteTask(String owner, String fileName) {
        this.owner = owner;
        this.fileName = fileName;
    }

    public void run() {
        LOG.fine("FileDeleteTask starting");
        String[] cmd = new String[]{"ssh", USER + "@" + HOST,  "rm " + REMOTE_FILE_ROOT + owner + "/mynexusfiles/" + fileName};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p;
        try {
            p = pb.start();
            int exitValue = 0;
            try {
                exitValue = p.waitFor();
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            if (exitValue>0) {
                throw new IOException("A problem occurred; exit value=" + exitValue);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        LOG.fine("FileDeleteTask finished");
    }

}
