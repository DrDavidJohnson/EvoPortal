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

/*
 * Dashboard.java
 *
 * Created on July 19, 2009, 4:41 PM
 */

package uk.ac.rdg.evoportal.pages;

import uk.ac.rdg.evoportal.LoginSession;
import uk.ac.rdg.evoportal.data.ComputeJobsDataProvider;

public class ComputeJobs extends AuthenticatedBasePage {

    public ComputeJobs() {
        super();
        LoginSession session = (LoginSession)getSession();
        if (session.getUsername()!=null) {
            ComputeJobsDataProvider computeJobsProvider = new ComputeJobsDataProvider(session.getUsername());
            if (computeJobsProvider.size()>0) {
                ComputeJobsDataView dashboardView = new ComputeJobsDataView("jobDetails", computeJobsProvider);
                add(dashboardView);
            } else {
                setResponsePage(NoComputeJobs.class);
            }
        }
}
}
