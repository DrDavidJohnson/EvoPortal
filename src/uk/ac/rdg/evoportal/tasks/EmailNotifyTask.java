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

import java.util.Iterator;
import java.util.List;
import java.util.TimerTask;
import org.hibernate.Session;
import org.hibernate.Transaction;
import uk.ac.rdg.evoportal.beans.ComputeJob;
import uk.ac.rdg.evoportal.beans.ScaleTest;
import uk.ac.rdg.util.HibernateUtil;

/**
 *
 * @author david
 */
public class EmailNotifyTask extends TimerTask {

    @Override
    public void run() {
        Session s = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = s.beginTransaction();
        List<ComputeJob> cjResults = s.createQuery("from ComputeJob cj where cj.notified=false and cj.status='S'").list();
        tx.commit();
        if (tx.wasCommitted()) {
            for (Iterator<ComputeJob> i = cjResults.iterator();i.hasNext();) {
                ComputeJob cj = i.next();
                int jobID = cj.getJobID();
                ComputeJobNotifyTask computeJobNotifyTask = new ComputeJobNotifyTask(jobID);
                computeJobNotifyTask.run();
            }
        }
        tx = s.beginTransaction();
        List<ScaleTest> stResults = s.createQuery("from ScaleTest st where st.notified=false").list();
        tx.commit();
        if (tx.wasCommitted()) {
            for (Iterator<ScaleTest> i = stResults.iterator();i.hasNext();) {
                ScaleTest st = i.next();
                if (st.getPercentageDone()==100) {
                    long testID = st.getTestID();
                    ScaleTestNotifyTask scaleTestNotifyTask = new ScaleTestNotifyTask(testID);
                    scaleTestNotifyTask.run();       
                }
            }
        }
    }

}
