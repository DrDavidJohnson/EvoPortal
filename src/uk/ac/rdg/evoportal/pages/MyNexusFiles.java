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

package uk.ac.rdg.evoportal.pages;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.upload.FileUpload;
import org.apache.wicket.markup.html.form.upload.FileUploadField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.file.Files;
import org.apache.wicket.util.lang.Bytes;
import uk.ac.rdg.evoportal.GlobalConstants;
import uk.ac.rdg.evoportal.LoginSession;
import uk.ac.rdg.evoportal.data.InputFilesDataProvider;
import uk.ac.rdg.evoportal.tasks.RsyncTask;
import uk.ac.rdg.util.NexusUtil;

/**
 *
 * @author david
 */
public final class MyNexusFiles extends AuthenticatedBasePage {
    String owner = "";
    public MyNexusFiles() {
        super ();
        owner = ((LoginSession)getSession()).getUsername();
        final InputFilesDataProvider inputFilesProvider = new InputFilesDataProvider(owner);
        InputFilesDataView inputFilesDataView = new InputFilesDataView("inputFiles", inputFilesProvider);
        add(new UploadNexusForm("uploadNexusForm"));
        add(inputFilesDataView);
    }

    class UploadNexusForm extends Form {

        private FileUploadField fileUploadField;
        private String HOST = GlobalConstants.getProperty("pbsnode.host");
        private String USER = GlobalConstants.getProperty("pbsnode.username");
        private String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");
        private String LOCAL_FILE_ROOT = GlobalConstants.getProperty("local.fileroot");
        transient Logger LOG = Logger.getLogger(UploadNexusForm.class.getName());

        public UploadNexusForm(String id) {
            super(id);
            add(fileUploadField = new FileUploadField("fileInput",new Model<FileUpload>()));
            setMultiPart(true);
            setMaxSize(Bytes.kilobytes(Integer.parseInt(GlobalConstants.getProperty("upload.maxfilesize"))));
            add(new FeedbackPanel("feedback"));
        }

        @Override
        protected void onSubmit() {
            super.onSubmit();
            // first upload nexus file to server, append control block to file later
            final FileUpload upload = fileUploadField.getFileUpload();
            if(!upload.getClientFileName().toUpperCase().endsWith(".NEX")) {
                error("File does not end with .nex; please ensure you selected a valid Nexus data file");
                return;
            }
            File newFile = null;
            if (upload != null) {
                // Create a new file
                newFile = new File(LOCAL_FILE_ROOT+ "/" + owner + "/mynexusfiles/", upload.getClientFileName());
                // Check new file, delete if it already existed
                checkFileExists(newFile);
                try {
                    // Save to new file
                    newFile.createNewFile();
                    upload.writeTo(newFile);
                }
                catch (Exception e) {
                    throw new IllegalStateException("Unable to write file");
                }
            }
            try {
                NexusUtil.parseFile(newFile);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
                error(ex);
                return; // don't run to end of processing
            }
            try {
                NexusUtil.cleanNexusDataFile(newFile);
            } catch (Exception ex) {
                LOG.log(Level.SEVERE, null, ex);
                error(ex);
                return; // don't run to end of processing
            }

            // move to PBS node
            try {
                // scp nexus file
                String[] cmd = new String[]{"scp", newFile.getCanonicalPath(), USER + "@" + HOST + ":" + REMOTE_FILE_ROOT + "/" + owner + "/mynexusfiles"};
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
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
                error(ex);
            }
            if (newFile.delete()) {
                RsyncTask rsyncTask = new RsyncTask();
                rsyncTask.run();
            }
            setResponsePage(MyNexusFiles.class);
        }

        private void checkFileExists(File newFile) {
            if (newFile.exists()) {
                // Try to delete the file
                if (!Files.remove(newFile)){
                    throw new IllegalStateException("Unable to overwrite " + newFile.getAbsolutePath());
                }
            }
        }
    }

}

