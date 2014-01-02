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

package uk.ac.rdg.util;

import org.biojava.bio.seq.io.ParseException;
import org.biojavax.bio.phylo.io.nexus.*;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;

/**
 *
 * @author david
 */
public class NexusUtil {

    public static void parseFile(File f) throws IOException, ParseException {
        NexusFileBuilder builder = new NexusFileBuilder();
        NexusFileFormat.parseFile(builder, f); // throws exception is parse error
    }

    public static void parseInputStream(InputStream is) throws IOException, ParseException {
        NexusFileBuilder builder = new NexusFileBuilder();
        NexusFileFormat.parseInputStream(builder, is);
    }

    public static void cleanNexusDataFile(File f) throws IOException, ParseException {
        NexusFileBuilder builder = new NexusFileBuilder();
        NexusFileFormat.parseFile(builder, f);
        NexusFile parsedFile = builder.getNexusFile();
        String current_block_name;
        for (Iterator i = parsedFile.blockIterator(); i.hasNext();) {
              NexusBlock block = (NexusBlock)i.next();
              current_block_name = block.getBlockName();
              if(!current_block_name.equalsIgnoreCase("data")){
                   parsedFile.removeObject(block);
              }
        }
        NexusFileFormat.writeFile(f, parsedFile);
    }

    public static void cleanNexusDataInputStream(InputStream is, OutputStream os) throws IOException, ParseException {
        NexusFileBuilder builder = new NexusFileBuilder();
        NexusFileFormat.parseInputStream(builder, is);
        NexusFile parsedFile = builder.getNexusFile();
        String current_block_name;
        for (Iterator i = parsedFile.blockIterator(); i.hasNext();) {
              NexusBlock block = (NexusBlock)i.next();
              current_block_name = block.getBlockName();
              if(!current_block_name.equalsIgnoreCase("data")){
                   parsedFile.removeObject(block);
              }
        }
        NexusFileFormat.writeStream(os, parsedFile);
    }
    
    public static List getMatrixLabels(File f) throws IOException, ParseException {
        NexusFileBuilder builder = new NexusFileBuilder();
        NexusFileFormat.parseFile(builder, f);
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
        Collection matrixLabels = dataBlock.getMatrixLabels();
        Iterator i = matrixLabels.iterator();
        return Arrays.asList(matrixLabels.toArray());
    }

}
