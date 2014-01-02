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

package uk.ac.rdg.evoportal;

/**
 *
 * @author david
 */
public class MpiBayesPhyloJob {

    String jobName = "";
    int walltimeHours = 0;
    int numNodes = 1;
    int ppn = 4;
    String user;
    private String realFileName;
    private String tmpFileName;
    private String REMOTE_FILE_ROOT = GlobalConstants.getProperty("remote.fileroot");

    public MpiBayesPhyloJob(String jobName, String user, int walltimeHours, int numNodes, int ppn, String tmpFileName, String realFileName) {
        this.jobName = jobName;
        this.walltimeHours = walltimeHours;
        this.numNodes = numNodes;
        this.ppn = ppn;
        this.tmpFileName = tmpFileName;
        this.user = user;
        this.realFileName = realFileName;
    }

    public String getRealFileName() {
        return realFileName;
    }

    public String getTmpFileName() {
        return tmpFileName;
    }

    public String getJobName() {
        return jobName;
    }

    public int getNumNodes() {
        return numNodes;
    }

    public int getWalltimeHours() {
        return walltimeHours;
    }

    public String toSubmitScript() {
        String script =
                "#!/bin/sh\n" +
                "# This auto-generated script runs MPI Bayes Phylogenies \n" +
                "#PBS -S /bin/sh\n" +
                "#PBS -N " + this.jobName + "\n" +
                "#PBS -l walltime=" + this.walltimeHours + ":00:00\n" +
                "#PBS -l nodes=" + this.numNodes + ":ppn=" + this.ppn + "\n" +
                "#PBS -q auto\n" +
                "PBS_JOBID=`echo $PBS_JOBID | cut -d . -f 1`\n" +
                "PORTALWD=\"" + REMOTE_FILE_ROOT + this.user + "/$PBS_JOBID\"\n" +
                "mkdir $PORTALWD\n" +
                "DataFile=\"$PORTALWD/" + this.realFileName + "\"\n" +
                "cp " + GlobalConstants.getProperty("remote.tmp") + this.tmpFileName + " $DataFile\n" +
//                "rm " + REMOTE_FILE_ROOT + this.user + "/" + tmpNexusFile.getName() + "\n" + // causes problems...
                "NN=`cat $PBS_NODEFILE | wc -l`\n" +
                "echo \"using $NN nodes:\"\n" +
                "BinName=\"" + REMOTE_FILE_ROOT + "bin/BayesPhylogenies-MPI\"\n" +
                "echo \"using binary: $BinName\"\n" +
                ". /etc/profile.d/modules.sh\n" +
                "module load openmpi/xl_9.0.0_64_mx\n" +
                "cat $PBS_NODEFILE\n" +
                "echo\n" +
                "pwd\n" +
                "date\n" +
                "echo\n" +
                "$MPIROOT/bin/mpirun -np $NN -machinefile $PBS_NODEFILE $BinName $DataFile\n" +
//                    "rm $BinName\n" +
                "echo\n" +
                "date\n";

        return script;
    }
}