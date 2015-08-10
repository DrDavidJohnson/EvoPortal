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

package uk.ac.rdg.evoportal.wizards;

import org.apache.wicket.IClusterable;

/**
 *
 * @author david
 */
public class BPScaleTestSettings implements IClusterable {
    private String name = "";
    private int repetitions = 1;
    private int scaleNodesMin = 10;
    private int scaleNodesIncrements = 10;
    private int scaleNodesMax = 100;
    private int ppn = 4;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPpn() {
        return ppn;
    }

    public void setPpn(int ppn) {
        this.ppn = ppn;
    }

    public int getRepetitions() {
        return repetitions;
    }

    public void setRepetitions(int repetitions) {
        this.repetitions = repetitions;
    }

    public int getScaleNodesIncrements() {
        return scaleNodesIncrements;
    }

    public void setScaleNodesIncrements(int scaleNodesIncrements) {
        this.scaleNodesIncrements = scaleNodesIncrements;
    }

    public int getScaleNodesMax() {
        return scaleNodesMax;
    }

    public void setScaleNodesMax(int scaleNodesMax) {
        this.scaleNodesMax = scaleNodesMax;
    }

    public int getScaleNodesMin() {
        return scaleNodesMin;
    }

    public void setScaleNodesMin(int scaleNodesMin) {
        this.scaleNodesMin = scaleNodesMin;
    }
}
