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
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import uk.ac.rdg.evoportal.GlobalConstants;
import uk.ac.rdg.evoportal.beans.ComputeJob;
import uk.ac.rdg.evoportal.data.ComputeJobsDataProvider;
import uk.ac.rdg.util.HibernateUtil;

/**
 *
 * @author david
 */
public class ComputeJobDeleteTask extends TimerTask {

    private int jobID;
    private ComputeJob computeJob = null;
    private transient Logger LOG = Logger.getLogger(ComputeJobCompleteTask.class.getName());

    private String HOST = GlobalConstants.getProperty("pbsnode.host");
    private String USER = GlobalConstants.getProperty("pbsnode.username");
    private String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");

    public ComputeJobDeleteTask(int jobID) {
        this.jobID = jobID;
    }

    @Override
    public void run() {
        LOG.fine("ComputeJobDeleteTask starting");        
        computeJob = ComputeJobsDataProvider.get(jobID);
        String[] cmd = new String[]{"ssh", USER + "@" + HOST,  "rm -r " + REMOTE_FILE_ROOT + computeJob.getOwner() + "/" + jobID};
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
        Session s = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = s.beginTransaction();
        s.delete(computeJob);
        tx.commit();
        if (!tx.wasCommitted()) {
            LOG.severe("Could not delete job " + computeJob.getJobID() + " from database");
            // TODO implement rollback if can't delete job?
        }
        LOG.fine("ComputeJobDeleteTask finished");
    }
}
