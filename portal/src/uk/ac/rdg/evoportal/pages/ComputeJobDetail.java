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
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.time.Duration;
import uk.ac.rdg.evoportal.GlobalConstants;
import uk.ac.rdg.evoportal.beans.ComputeJob;
import uk.ac.rdg.evoportal.data.ComputeJobsDataProvider;
import uk.ac.rdg.evoportal.data.OutputFilesDataProvider;
import uk.ac.rdg.util.TimeUtil;

/**
 *
 * @author david
 */
public final class ComputeJobDetail extends AuthenticatedBasePage {

    String LOCAL_FILE_ROOT = GlobalConstants.getProperty("local.fileroot");
    transient Logger LOG = Logger.getLogger(ComputeJobDetail.class.getName());

    public ComputeJobDetail(PageParameters params) {
        String idStr = params.getString("jobID");
        final int jobID = Integer.parseInt(idStr);
        final ComputeJob job = ComputeJobsDataProvider.get(jobID);
        add(new Label("id", idStr));
        add(new Label("name", job.getLabel()));
        add(new Label("submitted", new Date(job.getSubmitTime()).toString()));
        add(new Label("status", GlobalConstants.getStatusMsg(job.getStatus())));
        add(new Label("nodes", ""+job.getNodes()));
        add(new Label("processors", ""+(job.getNodes()*4)));  // calc processors
        add(new Label("timeRequested", TimeUtil.millisToLongDHMS(job.getTimeRequested()))); // currently in millis
        add(new Label("timeUsed", TimeUtil.millisToLongDHMS(job.getTimeUsed()))); // currently in millis

        // other outputs
        final String owner = job.getOwner();
        String dirToZipFileString = GlobalConstants.getProperty("local.fileroot") + owner + "/" + jobID;
        File dirToZipFile = new File(dirToZipFileString);
        if (dirToZipFile!=null && dirToZipFile.exists()) {
            String dirToZip = dirToZipFile.getPath();
            File zipFile = new File(jobID + ".zip");
            ZipFileLink zipLink = new ZipFileLink("zipLink", zipFile, dirToZip);
            add(zipLink);
        } else {
            add(new Link("zipLink") {
                @Override
                public void onClick() {
                    //
                }
            }.setEnabled(false));
        }
        OutputFilesDataProvider outputFilesProvider = new OutputFilesDataProvider(owner, jobID);
        OutputFilesDataView outputFilesDataView = new OutputFilesDataView("outputFiles", outputFilesProvider, jobID);
        add(outputFilesDataView);
        setOutputMarkupId(true);
        add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(15)) {
                    @Override
                    protected void onPostProcessTarget(AjaxRequestTarget target) {
                        super.onPostProcessTarget(target);
                        if('S'!=job.getStatus()) {
                            ComputeJob refreshedJob = ComputeJobsDataProvider.get(jobID);
                            get("status").replaceWith(new Label("status", GlobalConstants.getStatusMsg(refreshedJob.getStatus())));
                            get("timeUsed").replaceWith(new Label("timeUsed", Long.toString(refreshedJob.getTimeUsed())));
                            OutputFilesDataProvider outputFilesProvider = new OutputFilesDataProvider(owner, jobID);
                            get("outputFiles").replaceWith(new OutputFilesDataView("outputFiles", outputFilesProvider, jobID));
                            String dirToZipFileString = GlobalConstants.getProperty("local.fileroot") + owner + "/" + jobID;
                            File dirToZipFile = new File(dirToZipFileString);
                            if (dirToZipFile!=null) {
                                String dirToZip = dirToZipFile.getPath();
                                File zipFile = new File(jobID + ".zip");
                                get("zipLink").replaceWith(new ZipFileLink("zipLink", zipFile, dirToZip)
                                            .setDeleteAfterDownload(true));
                            } else {
                                get("zipLink").replaceWith(new Link("zipLink") {
                                    @Override
                                    public void onClick() {
                                        //
                                    }
                                }.setEnabled(false));
                            }

                        }
                    }
                });
    }

    public ComputeJobDetail() {
        setResponsePage(ComputeJobs.class);
    }

    private class ZipFileLink extends DownloadLink {

        File zipFile;
        String dirToZip;
        transient Logger LOG = Logger.getLogger(ZipFileLink.class.getName());

        public ZipFileLink(String id, File zipFile, String dirToZip) {
            super(id, zipFile);
            ZipOutputStream zos = null;
            try {
                zos = new ZipOutputStream(new FileOutputStream(zipFile));
                zipDir(dirToZip, zos);
            } catch (IOException ex) {
                LOG.log(Level.SEVERE, null, ex);
            } finally {
                try {
                    zos.close();
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            }
        }

        private void zipDir(String dir2zip, ZipOutputStream zos) throws IOException
        {
            File zipDir = new File(dir2zip);
            String[] dirList = zipDir.list();
            byte[] readBuffer = new byte[2048];
            int bytesIn = 0;
            for(int i=0; i<dirList.length; i++) {
                File f = new File(zipDir, dirList[i]);
                if(f.isDirectory()) {
                    String filePath = f.getPath();
                    zipDir(filePath, zos);
                    continue;
                }
                FileInputStream fis = new FileInputStream(f);
                ZipEntry anEntry = new ZipEntry(f.getName());
                zos.putNextEntry(anEntry);
                while((bytesIn = fis.read(readBuffer)) != -1) {
                    zos.write(readBuffer, 0, bytesIn);
                }
               //close the Stream
               fis.close();
            }
        }
    }

}