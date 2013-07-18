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

package uk.ac.rdg.evoportal.beans;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.IClusterable;

/**
 *
 * @author david
 */
public class ScaleTest implements IClusterable {

    private long testID;
    private String label;
    private String BPBlock;
    private String owner;
    private int iterations;
    private List<ScaleTestComputeJob> scaleTestComputeJobs;
    private boolean notified;
    private long id;

    public ScaleTest(long testID, String label, String BPBlock, String owner, long id, int iterations, List<ScaleTestComputeJob> scaleTestComputeJobs) {
        this.testID = testID;
        this.label = label;
        this.BPBlock = BPBlock;
        this.owner = owner;
        this.id = id;
        this.iterations = iterations;
        this.scaleTestComputeJobs = scaleTestComputeJobs;
        this.notified = false;
    }

    public ScaleTest() {
        super();
        this.id = -1;
        this.testID = -1;
        this.label = "";
        this.BPBlock = "";
        this.owner = "";
        this.iterations = 0;
        this.scaleTestComputeJobs = new ArrayList();
        this.notified = false;
    }

    public List getScaleTestComputeJobs() {
        return scaleTestComputeJobs;
    }

    public void setScaleTestComputeJobs(List scaleTestComputeJobs) {
        this.scaleTestComputeJobs = scaleTestComputeJobs;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }   

    public String getBPBlock() {
        return BPBlock;
    }

    public void setBPBlock(String BPBlock) {
        this.BPBlock = BPBlock;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public long getTestID() {
        return testID;
    }

    public void setTestID(long testID) {
        this.testID = testID;
    }

    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public boolean isNotified() {
        return notified;
    }

    public void setNotified(boolean notified) {
        this.notified = notified;
    }

    public int getPercentageDone() {
        int numScaleTestComputeJobs = scaleTestComputeJobs.size();
        // count number of results ready
        int numJobsFinished = 0;
        for (Iterator i = scaleTestComputeJobs.iterator();i.hasNext();) {
            ScaleTestComputeJob scaleTestComputeJob = (ScaleTestComputeJob)i.next();
            if (scaleTestComputeJob.getStatus()=='S') {
                numJobsFinished++;
            }
        }
        if (numJobsFinished>0) {
            return (int)(((double)numJobsFinished / (double)numScaleTestComputeJobs) * 100);
        } else {
            return 0;
        }
    }
    
}
