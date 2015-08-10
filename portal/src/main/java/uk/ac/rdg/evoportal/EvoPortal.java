/*
 *  Copyright 2009 David Johnson, School of Biological Sciences,
 *  University of Reading, UK.
 *
 *  Licensed to the Apache Software Foundation (ASF) under one
 *  or more contributor license agreements.  See the NOTICE file
 *  distributed with this work for additional information
 *  regarding copyright ownership.  The ASF licenses this file
 *  to you under the Apache License, Version 2.0 (the
 *  "License"); you may not use this file except in compliance
 *  with the License.  You may obtain a copy of the License at
 *
 *  http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing,
 *  software distributed under the License is distributed on an
 *  "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 *  KIND, either express or implied.  See the License for the
 *  specific language governing permissions and limitations
 *  under the License.
 */

package uk.ac.rdg.evoportal;

import org.apache.wicket.Request;
import org.apache.wicket.Response;
import org.apache.wicket.Session;
import org.apache.wicket.protocol.http.WebApplication;
import org.hibernate.Transaction;
import org.hsqldb.server.Server;
import uk.ac.rdg.evoportal.beans.PortalUser;
import uk.ac.rdg.evoportal.pages.Login;
import uk.ac.rdg.evoportal.tasks.*;
import uk.ac.rdg.pbsclient.PBSNoPassClient;
import uk.ac.rdg.util.ExpiringObjectTable;
import uk.ac.rdg.util.HibernateUtil;

import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Date;
import java.util.Hashtable;
import java.util.Timer;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 * Main entry point class to the EvoPortal application, a bit like a
 * main method in regular Java applications.
 */

public class EvoPortal extends WebApplication {

    private transient Logger LOG = Logger.getLogger(EvoPortal.class.getName()); // class logger
    private ExpiringObjectTable sessionCounter = new ExpiringObjectTable(60L*1000L); // tracks alive sessions, expires after 60s
    private boolean initializationError = false; // flag for init error
    private Hashtable<Integer, ActiveJob> activeJobsCache = new Hashtable<Integer, ActiveJob>(); // job cache for qstats
    private String pbsStats = "";

    // scheduling stuff
    private Timer timer = new Timer(true); // scheduler
    private QstatTask qstatTask = new QstatTask(activeJobsCache);
    private RsyncTask rsyncTask = new RsyncTask();
    private ComputeJobsUpdateTask computeJobsUpdateTask = new ComputeJobsUpdateTask(activeJobsCache);
    private ScaleTestUpdateTask scaleTestsUpdateTask = new ScaleTestUpdateTask(activeJobsCache);
    private EmailNotifyTask emailNotifyTask = new EmailNotifyTask();
    private Server db;

