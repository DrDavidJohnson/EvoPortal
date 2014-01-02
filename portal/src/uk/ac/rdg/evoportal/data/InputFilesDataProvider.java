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

package uk.ac.rdg.evoportal.data;

import org.apache.wicket.markup.repeater.data.IDataProvider;
import org.apache.wicket.model.IModel;
import uk.ac.rdg.evoportal.GlobalConstants;

import java.io.File;
import java.util.Iterator;
import java.util.List;
import java.util.Vector;

/**
 *
 * @author david
 */
public class InputFilesDataProvider implements IDataProvider {

    // jdbc Connection
    List<InputFile> l = new Vector<InputFile>();

    static String LOCAL_FILE_ROOT = GlobalConstants.getProperty("local.fileroot");

    public InputFilesDataProvider(String owner) {
        File f = new File(LOCAL_FILE_ROOT + owner + "/mynexusfiles/");
        if (f.isDirectory()) {
            File[] fileList = f.listFiles();
            for (int i=0;i<fileList.length;i++) {
                l.add(new InputFile(fileList[i]));
            }
        }
    }

    public List<String> listFiles() {
        List<String> filesList = new Vector();
        for(Iterator<InputFile> i = l.iterator();i.hasNext();) {
            filesList.add(i.next().getFile().getName());
        }
        return filesList;
    }

    public Iterator iterator(int first, int count) {
        return l.iterator();
    }

    public IModel model(Object object) {
        InputFile jobFile = (InputFile)object;
        return new InputFileLoadableDetachableModel(jobFile.getFile());
    }

    public int size() {
        return l.size();
    }

    public void detach() {
        //l = null;
    }

}
