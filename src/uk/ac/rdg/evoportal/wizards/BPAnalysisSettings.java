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

import java.util.Random;
import org.apache.wicket.IClusterable;

/**
 *
 * @author david
 */
public class BPAnalysisSettings implements IClusterable {

    private BPModel model = BPModel.GTR;
    private BPBaseFrequencyType baseFrequencies = BPBaseFrequencyType.estimate;
    private BPRateVariationType rateVariation = BPRateVariationType.None;
    private int rateVariationCategories = 2;
    private int patterns = 1; // GTR patterns
    private boolean patternsReversibleJump = false;
    private int branchLengthSets = 1;
    private boolean branchLengthSetsReversibleJump = false;
    private int topologies = 1;
    private boolean covarionOn = false;
    private int covarionValue = 2;
    private boolean invariantSitesOn = false;
    private boolean coolingOn = false;
    private int coolingPos = 0;
    private double coolingNeg = 0.0;
    private String outGroup = "";
    private String root = "";
    private int seed = new Random().nextInt(Integer.MAX_VALUE);
    private String other = "";

    private int printFrequency = 1000;
    private int iterations = 10000;

    private String fileName = "";

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }
    
    public int getIterations() {
        return iterations;
    }

    public void setIterations(int iterations) {
        this.iterations = iterations;
    }

    public int getPrintFrequency() {
        return printFrequency;
    }

    public void setPrintFrequency(int printFrequency) {
        this.printFrequency = printFrequency;
    }

    public boolean isBranchLengthSetsReversibleJump() {
        return branchLengthSetsReversibleJump;
    }

    public void setBranchLengthSetsReversibleJump(boolean branchLengthSetsReversibleJump) {
        this.branchLengthSetsReversibleJump = branchLengthSetsReversibleJump;
    }

    public boolean isCoolingOn() {
        return coolingOn;
    }

    public void setCoolingOn(boolean coolingOn) {
        this.coolingOn = coolingOn;
    }

    public boolean isCovarionOn() {
        return covarionOn;
    }

    public void setCovarionOn(boolean covarionOn) {
        this.covarionOn = covarionOn;
    }

    public boolean isInvariantSitesOn() {
        return invariantSitesOn;
    }

    public void setInvariantSitesOn(boolean invariantSitesOn) {
        this.invariantSitesOn = invariantSitesOn;
    }

    public boolean isPatternsReversibleJump() {
        return patternsReversibleJump;
    }

    public void setPatternsReversibleJump(boolean patternsReversibleJump) {
        this.patternsReversibleJump = patternsReversibleJump;
    }

    public double getCoolingNeg() {
        return coolingNeg;
    }

    public void setCoolingNeg(double coolingNeg) {
        this.coolingNeg = coolingNeg;
    }

    public int getCoolingPos() {
        return coolingPos;
    }

    public void setCoolingPos(int coolingPos) {
        this.coolingPos = coolingPos;
    }

    public int getCovarionValue() {
        return covarionValue;
    }

    public void setCovarionValue(int covarionValue) {
        this.covarionValue = covarionValue;
    }

    public String getOther() {
        return other;
    }

    public void setOther(String other) {
        if (other==null) {
            other = "";
        }
        this.other = other;
    }

    public String getOutGroup() {
        return outGroup;
    }

    public void setOutGroup(String outGroup) {
        if (outGroup==null) {
            outGroup = "";
        }
        this.outGroup = outGroup;
    }

    public String getRoot() {
        return root;
    }

    public void setRoot(String root) {
        if (root==null) {
            root = "";
        }
        this.root = root;
    }

    public int getSeed() {
        return seed;
    }

    public void setSeed(int seed) {
        this.seed = seed;
    }

    public int getTopologies() {
        return topologies;
    }

    public void setTopologies(int topologies) {
        this.topologies = topologies;
    }

    public int getBranchLengthSets() {
        return branchLengthSets;
    }

    public void setBranchLengthSets(int branchLengthSets) {
        this.branchLengthSets = branchLengthSets;
    }

    public int getPatterns() {
        return patterns;
    }

    public void setPatterns(int patterns) {
        this.patterns = patterns;
    }

    public BPBaseFrequencyType getBaseFrequencies() {
        return baseFrequencies;
    }

    public void setBaseFrequencies(BPBaseFrequencyType baseFrequencies) {
        this.baseFrequencies = baseFrequencies;
    }

    public BPModel getModel() {
        return model;
    }

    public void setModel(BPModel model) {
        this.model = model;
    }

    public BPRateVariationType getRateVariation() {
        return rateVariation;
    }

    public void setRateVariation(BPRateVariationType rateVariation) {
        this.rateVariation = rateVariation;
    }

    public int getRateVariationCategories() {
        return rateVariationCategories;
    }

    public void setRateVariationCategories(int rateVariationCategories) {
        this.rateVariationCategories = rateVariationCategories;
    }

    public void setControlBlock(String controlBlock) {
        // do nothing
    }

    public String getControlBlock() {
        String o = "";
        o+="Begin BayesPhylogenies;\n";
        o+="\tModel " + model.name() + "\n";
        o+="\tBaseFreqs " + baseFrequencies.name() + "\n";
        switch (rateVariation) {
            case None: break;
            case Beta: o+="\tBeta " + rateVariationCategories + "\n";
            case Gamma: o+="\tGamma " + rateVariationCategories + "\n";
        }
        if (BPModel.GTR.equals(model)) {
            if (!patternsReversibleJump)
                o+="\tPatterns " + patterns + "\n";
            else
                o+="\tRJPA\n";
        }
        if (!branchLengthSetsReversibleJump)
            o+="\tBLS " + branchLengthSets + "\n";
        else
            o+="\tRJBLS " + branchLengthSets + "\n";
        o+="\tTops " + topologies + "\n";
        if (covarionOn)
            o+="\tCovarion " + covarionValue + "\n";
        if (invariantSitesOn)
            o+="\tInvariant\n";
        o+="\tPrintFreq " + printFrequency + "\n";    // these are defaults
        o+="\tIterations " + iterations + "\n";     // these are defaults
        if (coolingOn) {
            o+="\tCooling " + coolingPos + " " + coolingNeg + "\n";
        }
        if (outGroup.trim().length()>0)
            o+="\tOutgroup " + outGroup + "\n";
        if (root.trim().length()>0)
            o+="\tRoot " + root.replace('\n', ' ') + "\n";
        o+="\tSeed " + seed + "\n";
        o+="\tAutoRun\n";
        o+="End;";
        return o;
    }

}
