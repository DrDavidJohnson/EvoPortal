/*
 *  Copyright 2009-2010 David Johnson, School of Biological Sciences,
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


/**
 *
 * @author david
 */
public class ScaleTestComputeJob {

    private int jobID;
    private String label;
    private int nodes;
    private long submitTime; // in millis
    private char status;
    private long timeRequested; // in millis
    private long timeUsed; // in millis
    private String owner;
    private long id;
    private int duration; // in millis
    private long scaletest_id;

    public ScaleTestComputeJob(int jobID, String label, int nodes, long submitTime, char status, long timeRequested, long timeUsed, String owner, long id, int duration, long scaletest_id) {
        this.jobID = jobID;
        this.label = label;
        this.nodes = nodes;
        this.submitTime = submitTime;
        this.status = status;
        this.timeRequested = timeRequested;
        this.timeUsed = timeUsed;
        this.owner = owner;
        this.id = id;
        this.duration = duration;
        this.scaletest_id = scaletest_id;
    }

    public ScaleTestComputeJob() {
        this.jobID = -1;
        this.label = "";
        this.nodes = 0;
        this.submitTime = -1L;
        this.status = 'S';
        this.timeRequested = -1L;
        this.timeUsed = -1L;
        this.owner = "";
        this.id = -1;
        this.duration = -1;
        this.scaletest_id = -1;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public int getJobID() {
        return jobID;
    }

    public void setJobID(int jobID) {
        this.jobID = jobID;
    }

    public String getLabel() {
        return label;
    }

    public void setLabel(String label) {
        this.label = label;
    }

    public int getNodes() {
        return nodes;
    }

    public void setNodes(int nodes) {
        this.nodes = nodes;
    }

    public String getOwner() {
        return owner;
    }

    public void setOwner(String owner) {
        this.owner = owner;
    }

    public char getStatus() {
        return status;
    }

    public void setStatus(char status) {
        this.status = status;
    }

    public long getSubmitTime() {
        return submitTime;
    }

    public void setSubmitTime(long submitTime) {
        this.submitTime = submitTime;
    }

    public long getTimeRequested() {
        return timeRequested;
    }

    public void setTimeRequested(long timeRequested) {
        this.timeRequested = timeRequested;
    }

    public long getTimeUsed() {
        return timeUsed;
    }

    public void setTimeUsed(long timeUsed) {
        this.timeUsed = timeUsed;
    }

    public int getDuration() {
        return duration;
    }

    public void setDuration(int duration) {
        this.duration = duration;
    }

    public long getScaletest_id() {
        return scaletest_id;
    }

    public void setScaletest_id(long scaletest_id) {
        this.scaletest_id = scaletest_id;
    }

}
