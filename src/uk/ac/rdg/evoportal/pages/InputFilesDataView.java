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
import java.text.DateFormat;
import java.util.Date;
import java.util.Iterator;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.DownloadLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.biojava.bio.seq.io.ParseException;
import org.biojavax.bio.phylo.io.nexus.DataBlock;
import org.biojavax.bio.phylo.io.nexus.NexusBlock;
import org.biojavax.bio.phylo.io.nexus.NexusFile;
import org.biojavax.bio.phylo.io.nexus.NexusFileBuilder;
import org.biojavax.bio.phylo.io.nexus.NexusFileFormat;
import uk.ac.rdg.evoportal.LoginSession;
import uk.ac.rdg.evoportal.data.InputFile;
import uk.ac.rdg.evoportal.data.InputFilesDataProvider;
import uk.ac.rdg.evoportal.tasks.FileDeleteTask;
import uk.ac.rdg.evoportal.tasks.RsyncTask;

/**
 *
 * @author david
 */
public class InputFilesDataView extends DataView {

    private transient Logger LOG = Logger.getLogger(InputFilesDataView.class.getName());

    public InputFilesDataView(String id, InputFilesDataProvider dataProvider) {
        super(id, dataProvider);
    }

    @Override
    protected void populateItem(Item item) {
        final InputFile inputFile = (InputFile)item.getModelObject();
        File file = inputFile.getFile();
        item.add(new Label("lastModified", DateFormat.getDateInstance(DateFormat.MEDIUM).format(new Date(file.lastModified()))));
        final DownloadLink downloadLink = new DownloadLink("fileLink", file);
        downloadLink.add(new Label("fileName", file.getName()));
        item.add(downloadLink);
        String fileStats = "";
        NexusFileBuilder builder = new NexusFileBuilder();
        try {
            NexusFileFormat.parseFile(builder, file);
            NexusFile parsedFile = builder.getNexusFile();
            String current_block_name;
            DataBlock dataBlock = null;
            for (Iterator i = parsedFile.blockIterator(); i.hasNext();) {
                  NexusBlock block = (NexusBlock)i.next();
                  current_block_name = block.getBlockName();
                  if(current_block_name.equalsIgnoreCase("DATA")){
                       dataBlock = (DataBlock)block;
                  }
            }
            fileStats = "datatype=" + dataBlock.getDataType() + "; ntaxa=" + dataBlock.getDimensionsNTax() + "; nchars=" + dataBlock.getDimensionsNChar();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (ParseException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        item.add(new Label("nexusDataStats", fileStats));
        item.add(new Label("fileSize", (file.length()/1024) + " kilobytes"));
        item.add(new DeleteLink("deleteLink", ((LoginSession)getSession()).getUsername(), file.getName()));
    }

    class DeleteLink extends Link {

        String user;
        String fileName;

        public DeleteLink(String id, String user, String fileName) {
            super(id);
            this.user = user;
            this.fileName = fileName;
        }

        @Override
        public void onClick() {
            new FileDeleteTask(user, fileName).run();
            new RsyncTask().run();
            setResponsePage(MyNexusFiles.class);
        }
    }

}