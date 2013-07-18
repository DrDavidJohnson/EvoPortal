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
import java.io.InputStream;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import uk.ac.rdg.evoportal.GlobalConstants;

/**
 *
 * @author david
 */
public class RsyncTask extends TimerTask {

    private String USER = GlobalConstants.getProperty("pbsnode.username");
    private String HOST  = GlobalConstants.getProperty("pbsnode.host");
    private String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");
    private String LOCAL_FILE_ROOT = GlobalConstants.getProperty("local.fileroot");
    private transient Logger LOG = Logger.getLogger(RsyncTask.class.getName());

    public void run() {
        LOG.fine("RsyncTask starting");
        String[] cmd = null;
        try {
            /*
             * This code executes rsync on local shell. Depends on package passwordless ssh to PBS host
             * change to -rv for verbose mode
             */
            cmd = new String[]{"rsync", "-r", "--delete-during", "--size-only", USER + "@" + HOST + ":" + REMOTE_FILE_ROOT, LOCAL_FILE_ROOT};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();
            InputStream err = p.getErrorStream();
            while(err.read()>0) {} // throw away error output?
            int val = p.waitFor();
            if (val != 0) {
                throw new IOException("Exception during RSync; return code = " + val);
            }
        } catch (InterruptedException ex) {
            LOG.severe("Problem executing: " + cmd[0]);
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOG.severe("Problem executing: " + cmd[0]);
            LOG.log(Level.SEVERE, ex.getMessage());
        }
        LOG.fine("RsyncTask finished");
    }
}