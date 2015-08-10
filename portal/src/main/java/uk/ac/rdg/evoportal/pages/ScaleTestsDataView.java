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
import java.util.Iterator;
import java.util.List;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.link.BookmarkablePageLink;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.repeater.Item;
import org.apache.wicket.markup.repeater.data.DataView;
import org.apache.wicket.util.time.Duration;
import uk.ac.rdg.evoportal.beans.ScaleTest;
import uk.ac.rdg.evoportal.beans.ScaleTestComputeJob;
import uk.ac.rdg.evoportal.data.ScaleTestsDataProvider;
import uk.ac.rdg.evoportal.tasks.ScaleTestDeleteTask;
import uk.ac.rdg.evoportal.tasks.ScaleTestStopTask;

/**
 *
 * @author david
 */
public class ScaleTestsDataView extends DataView {

    public ScaleTestsDataView(String id, ScaleTestsDataProvider dataProvider) {
        super(id, dataProvider);
    }

    @Override
    protected void populateItem(final Item item) {
        final ScaleTest scaleTest = (ScaleTest)item.getModelObject();
        final BookmarkablePageLink<Void> link = new BookmarkablePageLink<Void>("details", ScaleTestDetail.class);
        link.setParameter("id", scaleTest.getTestID());
        link.add(new Label("id", Long.toString(scaleTest.getTestID())));
        item.add(link);
        item.add(new Label("name", scaleTest.getLabel()));
        item.add(new Label("submitted", new Date(scaleTest.getTestID()).toString()));
        item.add(new Label("iterations", Integer.toString(scaleTest.getIterations())));
        List<ScaleTestComputeJob> jobsList = scaleTest.getScaleTestComputeJobs();
        String nodesList = "";
        for (Iterator<ScaleTestComputeJob> i = jobsList.iterator();i.hasNext();) {
            nodesList = nodesList + Integer.toString(i.next().getNodes());
            if (i.hasNext()) nodesList+=", ";
        }
        item.add(new Label("nodes", nodesList));
        int percentageDone = scaleTest.getPercentageDone();
        item.add(new Label("pcDone", percentageDone + "%"));
        boolean resultsReady = (percentageDone==100);
        StopOrDeleteLink stopLink = new StopOrDeleteLink("stopOrDeleteLink", resultsReady, scaleTest.getOwner(), scaleTest.getTestID());
        String stopOrDeleteLabel = "Stop";
        if (resultsReady) {
            stopOrDeleteLabel = "Delete";
        }
        stopLink.add(new Label("stopOrDelete", stopOrDeleteLabel));
        item.add(stopLink);
        item.setOutputMarkupId(true);
        AjaxSelfUpdatingTimerBehavior timer = new AjaxSelfUpdatingTimerBehavior(Duration.seconds(15)) {
            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                ScaleTest refreshedTest = ScaleTestsDataProvider.get(scaleTest.getTestID());
                int percentageDone = scaleTest.getPercentageDone();
                item.get("pcDone").replaceWith(new Label("pcDone", percentageDone + "%"));
                boolean resultsReady = (percentageDone==100);
                if (resultsReady) {
                    StopOrDeleteLink deleteLink = new StopOrDeleteLink("stopOrDeleteLink", resultsReady, refreshedTest.getOwner(), refreshedTest.getTestID());
                    deleteLink.add(new Label("stopOrDelete", "Delete"));
                    item.get("stopOrDeleteLink").replaceWith(deleteLink);
                }
            }
        };
        item.add(timer);
    }

    class StopOrDeleteLink extends Link {

        long testID;
        String user;
        boolean isResultsReady;

        public StopOrDeleteLink(String id, boolean isResultsReady, String user, long testID) {
            super(id);
            this.user = user;
            this.testID = testID;
            this.isResultsReady = isResultsReady;
        }

        @Override
        public void onClick() {
            if (isResultsReady) {
                ScaleTestDeleteTask scaleTestDeleteTask = new ScaleTestDeleteTask(testID);
                scaleTestDeleteTask.run();
            } else {
                ScaleTestStopTask scaleTestStopTask = new ScaleTestStopTask(testID);
                scaleTestStopTask.run();
            }
            setResponsePage(ScaleTests.class);
        }

    }

}
