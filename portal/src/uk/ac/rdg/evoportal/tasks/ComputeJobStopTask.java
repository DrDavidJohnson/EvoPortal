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
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.rdg.pbsclient.PBSNoPassClient;

/**
 *
 * @author david
 */
public class ComputeJobStopTask {
    private int jobID;
    private transient Logger LOG = Logger.getLogger(ComputeJobStopTask.class.getName());

    public ComputeJobStopTask(int jobID) {
        this.jobID = jobID;
    }

    public void run() {
        // works independent of DB, so can be used to stop ScaleTestComputeJobs as well
        LOG.fine("ComputeJobStopTask starting");
        try {
            if (PBSNoPassClient.qdel(jobID)) {
                LOG.fine("Job " + jobID + " deleted from PBS");
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        LOG.fine("ComputeJobStopTask finished");
    }
    
}
