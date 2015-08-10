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
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.util.time.Duration;
import uk.ac.rdg.evoportal.GlobalConstants;
import uk.ac.rdg.evoportal.beans.ScaleTestComputeJob;
import uk.ac.rdg.evoportal.data.ScaleTestComputeJobsDataProvider;
import uk.ac.rdg.util.TimeUtil;

/**
 *
 * @author david
 */
public class ScaleTestComputeJobsDataView  extends DataView {

    private long testID;

    public ScaleTestComputeJobsDataView(String id, ScaleTestComputeJobsDataProvider dataProvider, long testID) {
        super(id, dataProvider);
        this.testID = testID;
    }

    @Override
    protected void populateItem(final Item item) {
        ScaleTestComputeJob job = (ScaleTestComputeJob)item.getModelObject();
        final int jobID = job.getJobID();
        final BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("details", ScaleTestComputeJobDetail.class);
        link.setParameter("jobID", jobID);
        link.setParameter("testID", testID);
        link.add(new Label("jobID", Integer.toString(jobID)));
        item.add(link);
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
        if (job.getTimeUsed()>0) {
            item.add(new Label("timeUsed", TimeUtil.millisToShortDHMS(job.getTimeUsed())));
        } else {
            item.add(new Label("timeUsed", "Negligable"));
        }
        if (job.getDuration()>0) {
            item.add(new Label("duration", Integer.toString(job.getDuration())));
        } else {
            item.add(new Label("duration", "No result"));
        }
        if (!isStopped) {
            item.setOutputMarkupId(true);
            AjaxSelfUpdatingTimerBehavior timer = new AjaxSelfUpdatingTimerBehavior(Duration.seconds(15)) {
                @Override
                protected void onPostProcessTarget(AjaxRequestTarget target) {
                    ScaleTestComputeJob refreshedJob = ScaleTestComputeJobsDataProvider.get(jobID);
                    item.get("status").replaceWith(new Label("status", GlobalConstants.getStatusMsg(refreshedJob.getStatus())));
                    if (refreshedJob.getTimeUsed()>0) {
                        item.get("timeUsed").replaceWith(new Label("timeUsed", TimeUtil.millisToShortDHMS(refreshedJob.getTimeUsed())));
                    } else {
                        item.get("timeUsed").replaceWith(new Label("timeUsed", "Negligable"));
                    }
                    if (refreshedJob.getDuration()>0) {
                        item.get("duration").replaceWith(new Label("duration", Integer.toString(refreshedJob.getDuration())));
                    } else {
                        item.get("duration").replaceWith(new Label("duration", "No result"));
                    }
                }
            };
            item.add(timer);
        }
    }

}
