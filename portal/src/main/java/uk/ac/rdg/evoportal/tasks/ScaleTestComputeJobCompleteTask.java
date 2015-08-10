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

package uk.ac.rdg.evoportal.tasks;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import uk.ac.rdg.evoportal.GlobalConstants;
import uk.ac.rdg.evoportal.beans.ScaleTestComputeJob;
import uk.ac.rdg.evoportal.data.ScaleTestComputeJobsDataProvider;
import uk.ac.rdg.util.HibernateUtil;

/**
 *
 * @author david
 */
public class ScaleTestComputeJobCompleteTask extends TimerTask {
    
    private String HOST = GlobalConstants.getProperty("pbsnode.host");
    private String USER = GlobalConstants.getProperty("pbsnode.username");
    private String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");
    private String LOCAL_FILE_ROOT = GlobalConstants.getProperty("local.fileroot");
    private int jobID;
    private transient Logger LOG = Logger.getLogger(ScaleTestComputeJobCompleteTask.class.getName());

    public ScaleTestComputeJobCompleteTask(int jobID) {
        this.jobID = jobID;
    }

    @Override
    public void run() {
        LOG.fine("ScaleTestComputeJobCompleteTask starting");
        ScaleTestComputeJob computeJob = ScaleTestComputeJobsDataProvider.get(jobID);
        String user = computeJob.getOwner();
        if (computeJob.getStatus()=='S') {
            String userDir = REMOTE_FILE_ROOT + user;
            // cleanup filesystem
            String[] cmd = new String[]{"ssh", USER + "@" + HOST, "mkdir " + userDir + "/" + jobID + ";" +
                                  "mv " + userDir + "/*.e" + jobID + " " + userDir + "/" + jobID + "/;" +
                                  "mv " + userDir + "/*.o" + jobID + " " + userDir + "/" + jobID + "/"};
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

            // extract test result for this particular job
            RsyncTask rsyncTask = new RsyncTask();
            rsyncTask.run(); // ensure we're sync'd as we read results locally
            String localJobDir = LOCAL_FILE_ROOT + user + "/" + jobID;
            File f = new File(localJobDir);
            if (f.isDirectory()) {
                File[] files = f.listFiles();
                for (int i = 0; i < files.length; i++) {
                    try {
                        if (files[i].getCanonicalPath().endsWith("o" + jobID)) {
                            File outputFile = files[i];
                            BufferedReader br = new BufferedReader(new FileReader(outputFile));
                            String l = null;
                            Date date1 = null;
                            Date date2 = null;
                            SimpleDateFormat format = new SimpleDateFormat("dd/MM/yy HH:mm:ss");
                            while ((l = br.readLine()) != null) {
                                try {
                                    if (date1 == null) {
                                        date1 = format.parse(l);
                                    } else {
                                        date2 = format.parse(l);
                                    }
                                } catch (ParseException ex) {
                                } // ignore exceptions as we're only concerned with lines with valid dates
                            }
                            if (date1 != null && date2 != null) {
                                long millis = date2.getTime() - date1.getTime(); 
                                int seconds = (int)(millis / 1000L);
                                computeJob.setDuration(seconds);
                                Session s = HibernateUtil.getSessionFactory().openSession();
                                Transaction tx = s.beginTransaction();
                                s.update(computeJob);
                                tx.commit();
                                if (!tx.wasCommitted()) {
                                    throw new IOException("Could not update job " + computeJob.getJobID());
                                }
                            }
                        }
                    } catch (IOException ex) {
                        LOG.log(Level.SEVERE, null, ex);
                    }
                }
            }
        }
        LOG.fine("ScaleTestComputeJobCompleteTask finished");
    }

}