    @Override
    protected void init() {
        super.init();
        // check config.properties on initialization of portal
        // check props
        boolean propError = false;
        String uploadMaxFileSizeString = GlobalConstants.getProperty("upload.maxfilesize");
        try {
            Integer.parseInt(uploadMaxFileSizeString); // throws exception if can't format
        } catch (NumberFormatException ex) {
            LOG.log(Level.SEVERE,  "Could not parse upload size property: " + uploadMaxFileSizeString, ex);
            propError = true;
        }

        if (propError) {
            // start app with error page only
            initializationError = true;
        }
        
        getResourceSettings().setThrowExceptionOnMissingResource(false);
        getMarkupSettings().setDefaultBeforeDisabledLink("<a href=\"#\">");
        getMarkupSettings().setDefaultAfterDisabledLink("</a>");
        getDebugSettings().setAjaxDebugModeEnabled(false);

        // startup embedded db server
        db = new Server();
//        db.setLogWriter(null); // point to null so no logging happens
        db.setSilent(false);
        db.setDatabaseName(0, "evoportal"); // use evoportal db name
        db.setDatabasePath(0, "file:" + GlobalConstants.getProperty("local.fileroot") +  "../db/evoportal"); // where to save db todb.start(); // startup
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
                ResultSet results = s.executeQuery("SELECT * FROM INFORMATION_SCHEMA.SYSTEM_TABLES");
                boolean portalUserExists = false;
                boolean computeJobExists = false;
                boolean scaleTestExists = false;
                boolean scaleTestComputeJobExists = false;
                if (results!=null) {
                    while(results.next()) {
                        String tableName = results.getString("TABLE_NAME");
                        if ("PORTALUSER".equals(tableName)) {
                            portalUserExists = true;
                        }
                        if ("COMPUTEJOB".equals(tableName)) {
                            computeJobExists = true;
                        }
                        if ("SCALETEST".equals(tableName)) {
                            scaleTestExists = true;
                        }
                        if ("SCALETESTCOMPUTEJOB".equals(tableName)) {
                            scaleTestComputeJobExists = true;
                        }
                    }
                }
                results.close();
                s.close();
                // create tables that don't yet exist if need to create them
                if (!portalUserExists) {
                    // create PORTALUSER table
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
                    // insert a default user
                    PortalUser adminUser = new PortalUser();
                    adminUser.setUsername("admin");
                    adminUser.setPasswordHash("adminadmin");
                    adminUser.setEmailAddress("admin@mydomain.org");
                    org.hibernate.Session sess = HibernateUtil.getSessionFactory().openSession();
                    Transaction tx = sess.beginTransaction();
                    sess.persist(adminUser);
                    tx.commit();
                    if (!tx.wasCommitted()) {
                        LOG.severe("Could not insert admin user into DB");
                    } else {
                        // create user's working directories on the PBS node
                        try {
                            String HOST = GlobalConstants.getProperty("pbsnode.host");
                            String USER = GlobalConstants.getProperty("pbsnode.username");
                            String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");
                            String[] cmd = new String[]{"ssh", USER + "@" + HOST, "mkdir " + REMOTE_FILE_ROOT + "admin"};
                            ProcessBuilder pb = new ProcessBuilder(cmd);
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
                            cmd = new String[]{"ssh", USER + "@" + HOST, "mkdir " + REMOTE_FILE_ROOT + "admin" + "/mynexusfiles/"};
                            pb = new ProcessBuilder(cmd);
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
                        } catch (IOException ioEx) {
                            LOG.log(Level.SEVERE, ioEx.getMessage(), ioEx);
                            // TODO implement rollback
                        }
                    }
                }
                if (!computeJobExists) {
                    // create COMPUTEJOB table
                    s = c.createStatement();
                    s.executeUpdate("CREATE TABLE COMPUTEJOB " +
                            "(" +
                            "computejob_id BIGINT IDENTITY," +
                            "jobID INTEGER," +
                            "label VARCHAR(255)," +
                            "nodes INTEGER," +
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
                    s = c.createStatement();
                    s.executeUpdate("CREATE TABLE SCALETESTCOMPUTEJOB" +
                            "(" +
                            "scaletestcomputejob_id BIGINT IDENTITY," +
                            "jobID INTEGER," +
                            "label VARCHAR(255)," +
                            "nodes INTEGER," +
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
                LOG.log(Level.SEVERE, ex.getMessage(), ex);
                initializationError = true;
            }
        } else {
            LOG.severe("Could not start DB server");
            initializationError = true;
        }

        // we assume the connection to the PBS node is fine, no check happens so start scheduler
        if (!initializationError) {
            startScheduler();
        }

    }

    /**
     * Returns an map of job IDs to active jobs. Active jobs are
     * representations of jobs still running on the PBS node.
     *
     * @return      a hashtable containing job IDs mapping to jobs
     *              representing active jobs
     * @see         EvoPortal, ActiveJob
     */
    public Hashtable<Integer, ActiveJob> getActiveJobsCache() {
        // accessor for active jobs as updated from last qstat task
        return activeJobsCache;
    }

    public Class getHomePage() {
        // if there was a problem starting up report an internal error occurred
        // otherwise startup as normal
        if (initializationError) {
            return getApplicationSettings().getInternalErrorPage();
        } else {
            return Login.class;
        }
    }

    @Override
    public Session newSession(Request request, Response response) {
        return new LoginSession(this, request);
    }

    /**
     * Starts the scheduler which runs periodic tasks including qstat,
     * rsync, job and test updaters, and email notifier
     *
     * @see         EvoPortal
     */
    private void startScheduler() {
        // when users are logged in, increase granularity of update tasks to closer to real-time
        timer.schedule(new TimerTask() {

            @Override
            public void run() {
                int liveSessions = sessionCounter.size();
                if (liveSessions>0) {
                    if (qstatTask!=null) qstatTask.cancel();
                    if (rsyncTask!=null) rsyncTask.cancel();
                    if (computeJobsUpdateTask!=null) computeJobsUpdateTask.cancel();
                    if (scaleTestsUpdateTask!=null) scaleTestsUpdateTask.cancel();
                    qstatTask = null;
                    rsyncTask = null;
                    computeJobsUpdateTask = null;
                    scaleTestsUpdateTask = null;
//                    System.gc(); // do we need to run garbage collect or not?
                    qstatTask = new QstatTask(activeJobsCache);
                    rsyncTask = new RsyncTask();
                    computeJobsUpdateTask = new ComputeJobsUpdateTask(activeJobsCache);
                    scaleTestsUpdateTask = new ScaleTestUpdateTask(activeJobsCache);
                    timer = new Timer(true);
                    timer.schedule(qstatTask, 0L, 15L*60L*1000L);
                    timer.schedule(rsyncTask, 0L, 15L*60L*1000L);
                    timer.schedule(computeJobsUpdateTask, 0L, 15L*60L*1000L);
                    timer.schedule(scaleTestsUpdateTask, 0L, 15L*60L*1000L);
                    // stat pbs every 15 seconds ONLY when someone is logged in
                    try {
                        pbsStats = PBSNoPassClient.showqProcessorsActive() + " (last updated " + new Date().toString() + ")";
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }, 0L, 15L*1000L); // every 15 seconds

        // wait a minute before starting our tasks to allow portal to finish init
        timer.schedule(qstatTask, 60L*1000L, 15L*60L*1000L); // starts qstatTask every 15 mins
        timer.schedule(rsyncTask, 60L*1000L, 15L*60L*1000L); // starts qstatTask every 15 mins
        timer.schedule(computeJobsUpdateTask, 60L*1000L, 15L*60L*1000L); // starts qstatTask every 15 mins
        timer.schedule(scaleTestsUpdateTask, 60L*1000L, 15L*60L*1000L);

        // the email notifier task checks DB every 5 minutes
        timer.schedule(emailNotifyTask, 60L*1000L, 5L*60L*1000L);
    }

    /**
     * Returns a string containing the most recent cluster usage
     * statistics which are returned by the showq command. This string
     * is updated by a scheduled task that executes only while one or
     * more users is logged into the portal.
     *
     * @return      a string containing the most recent cluster stats
     * @see         EvoPortal
     */
    public String getPBSStats() {
        return pbsStats;
    }

    public synchronized void touchSession(String username) {
        sessionCounter.put(username, new Object());
    }

    public synchronized void invalidateSession(String username) {
        sessionCounter.remove(username);
        // also cleanup tmp files from user directory here?
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        // cancel all tasks and timer
        qstatTask.cancel();
        rsyncTask.cancel();
        computeJobsUpdateTask.cancel();
        scaleTestsUpdateTask.cancel();
        emailNotifyTask.cancel();
        timer.cancel();

        // nullify all tasks and timer
        qstatTask = null;
        rsyncTask = null;
        computeJobsUpdateTask = null;
        scaleTestsUpdateTask = null;
        emailNotifyTask = null;
        timer = null;

        // stop db
        if (db!=null) {
            db.stop();
        }
    }


}
