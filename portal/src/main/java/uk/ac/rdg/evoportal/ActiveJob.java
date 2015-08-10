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

package uk.ac.rdg.evoportal;

/**
 *
 * @author david
 */
public class ActiveJob {

    int jobID;
    char status;
    long timeUsed; // in millis

    public ActiveJob(int jobID, char status, long timeUsed) {
        this.jobID = jobID;
        this.status = status;
        this.timeUsed = timeUsed;
    }

    public int getJobID() {
        return jobID;
    }

    public char getStatus() {
        return status;
    }

    public long getTimeUsed() {
        return timeUsed;
    }

}