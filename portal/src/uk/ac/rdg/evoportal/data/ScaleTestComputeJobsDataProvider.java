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

package uk.ac.rdg.evoportal.data;

import java.util.Iterator;
import java.util.List;
import java.util.Vector;
import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import org.hibernate.Query;
import org.hibernate.Session;
import org.hibernate.Transaction;
import uk.ac.rdg.evoportal.beans.ScaleTest;
import uk.ac.rdg.evoportal.beans.ScaleTestComputeJob;
import uk.ac.rdg.util.HibernateUtil;

/**
 *
 * @author david
 */
public class ScaleTestComputeJobsDataProvider implements IDataProvider {

    List<ScaleTestComputeJob> l = new Vector<ScaleTestComputeJob>();

    public ScaleTestComputeJobsDataProvider(long testID) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = s.beginTransaction();
        Query q = s.createQuery("from ScaleTest st where st.testID=" + testID);
        tx.commit();
        if (tx.wasCommitted()) {
            Object result = q.uniqueResult();
            if (result!=null && result instanceof ScaleTest) {
               ScaleTest scaleTest = (ScaleTest)result;
               l.addAll(scaleTest.getScaleTestComputeJobs());
            }
        }
    }

    public static ScaleTestComputeJob get(int jobID) {
        ScaleTestComputeJob scaleTestComputeJob = null;
        Session s = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = s.beginTransaction();
        Object result = s.createQuery("from ScaleTestComputeJob stcj where stcj.jobID=" + jobID).uniqueResult();
        tx.commit();
        if (tx.wasCommitted()) {
            scaleTestComputeJob = (ScaleTestComputeJob)result;
        }
        return scaleTestComputeJob;
    }

    public Iterator iterator(int first, int count) {
        return l.iterator();
    }

    public IModel model(Object object) {
        ScaleTestComputeJob scaleTestComputeJob = (ScaleTestComputeJob) object;
        return new ScaleTestComputeJobLoadableDetachableModel(scaleTestComputeJob.getJobID());
    }

    public int size() {
        return l.size();
    }

    public void detach() {
        // l=null;
    }

}
