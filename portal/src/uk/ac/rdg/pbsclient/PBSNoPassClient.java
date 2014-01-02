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

package uk.ac.rdg.pbsclient;

import uk.ac.rdg.evoportal.GlobalConstants;

import java.io.*;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author david
 */
public class PBSNoPassClient {

    private static String USER = GlobalConstants.getProperty("pbsnode.username");
    private static String HOST  = GlobalConstants.getProperty("pbsnode.host");
    private static transient Logger LOG = Logger.getLogger(PBSNoPassClient.class.getName());

    public static String qstatGrep(String key) throws IOException {
        LOG.fine("PBSNoPassClient.qstatGrep starting");
        String[] cmd = new String[]{"ssh", USER + "@" + HOST, "qstat | grep " + key};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = pb.start();
        InputStream is = p.getInputStream();
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(reader);
        String line;
        String result = "";
        while ((line = br.readLine()) != null) {
            result = result + line + "\n";
        }
        int exitValue = 0;
        try {
            exitValue = p.waitFor();
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        if (exitValue>0) {
            if (exitValue==1) {
                result = "";
            } else {
                throw new IOException("A problem occurred; exit value=" + exitValue);
            }
        }
        LOG.fine("PBSNoPassClient.qstatGrep finishing");
        return result;
    }

    public static String showqProcessorsActive() throws IOException {
        LOG.fine("PBSNoPassClient.showqProcessorsActive starting");
        String[] cmd = new String[]{"ssh", USER + "@" + HOST, "showq | grep \"Processors Active\""};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = pb.start();
        InputStream is = p.getInputStream();
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(reader);
        String line;
        String result = "";
        while ((line = br.readLine()) != null) {
            result = result + line + "\n";
        }
        int exitValue = 0;
        try {
            exitValue = p.waitFor();
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        if (exitValue>0) {
            throw new IOException("A problem occurred; exit value=" + exitValue);
        }
        LOG.fine("PBSNoPassClient.showqProcessorsActive finished");
        return result;
    }

    public static int qsub(String dir, File qsubScript) throws IOException {
        LOG.fine("PBSNoPassClient.qsub starting");

        // scp script
        String[] cmd = new String[]{"scp", qsubScript.getCanonicalPath(), USER + "@" + HOST + ":" + dir};
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

        // ssh qsub
        cmd = new String[]{"ssh", USER + "@" + HOST, "cd " + dir + "; qsub " + dir + "/" + qsubScript.getName() + ""};
        pb = new ProcessBuilder(cmd);
        p = pb.start();
        exitValue = 0;
        try {
            exitValue = p.waitFor();
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        if (exitValue>0) {
            throw new IOException("A problem occurred; exit value=" + exitValue);
        }
        InputStream is = p.getInputStream();
        InputStreamReader reader = new InputStreamReader(is);
        BufferedReader br = new BufferedReader(reader);
        String line;
        String result = "";
        while ((line = br.readLine()) != null) {
            result = result + line + "\n";
        }
        String[] lineParts = result.split("[.]");
        String jobIDStr = lineParts[0];
        int jobID = -1;
        jobID = Integer.parseInt(jobIDStr);


        // rm script "rm " + dir + "/" + qsubScript.getName()
        cmd = new String[]{"ssh", USER + "@" + HOST, "rm " + dir + "/" + qsubScript.getName()};
        pb = new ProcessBuilder(cmd);
        p = pb.start();
        exitValue = 0;
        try {
            exitValue = p.waitFor();
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        if (exitValue>0) {
            throw new IOException("A problem occurred; exit value=" + exitValue);
        }

        LOG.fine("PBSNoPassClient.qsub finished");
        return jobID;
    }

    public static boolean qdel(int jobID) throws IOException {
        LOG.fine("PBSNoPassClient.qdel finished");
        String[] cmd = new String[]{"ssh", USER + "@" + HOST, "qdel " + jobID};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p = pb.start();
        int exitValue = 0;
        try {
            exitValue = p.waitFor();
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        if (exitValue>0) {
            if (exitValue==153) {
               LOG.fine("Job " + jobID + " already deleted from PBS");
            } else {
                throw new IOException("A problem occurred; exit value=" + exitValue);
            }
        }
        LOG.fine("PBSNoPassClient.qdel finished");
        return true;
    }

}
