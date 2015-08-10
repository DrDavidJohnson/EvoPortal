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
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import uk.ac.rdg.evoportal.data.OutputFile;
import uk.ac.rdg.evoportal.data.OutputFilesDataProvider;

/**
 *
 * @author david
 */
public class ScaleTestComputeJobOutputFilesDataView extends DataView {

    private int jobID;
    private long testID;

    public ScaleTestComputeJobOutputFilesDataView(String id, OutputFilesDataProvider dataProvider, int jobID, long testID) {
        super(id, dataProvider);
        this.jobID = jobID;
        this.testID = testID;
    }

    @Override
    protected void populateItem(Item item) {
        final OutputFile jobFile = (OutputFile)item.getModelObject();
        File file = jobFile.getFile();
        final DownloadLink downloadLink = new DownloadLink("fileLink", file);
        downloadLink.add(new Label("fileName", file.getName()));
        item.add(downloadLink);
        // if it's a parameters file, add taillink, not downloadlink
        String fileName = file.getName();
        int dotPos = fileName.lastIndexOf(".");
        String extension = fileName.substring(dotPos);
        final TailLink tailLink = new TailLink("tailLink", file, jobID, testID);
        tailLink.add(new Label("tailLabel", "(tail)"));
        tailLink.setVisible(false);
        if (".parameters".equalsIgnoreCase(extension)) {
            tailLink.setVisible(true);
        }
        item.add(tailLink);
    }

    private class TailLink extends Link {
        private File file;
        private int jobID;
        private long testID;

        public TailLink(String id, File file, int jobID, long testID) {
            super(id);
            this.file = file;
            this.jobID = jobID;
            this.testID = testID;
        }

        @Override
        public void onClick() {
            setResponsePage(new ScaleTestComputeJobOutputTailPage(jobID, testID, file));
        }
    }
}
