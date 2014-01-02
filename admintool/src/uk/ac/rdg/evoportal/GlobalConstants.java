/*
 *  Copyright 2009-2010 David Johnson, School of Biological Sciences,
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

package uk.ac.rdg.evoportal;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;
import java.util.logging.Logger;

/**
 *
 * @author david
 */
public class GlobalConstants {

    public enum ModelType { GTNR, GTR, HKY85, F81, SYM, JC, K2P, M1P, M2P };
    public enum BaseFreqsType { uniform, estimate, empirical };
    public enum RateVarType { Beta, Gamma, None };
    public enum RunDurationType { iterations, hours, infinite };
    private static Properties props;
    static Logger LOG = Logger.getLogger(Main.class.getName());

    static {
        // load portal configuration
        InputStream is = Main.class.getResourceAsStream("/config.properties");
        props = new Properties();
        try {
            props.load(is);
        } catch (IOException ex) {
           LOG.severe(ex.getMessage());
        }
    }

    public static String getStatusMsg(char c) {
        String status = "Undefined";
            switch(c) {
                case '!': status = "Submitted to cluster";
                          break;
                case 'E': status = "Exiting after run";
                          break;
                case 'H': status = "Held";
                          break;
                case 'Q': status = "Queued";
                          break;
                case 'R': status = "Running";
                          break;
                case 'T': status = "Job is being moved";
                          break;
                case 'W': status = "Waiting for execution time";
                          break;
                case 'S': status = "Exited after run";
                          break;
            }
        return status;
    }

    public static String getProperty(String prop) {
        return props.getProperty(prop);
    }

}
