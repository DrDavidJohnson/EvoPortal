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
import uk.ac.rdg.evoportal.beans.ComputeJob;
import uk.ac.rdg.pbsclient.PBSNoPassClient;
import uk.ac.rdg.util.HibernateUtil;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author david
 */
public class ComputeJobSubmitTask extends TimerTask {

    private String HOST = GlobalConstants.getProperty("pbsnode.host");
    private String USER = GlobalConstants.getProperty("pbsnode.username");
    private String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");
    private File tmpNexusFile;
    private String submittingUser;
    private String jobName;
    private int walltime;
    private int nodes;
    private int ppn;
    private String realFileName;

    private transient Logger LOG = Logger.getLogger(ComputeJobSubmitTask.class.getName());

    public ComputeJobSubmitTask(String submittingUser, String jobName, int walltime, int nodes, int ppn, File tmpNexusFile, String realFileName) {
        this.submittingUser = submittingUser;
        this.jobName = jobName;
        this.walltime = walltime;
        this.nodes = nodes;
        this.ppn = ppn;
        this.tmpNexusFile = tmpNexusFile; // remember this is still LOCAL
        this.realFileName = realFileName;
    }

    public void run() {
        LOG.fine("ComputeJobSubmitTask starting");
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
                Session s = HibernateUtil.getSessionFactory().openSession();
                Transaction tx = s.beginTransaction();
                ComputeJob computeJob = new ComputeJob();
                computeJob.setJobID(jobID);
                computeJob.setLabel(jobName);
                computeJob.setSubmitTime(System.currentTimeMillis());
                computeJob.setOwner(submittingUser);
                computeJob.setStatus('!');
                computeJob.setNodes(nodes);
                computeJob.setTimeRequested(mpiBPJob.getWalltimeHours()*60L*60L*1000L);
                computeJob.setTimeUsed(0);
                s.persist(computeJob);
                tx.commit();
                if (!tx.wasCommitted()) {
                    // TODO implement rollback and notify user
                    throw new IOException("Could not save job to DB");
                }
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        LOG.fine("ComputeJobSubmitTask finished");
    }

}
