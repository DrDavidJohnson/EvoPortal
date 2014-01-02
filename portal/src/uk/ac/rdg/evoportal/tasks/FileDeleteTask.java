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

import uk.ac.rdg.evoportal.GlobalConstants;

import java.io.IOException;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;

/**
 *
 * @author david
 */
public class FileDeleteTask extends TimerTask {

    private String USER = GlobalConstants.getProperty("pbsnode.username");
    private String HOST  = GlobalConstants.getProperty("pbsnode.host");
    private String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");
    private String owner;
    private String fileName;
    private transient Logger LOG = Logger.getLogger(FileDeleteTask.class.getName());

    public FileDeleteTask(String owner, String fileName) {
        this.owner = owner;
        this.fileName = fileName;
    }

    public void run() {
        LOG.fine("FileDeleteTask starting");
        String[] cmd = new String[]{"ssh", USER + "@" + HOST,  "rm " + REMOTE_FILE_ROOT + owner + "/mynexusfiles/" + fileName};
        ProcessBuilder pb = new ProcessBuilder(cmd);
        Process p;
        try {
            p = pb.start();
            int exitValue = 0;
            try {
                exitValue = p.waitFor();
            } catch (InterruptedException ex) {
                LOG.log(Level.SEVERE, null, ex);
            }
            if (exitValue>0) {
                throw new IOException("A problem occurred; exit value=" + exitValue);
            }
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        LOG.fine("FileDeleteTask finished");
    }

}
