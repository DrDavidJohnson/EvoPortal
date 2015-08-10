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
import java.util.Hashtable;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.rdg.evoportal.ActiveJob;
import uk.ac.rdg.evoportal.GlobalConstants;
import uk.ac.rdg.pbsclient.PBSNoPassClient;

/**
 *
 * @author david
 */
public class QstatTask extends TimerTask {

    private String USER = GlobalConstants.getProperty("pbsnode.username");
    private transient Logger LOG = Logger.getLogger(QstatTask.class.getName());
    private Hashtable<Integer, ActiveJob> activeJobsCache;

    public QstatTask(Hashtable<Integer, ActiveJob> activeJobsCache) {
        this.activeJobsCache = activeJobsCache;
    }

    public void run() {
        LOG.fine("QstatTask starting");
        String qstatString = null;
        try {
            qstatString = PBSNoPassClient.qstatGrep(USER); // grep for portal/PBS user only
        } catch (IOException ex) {
           LOG.log(Level.SEVERE, null, ex);
           return;
        }
        activeJobsCache.clear(); // clear it and repopulate according to qstat
        if (qstatString.length()>0) {
            String[] qstatJobs = qstatString.split("\n");
            // build a Hashtable of active jobs
            for (int i=0;i<qstatJobs.length;i++) {
                if (qstatJobs[i].length()>0) {
                    String[] qstatJobFields = qstatJobs[i].trim().split("[ ]+");
                    int id = -1;
                    char status = 'S';
                    String timeUsedAsString = null;
                    if (qstatJobFields.length==6) {
                        String idStr = qstatJobFields[0].trim().split("[.]")[0];
                        id = Integer.parseInt(idStr);
                        timeUsedAsString = qstatJobFields[3];
                        status = qstatJobFields[4].charAt(0);
                    } else {
                        // deal with exception for unexpected number of qstat fields
                    }
                    long timeUsed = 0L;
                    if (!timeUsedAsString.equals("0")) {
                        String[] timeUsedField = timeUsedAsString.trim().split("[:]");
                        timeUsed = timeUsed + Long.parseLong(timeUsedField[2]) * 1000L; // seconds
                        timeUsed = timeUsed + Long.parseLong(timeUsedField[1]) * 1000L * 60L; // minutes
                        timeUsed = timeUsed + Long.parseLong(timeUsedField[0]) * 1000L * 60L * 60L; // hours
                    }
                    LOG.fine("Refreshing job " + id);
                    ActiveJob activeJob = new ActiveJob(id, status, timeUsed);
                    activeJobsCache.put(activeJob.getJobID(), activeJob);
                }
            }
        }
        LOG.fine("QstatTask finished");
    }

}
