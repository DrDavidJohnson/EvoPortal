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

import java.io.IOException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import uk.ac.rdg.evoportal.GlobalConstants;
import uk.ac.rdg.evoportal.beans.ComputeJob;
import uk.ac.rdg.evoportal.beans.PortalUser;
import uk.ac.rdg.evoportal.data.ComputeJobsDataProvider;
import uk.ac.rdg.util.HibernateUtil;

/**
 *
 * @author david
 */
public class ComputeJobCompleteTask extends TimerTask {
    
    private int jobID;
    private ComputeJob computeJob = null;
    private PortalUser user = null;
    private transient Logger LOG = Logger.getLogger(ComputeJobCompleteTask.class.getName());

    private String HOST = GlobalConstants.getProperty("pbsnode.host");
    private String USER = GlobalConstants.getProperty("pbsnode.username");
    private String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");

    public ComputeJobCompleteTask(int jobID) {
        this.jobID = jobID;
        computeJob = ComputeJobsDataProvider.get(jobID);
        Session s = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = s.beginTransaction();
        Query q = s.createQuery("from PortalUser u where u.username='" + computeJob.getOwner() + "'");
        tx.commit();
        if (tx.wasCommitted()) {
            Object result = q.uniqueResult();
            if (result!=null && result instanceof PortalUser) {
                user = (PortalUser)result;
            }
        }
    }

    public void run() {
        LOG.fine("ComputeJobCompleteTask starting");
        if (computeJob.getStatus()=='S') {
            String userDir = REMOTE_FILE_ROOT + user.getUsername();
            // cleanup filesystem
            try {
                String[] cmd = new String[]{"ssh", USER + "@" + HOST,
                    "mkdir " + userDir + "/" + jobID + ";" +
                    "mv " + userDir + "/*.e" + jobID + " " + userDir + "/" + jobID + "/;" +
                    "mv " + userDir + "/*.o" + jobID + " " + userDir + "/" + jobID + "/"};
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
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
        }
        LOG.fine("ComputeJobCompleteTask finished");
    }

}
