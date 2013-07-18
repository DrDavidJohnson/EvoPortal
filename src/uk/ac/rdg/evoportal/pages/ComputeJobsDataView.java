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

import java.util.Date;
import org.apache.wicket.Component;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.util.time.Duration;
import uk.ac.rdg.evoportal.GlobalConstants;
import uk.ac.rdg.evoportal.beans.ComputeJob;
import uk.ac.rdg.evoportal.data.ComputeJobsDataProvider;
import uk.ac.rdg.evoportal.tasks.ComputeJobDeleteTask;
import uk.ac.rdg.evoportal.tasks.ComputeJobStopTask;
import uk.ac.rdg.util.TimeUtil;


/**
 *
 * @author david
 */
public class ComputeJobsDataView extends DataView {

    public ComputeJobsDataView(String id, ComputeJobsDataProvider dataProvider) {
        super(id, dataProvider);
    }

    @Override
    protected void populateItem(final Item item) {
        ComputeJob job = (ComputeJob)item.getModelObject();
        final int jobID = job.getJobID();
        final BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("details", ComputeJobDetail.class);
        link.setParameter("jobID", jobID);
        link.add(new Label("jobID", Integer.toString(jobID)));
        item.add(link);
        item.add(new Label("name", job.getLabel()));
        item.add(new Label("submitted", new Date(job.getSubmitTime()).toString()));
        char s = job.getStatus();
        String status = "Undefined";
        boolean isStopped = false;
        status = GlobalConstants.getStatusMsg(s);
        if (s=='S')
            isStopped = true;
        Label statusLabel = new Label("status", status);
        item.add(statusLabel);
        item.add(new Label("nodes", job.getNodes() + " (" + job.getNodes()*4 + ")"));
        item.add(new Label("timeRequested", TimeUtil.millisToLongDHMS(job.getTimeRequested())));
        item.add(new Label("timeUsed", TimeUtil.millisToLongDHMS(job.getTimeUsed())));
        StopOrDeleteLink stopLink = new StopOrDeleteLink("stopOrDeleteLink", isStopped, job.getOwner(), job.getJobID());
        String stopOrDeleteLabel = "Stop";
        if (isStopped) {
            stopOrDeleteLabel = "Delete";
        }
        stopLink.add(new Label("stopOrDelete", stopOrDeleteLabel));
        item.add(stopLink);
        if (!isStopped) {

        }
        item.setOutputMarkupId(true);
        AjaxSelfUpdatingTimerBehavior timer = new AjaxSelfUpdatingTimerBehavior(Duration.seconds(15)) {
            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                ComputeJob refreshedJob = ComputeJobsDataProvider.get(jobID);
                item.get("status").replaceWith(new Label("status", GlobalConstants.getStatusMsg(refreshedJob.getStatus())));
                item.get("timeUsed").replaceWith(new Label("timeUsed", TimeUtil.millisToLongDHMS(refreshedJob.getTimeUsed())));
                if (refreshedJob.getStatus()=='S') {
                    Component c = item.get("stopOrDeleteLink");
                    if (c instanceof StopOrDeleteLink) {
                        if (((StopOrDeleteLink)c).getJobID()==refreshedJob.getJobID()) {
                            StopOrDeleteLink stopLink = new StopOrDeleteLink("stopOrDeleteLink", true, refreshedJob.getOwner(), refreshedJob.getJobID());
                            stopLink.add(new Label("stopOrDelete", "Delete"));
                            c.replaceWith(stopLink);
                        }
                    }
                }

            }
        };
        item.add(timer);
    }

    private class StopOrDeleteLink extends Link {

        int jobID;
        String user;
        boolean isStopped;

        public StopOrDeleteLink(String id, boolean isStopped, String user, int jobID) {
            super(id);
            this.user = user;
            this.jobID = jobID;
            this.isStopped = isStopped;
        }

        public int getJobID() {
            return jobID;
        }

        @Override
        public void onClick() {
            if (isStopped) {
                // remove from DB, cleanup files
                ComputeJobDeleteTask computeJobDeleteTask = new ComputeJobDeleteTask(jobID);
                computeJobDeleteTask.run();
            } else {
                ComputeJobStopTask computeJobStopTask = new ComputeJobStopTask(jobID);
                computeJobStopTask.run();
            }
            setResponsePage(ComputeJobs.class);
        }

    }

}
