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

import org.hibernate.Session;
import org.hibernate.Transaction;
import uk.ac.rdg.evoportal.GlobalConstants;
import uk.ac.rdg.evoportal.MpiBayesPhyloJob;
import uk.ac.rdg.evoportal.beans.ScaleTest;
import uk.ac.rdg.evoportal.beans.ScaleTestComputeJob;
import uk.ac.rdg.pbsclient.PBSNoPassClient;
import uk.ac.rdg.util.HibernateUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.List;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author david
 */
public class ScaleTestSubmitTask extends TimerTask {

    private String HOST = GlobalConstants.getProperty("pbsnode.host");
    private String USER = GlobalConstants.getProperty("pbsnode.username");;
    private String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");
    private File tmpNexusFile;
    private String submittingUser;
    private String testName;
    private int iterations;
    private int minNodes;
    private int maxNodes;
    private int increments;
    private int reps;
    String realFileName;
    String controlBlock;
    Session s;
    Transaction tx;

    transient Logger LOG = Logger.getLogger(ScaleTestSubmitTask.class.getName());

    public ScaleTestSubmitTask(File tmpNexusFile, String submittingUser, String testName, int iterations, int minNodes, int maxNodes, int increments, int reps, String realFileName, String controlBlock) {
        this.tmpNexusFile = tmpNexusFile;
        this.submittingUser = submittingUser;
        this.testName = testName;
        this.iterations = iterations;
        this.minNodes = minNodes;
        this.maxNodes = maxNodes;
        this.increments = increments;
        this.reps = reps;
        this.realFileName = realFileName;
        this.controlBlock = controlBlock;
    }

    @Override
    public void run() {
        LOG.fine("ScaleTestSubmitTask starting");
        s = HibernateUtil.getSessionFactory().openSession();
        for (int i=1;i<=reps;i++) {
            ScaleTest scaleTest = new ScaleTest();
            scaleTest.setTestID(System.currentTimeMillis());
            scaleTest.setOwner(submittingUser);
            if (reps>1) {
                scaleTest.setLabel(testName + "_rep-" + i);
            } else {
                scaleTest.setLabel(testName);
            }
            scaleTest.setBPBlock(controlBlock);
            scaleTest.setIterations(iterations);
            List<ScaleTestComputeJob> jobsList = scaleTest.getScaleTestComputeJobs();
            // TODO loop for test jobs here
            for (int j=minNodes;j<=maxNodes;j+=increments) {
                ScaleTestComputeJob job = submitScaleTestComputeJob(submittingUser, testName, 1, j, 4, tmpNexusFile, realFileName);
                if (job!=null) {
                    tx = s.beginTransaction();
                    s.persist(job);
                    jobsList.add(job);
                } else {
                    LOG.severe("ScaleTestSubmitTask.submitScaleTestComputeJob returned null");
                }
            }
            scaleTest.setScaleTestComputeJobs(jobsList);
            s.persist(scaleTest);
            tx.commit();
            if (!tx.wasCommitted()) {
                LOG.severe("Could not submit scale test to DB");
            }
        }
        s.close();
        LOG.fine("ScaleTestSubmitTask finished");
    }

    private ScaleTestComputeJob submitScaleTestComputeJob(String submittingUser, String jobName, int walltime, int nodes, int ppn, File tmpNexusFile, String realFileName) {
        LOG.fine("ScaleTestSubmitTask.submitScaleTestComputeJob starting");
        ScaleTestComputeJob scaleTestComputeJob = null;
        try {
            // scp nexus file
            String[] cmd = new String[]{"scp", tmpNexusFile.getCanonicalPath(), USER + "@" + HOST + ":" + REMOTE_FILE_ROOT + this.submittingUser + "/"};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();
            int exitValue = 0;
            try {
                exitValue = p.waitFor();
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            if (exitValue>0) {
                throw new IOException("A problem occurred; exit value=" + exitValue);
            }
            /*
             * Build a submit script, can use a tmp file name
             */
            int jobID = -1;
            MpiBayesPhyloJob mpiBPJob = null;
            File tmpScriptFile = File.createTempFile("mpiBPRun_" + submittingUser + ".sh", ".tmp");
            // write the script file here
            mpiBPJob = new MpiBayesPhyloJob(jobName, submittingUser, walltime, nodes, ppn, tmpNexusFile.getName(), realFileName);
            String mpiBPScript = mpiBPJob.toSubmitScript();
            BufferedWriter out;
            out = new BufferedWriter(new FileWriter(tmpScriptFile));
            out.write(mpiBPScript);
            out.flush();
            out.close();
            /*
             * Submit the script to PBS
             */
            jobID = PBSNoPassClient.qsub(REMOTE_FILE_ROOT + submittingUser + "/", tmpScriptFile);
            if (!tmpScriptFile.delete()) {
                throw new IOException("Could not delete tmp script: " + tmpScriptFile.getCanonicalPath());
            }
            /*
             * If successfully submitted to PBS, clean up local tmp and add to DB
             */
            if (jobID>0 && mpiBPJob!=null) {
                LOG.fine("Job " + jobID + " submitted to PBS");
                // add to DB
                tx = s.beginTransaction();
                scaleTestComputeJob = new ScaleTestComputeJob();
                scaleTestComputeJob.setJobID(jobID);
                scaleTestComputeJob.setLabel(jobName);
                scaleTestComputeJob.setSubmitTime(System.currentTimeMillis());
                scaleTestComputeJob.setOwner(submittingUser);
                scaleTestComputeJob.setStatus('!');
                scaleTestComputeJob.setNodes(nodes);
                scaleTestComputeJob.setTimeRequested(mpiBPJob.getWalltimeHours()*60L*60L*1000L);
                scaleTestComputeJob.setTimeUsed(0);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        LOG.fine("ScaleTestSubmitTask.submitScaleTestComputeJob finished");
        return scaleTestComputeJob;
    }

}
