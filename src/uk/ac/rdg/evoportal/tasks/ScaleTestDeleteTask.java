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
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import uk.ac.rdg.evoportal.GlobalConstants;
import uk.ac.rdg.evoportal.beans.ScaleTest;
import uk.ac.rdg.evoportal.beans.ScaleTestComputeJob;
import uk.ac.rdg.util.HibernateUtil;

/**
 *
 * @author david
 */
public class ScaleTestDeleteTask extends TimerTask {
    
    private long testID;
    private String HOST = GlobalConstants.getProperty("pbsnode.host");
    private String USER = GlobalConstants.getProperty("pbsnode.username");
    private String PWD = GlobalConstants.getProperty("pbsnode.password");
    private String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");
    private transient Logger LOG = Logger.getLogger(ScaleTestDeleteTask.class.getName());

    public ScaleTestDeleteTask(long testID) {
        this.testID = testID;
    }

    @Override
    public void run() {
        LOG.fine("ScaleTestDeleteTask starting");
        Session s = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = s.beginTransaction();
        Object result = s.createQuery("from ScaleTest st where st.testID=" + testID).uniqueResult();
        tx.commit();
        if (tx.wasCommitted()) {
            if (result!=null && result instanceof ScaleTest) {
                ScaleTest scaleTest = (ScaleTest)result;
                List<ScaleTestComputeJob> scaleTestComputeJobs = scaleTest.getScaleTestComputeJobs();
                try {
                    for (Iterator<ScaleTestComputeJob> i = scaleTestComputeJobs.iterator();i.hasNext();) {
                        ScaleTestComputeJob scaleTestComputeJob = i.next();
                        String[] cmd = new String[]{"ssh", USER + "@" + HOST,   "rm -r " + REMOTE_FILE_ROOT + scaleTestComputeJob.getOwner() + "/" + scaleTestComputeJob.getJobID()};
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
                        tx = s.beginTransaction();
                        s.delete(scaleTestComputeJob);
                        tx.commit();
                        if (!tx.wasCommitted()) {
                            throw new IOException("Could not delete job " + scaleTestComputeJob.getJobID() + " from DB");
                        }
                    }
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
                tx = s.beginTransaction();
                s.delete(scaleTest); // does this remove the corresponding ScaleTestComputeJobs?
                tx.commit();
                if (!tx.wasCommitted()) {
                    LOG.severe("Could not delete scale test " + scaleTest.getTestID());
                }                
            }
        }
        LOG.fine("ScaleTestDeleteTask finished");
    }

}
