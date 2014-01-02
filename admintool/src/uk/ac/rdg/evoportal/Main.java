package uk.ac.rdg.evoportal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Iterator;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Transaction;
import org.hsqldb.server.Server;
import uk.ac.rdg.evoportal.beans.PortalUser;
import uk.ac.rdg.util.HibernateUtil;

/**
 *
 * @author david
 */
public class Main {

    private static Logger LOG = Logger.getLogger(Main.class.getName()); // class logger

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws IOException {
        // check config.properties on initialization of portal
        // check props
        boolean propError = false;
        boolean initializationError = false;
        String uploadMaxFileSizeString = GlobalConstants.getProperty("upload.maxfilesize");
        try {
            Integer.parseInt(uploadMaxFileSizeString); // throws exception if can't format
        } catch (NumberFormatException ex) {
            ex.printStackTrace();
            propError = true;
        }

        if (propError) {
            // start app with error page only
            initializationError = true;
        }

        // check if all filesystems are accessible

        // local file root
        File localDir = new File(GlobalConstants.getProperty("local.fileroot"));
        if (!localDir.exists()) {
            if (!localDir.mkdir()) {
                // error could not create directory
            }
        }
        // local db directory (outside of fileroot)
        File dbDir = new File(GlobalConstants.getProperty("local.dbroot"));
        if (!dbDir.exists()) {
            if (!dbDir.mkdir()) {
                // error could not create directory
            }
        }

        // startup embedded db server
        Server db = new Server();
//        db.setLogWriter(null); // point to null so no logging happens
        db.setSilent(true);
        db.setDatabaseName(0, "evoportal"); // use evoportal db name
        db.setDatabasePath(0, "file:" + GlobalConstants.getProperty("local.dbroot") + "evoportal"); // where to save db todb.start(); // startup
        db.start();
        while(db.getState()>1) {
            // poll until startup confirmed/complete, TODO need some way of timing this out
        }
        if (db.getState()==1) {
            // check database tables exist
            try {
                Class.forName("org.hsqldb.jdbcDriver" );
                Connection c = DriverManager.getConnection("jdbc:hsqldb:hsql://localhost/evoportal", "sa", "");
                Statement s = c.createStatement();
                System.out.println("Validating EvoPortal tables...");
                ResultSet results = s.executeQuery("SELECT * FROM INFORMATION_SCHEMA.SYSTEM_TABLES");
                boolean portalUserExists = false;
                boolean computeJobExists = false;
                boolean scaleTestExists = false;
                boolean scaleTestComputeJobExists = false;
                if (results!=null) {
                    while(results.next()) {
                        String tableName = results.getString("TABLE_NAME");
                        if ("PORTALUSER".equals(tableName)) {
                            System.out.println("PORTALUSER exists...");
                            portalUserExists = true;
                        }
                        if ("COMPUTEJOB".equals(tableName)) {
                            System.out.println("COMPUTEJOB exists...");
                            computeJobExists = true;
                        }
                        if ("SCALETEST".equals(tableName)) {
                            System.out.println("SCALETEST exists...");
                            scaleTestExists = true;
                        }
                        if ("SCALETESTCOMPUTEJOB".equals(tableName)) {
                            System.out.println("SCALETESTCOMPUTEJOB exists...");
                            scaleTestComputeJobExists = true;
                        }
                    }
                }
                results.close();
                s.close();
                // create tables that don't yet exist if need to create them
                if (!portalUserExists) {
                    // create PORTALUSER table
                    System.out.println("Creating PORTALUSER...");
                    s = c.createStatement();
                    s.executeUpdate("CREATE TABLE PORTALUSER " +
                            "(" +
                            "user_id BIGINT IDENTITY," +
                            "username VARCHAR(255)," +
                            "passwordHash VARCHAR(255)," +
                            "emailAddress VARCHAR(255)," +
                            "lastTouch BIGINT" +
                            ");");
                    s.close();                    
                }
                if (!computeJobExists) {
                    // create COMPUTEJOB table
                    System.out.println("Creating COMPUTEJOB...");
                    s = c.createStatement();
                    s.executeUpdate("CREATE TABLE COMPUTEJOB " +
                            "(" +
                            "computejob_id BIGINT IDENTITY," +
                            "jobID INTEGER," +
                            "label VARCHAR(255)," +
                            "nodes INTEGER," +
                            "ppn INTEGER," +
                            "submitTime BIGINT," +
                            "status CHAR(1)," +
                            "timeRequested BIGINT," +
                            "timeUsed BIGINT," +
                            "owner VARCHAR(255)," +
                            "notified BIT" +
                            ");");
                    s.close();
                }
                if (!scaleTestExists) {
                    // create SCALETEST table
                    System.out.println("Creating SCALETEST...");
                    s = c.createStatement();
                    s.executeUpdate("CREATE TABLE SCALETEST" +
                            "(" +
                            "scaletest_id BIGINT IDENTITY," +
                            "testID BIGINT," +
                            "label VARCHAR(255)," +
                            "BPBlock VARCHAR(255)," +
                            "owner VARCHAR(255)," +
                            "iterations INTEGER," +
                            "notified BIT" +
                            ");");
                    s.close();
                }
                if (!scaleTestComputeJobExists) {
                    // create SCALETESTCOMPUTEJOB table
                    System.out.println("Creating SCALETESTCOMPUTEJOB...");
                    s = c.createStatement();
                    s.executeUpdate("CREATE TABLE SCALETESTCOMPUTEJOB" +
                            "(" +
                            "scaletestcomputejob_id BIGINT IDENTITY," +
                            "jobID INTEGER," +
                            "label VARCHAR(255)," +
                            "nodes INTEGER," +
                            "ppn INTEGER," +
                            "submitTime BIGINT," +
                            "status CHAR(1)," +
                            "timeRequested BIGINT," +
                            "timeUsed BIGINT," +
                            "owner VARCHAR(255)," +
                            "duration INTEGER," +
                            "scaletest_id BIGINT," +
                            "indx INTEGER" +
    //                        "FOREIGN KEY (scaletest_id) REFERENCES SCALETEST(scaletest_id)" + // FIXME causes problems when saving
                            ");");
                    s.close();
                }
                c.close();
            } catch (Exception ex) {
                ex.printStackTrace();
                initializationError = true;
            }
        } else {
            System.out.println("Could not start DB server");
            initializationError = true;
        }

        if (!initializationError) {
            if (args.length>0) {


                String cmd = args[0]; // the command from {add, remove, setpass, list}
                if (cmd.equals("list")) {
                    org.hibernate.Session s = HibernateUtil.getSessionFactory().openSession();
                    Transaction tx = s.beginTransaction();
                    List results = s.createQuery("from PortalUser").list();
                    tx.commit();
                    if (tx.wasCommitted()) {
                        System.out.println("\nEvoPortal PortalUser list");
                        System.out.println("=========================");
                        for (Iterator i = results.iterator(); i.hasNext();) {
                            PortalUser user = (PortalUser)i.next();
                            System.out.println("Username: " + user.getUsername());
                            System.out.println("Email: " + user.getEmailAddress());
                            System.out.println("Last touch: " + new Date(user.getLastTouch()).toString());
                        }
                    }
                } else
                if (cmd.equals("add")) {
                    if (args.length!=4) {
                        System.out.println("\nusage: java -jar admintool.jar add [username] [password] [email]");
                        System.exit(0);
                    }
                    // insert a default user
                    PortalUser user = new PortalUser();
                    user.setUsername(args[1]);
                    user.setPasswordHash(args[2]);
                    user.setEmailAddress(args[3]);
                    org.hibernate.Session sess = HibernateUtil.getSessionFactory().openSession();
                    Transaction tx = sess.beginTransaction();
                    sess.persist(user);
                    tx.commit();
                    if (!tx.wasCommitted()) {
                        System.out.println("Could not insert user into DB");
                    } else {
                        // create user's working directories on the PBS node
                        System.out.println("Building user filesystems...");
                        try {
                            String HOST = GlobalConstants.getProperty("pbsnode.host");
                            String USER = GlobalConstants.getProperty("pbsnode.username");;
                            String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");
                            String[] sshcmd = new String[]{"ssh", USER + "@" + HOST, "mkdir " + REMOTE_FILE_ROOT + args[1]};
                            ProcessBuilder pb = new ProcessBuilder(sshcmd);
                            Process p = pb.start();
                            int exitValue = 0;
                            try {
                                exitValue = p.waitFor();
                            } catch (InterruptedException ex) {
                                LOG.log(Level.SEVERE, null, ex);
                            }
                            if (exitValue>0) {
                                throw new IOException("A problem occurred; exit value=" + exitValue);
                            }
                            sshcmd = new String[]{"ssh", USER + "@" + HOST, "mkdir " + REMOTE_FILE_ROOT + args[1] + "/mynexusfiles/"};
                            pb = new ProcessBuilder(sshcmd);
                            p = pb.start();
                            exitValue = 0;
                            try {
                                exitValue = p.waitFor();
                            } catch (InterruptedException ex) {
                                LOG.log(Level.SEVERE, null, ex);
                            }
                            if (exitValue>0) {
                                throw new IOException("A problem occurred; exit value=" + exitValue);
                            }
                            System.out.println("User filesystems created");
                        } catch (IOException ioEx) {
                            LOG.log(Level.SEVERE, ioEx.getMessage(), ioEx);
                            // TODO implement rollback
                        }
                    }
                } else
                if (cmd.equals("setpass") && args.length==3) {
                    if (args.length!=3) {
                        System.out.println("\nusage: java -jar admintool.jar setpass [username] [password]");
                        System.exit(0);
                    }
                    System.out.print ("Are you sure you want to set user " + args[1] +  " with password " + args[2] + " (yes/no): ");
                    System.out.flush(); // empties buffer, before you input text
                    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
                    String message = stdin.readLine();
                    if (!message.trim().equals("yes")) {
                        LOG.severe("Quitting...");
                        System.exit(0);
                    }
                    org.hibernate.Session sess = HibernateUtil.getSessionFactory().openSession();
                    Transaction tx = sess.beginTransaction();
                    Object result = sess.createQuery("from PortalUser user where user.username='" + args[1] + "'").uniqueResult();
                    tx.commit();
                    if (tx.wasCommitted()) {
                        if (result!=null && result instanceof PortalUser) {
                            PortalUser user = (PortalUser)result;
                            user.setPasswordHash(args[2]);
                            tx = sess.beginTransaction();
                            sess.update(user);
                            tx.commit();
                            if (!tx.wasCommitted()) {
                                System.out.println("Could not update user record");
                            } else {
                                System.out.println("User " + args[1] + " updated");
                            }
                        }
                    }
                } else
                if (cmd.equals("remove") && args.length==2) {
                    if (args.length!=2) {
                        System.out.println("\nusage: java -jar admintool.jar remove [username]");
                        System.exit(0);
                    }
                    // notify admin to manually delete all jobs of user by logging in as user
                    System.out.println("You want to delete user: " + args[1]);
                    System.out.print ("Have you manually removed all user's jobs and tests (yes/no): ");
                    System.out.flush(); // empties buffer, before you input text
                    BufferedReader stdin = new BufferedReader(new InputStreamReader(System.in));
                    String message = stdin.readLine();
                    if (!message.trim().equals("yes")) {
                        System.out.println("Quitting...");
                        System.exit(0);
                    }
                    org.hibernate.Session sess = HibernateUtil.getSessionFactory().openSession();
                    Transaction tx = sess.beginTransaction();
                    Object result = sess.createQuery("from PortalUser user where user.username='" + args[1] + "'").uniqueResult();
                    tx.commit();
                    if (tx.wasCommitted()) {
                        if (result!=null && result instanceof PortalUser) {
                            PortalUser user = (PortalUser)result;
                            tx = sess.beginTransaction();
                            sess.delete(user);
                            tx.commit();
                            if (tx.wasCommitted()) {
                                System.out.println("Deleting user filesystems...");
                                try {
                                    // remove directories!
                                    String HOST = GlobalConstants.getProperty("pbsnode.host");
                                    String USER = GlobalConstants.getProperty("pbsnode.username");;
                                    String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");
                                    String[] sshcmd = new String[]{"ssh", USER + "@" + HOST, "rm -r " + REMOTE_FILE_ROOT + args[1]};
                                    ProcessBuilder pb = new ProcessBuilder(sshcmd);
                                    Process p = pb.start();
                                    int exitValue = 0;
                                    try {
                                        exitValue = p.waitFor();
                                    } catch (InterruptedException ex) {
                                        LOG.log(Level.SEVERE, null, ex);
                                    }
                                    if (exitValue>0) {
                                        throw new IOException("A problem occurred; exit value=" + exitValue);
                                    }
                                } catch (IOException ioEx) {
                                    ioEx.printStackTrace();
                                    // TODO implement rollback?
                                }
                                System.out.println("Filesystem cleaned up OK");
                            }
                        } else {
                            System.out.println("User does not exist in DB");
                        }
                    }
                } else {
                    System.out.println("\nUsage: java -jar admintool.jar {add, setpass, remove, list} [args...]");
                }
            } else {
                System.out.println("\nUsage: java -jar admintool.jar {add, setpass, remove, list} [args...]");
            }
        } else {
            LOG.severe("There was an error initializing the DB");
        }
        System.out.println();
//        db.shutdown();
        System.exit(0);
    }

}
