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

import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import uk.ac.rdg.evoportal.ActiveJob;
import uk.ac.rdg.evoportal.beans.ScaleTestComputeJob;
import uk.ac.rdg.util.HibernateUtil;

/**
 *
 * @author david
 */
public class ScaleTestUpdateTask extends TimerTask {

    transient Logger LOG = Logger.getLogger(ComputeJobsUpdateTask.class.getName());
    private Hashtable<Integer, ActiveJob> activeJobsCache;

    public ScaleTestUpdateTask(Hashtable<Integer, ActiveJob> activeJobsCache) {
        this.activeJobsCache = activeJobsCache;
    }

    @Override
    public void run() {
        LOG.fine("ScaleTestUpdateTask starting");
        Session s = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = s.beginTransaction();
        List<ScaleTestComputeJob> results = s.createQuery("from ScaleTestComputeJob").list();
        tx.commit();
        if (tx.wasCommitted()) {
            for (Iterator i = results.iterator();i.hasNext();) {
                ScaleTestComputeJob scaleTestComputeJob = (ScaleTestComputeJob)i.next();
                int jobID = scaleTestComputeJob.getJobID();
                ActiveJob activeJob = activeJobsCache.get(jobID);
                if (activeJob!=null) {
                    scaleTestComputeJob.setStatus(activeJob.getStatus());
                    scaleTestComputeJob.setTimeUsed(activeJob.getTimeUsed());
                    tx = s.beginTransaction();
                    s.update(scaleTestComputeJob);
                    tx.commit();
                    if (!tx.wasCommitted()) {
                        LOG.severe("Could not update job " + jobID);
                    }

                } else {
                    if (scaleTestComputeJob.getStatus()!='S' && scaleTestComputeJob.getStatus()!='!') {
                        // only cleanup compute job once
                        scaleTestComputeJob.setStatus('S');
                        tx = s.beginTransaction();
                        s.update(scaleTestComputeJob);
                        tx.commit();
                        if (!tx.wasCommitted()) {
                            LOG.severe("Could not update job " + jobID);
                        } else {
                            ScaleTestComputeJobCompleteTask scaleTestComputeJobCompleteTask = new ScaleTestComputeJobCompleteTask(jobID);
                            scaleTestComputeJobCompleteTask.run();
                        }
                    }
                }
            }
        } else {
            LOG.severe("Could not retrieve scale test jobs");
        }
        s.close();
        LOG.fine("ScaleTestUpdateTask finished");
    }
}
