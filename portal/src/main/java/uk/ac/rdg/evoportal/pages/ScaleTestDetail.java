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
import java.util.Arrays;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.SortedSet;
import java.util.TreeSet;
import org.apache.wicket.IClusterable;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.ajax.form.AjaxFormComponentUpdatingBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.DropDownChoice;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.TextArea;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.model.CompoundPropertyModel;
import org.apache.wicket.model.IModel;
import org.apache.wicket.model.Model;
import org.apache.wicket.util.time.Duration;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.data.xy.DefaultXYDataset;
import uk.ac.rdg.evoportal.beans.ScaleTest;
import uk.ac.rdg.evoportal.beans.ScaleTestComputeJob;
import uk.ac.rdg.evoportal.data.ScaleTestComputeJobsDataProvider;
import uk.ac.rdg.evoportal.data.ScaleTestsDataProvider;
import uk.ac.rdg.util.TimeUtil;

/**
 *
 * @author david
 */
public final class ScaleTestDetail extends AuthenticatedBasePage {

    CalculatorState calc = new CalculatorState();
    IModel<CalculatorState> calcModel = new Model<CalculatorState>(calc);

    public ScaleTestDetail(PageParameters params) {
        String idStr = params.getString("id");
        final long testID = Long.parseLong(idStr);
        ScaleTest scaleTest = ScaleTestsDataProvider.get(testID);
        int percentageDone = scaleTest.getPercentageDone();
        String testName = scaleTest.getLabel();
        int iterations = scaleTest.getIterations();
        add(new Label("id", idStr));
        add(new Label("name", testName));
        add(new Label("its", Integer.toString(iterations)));
        add(new Label("pcDone", percentageDone + "%"));
        add(new TextArea("controlBlock", new Model<String>(scaleTest.getBPBlock())));
        SortedSet<Integer> nodesSet = new TreeSet<Integer>();
        List<ScaleTestComputeJob> results = scaleTest.getScaleTestComputeJobs();
        double[][] runSeries = new double[2][results.size()];
        int j = 0;
        double testSize = (double)iterations;
        for (Iterator<ScaleTestComputeJob> i = results.iterator();i.hasNext();) {
            ScaleTestComputeJob result = i.next();
            int nodes = result.getNodes();
            int duration = result.getDuration();
            runSeries[0][j] = (double)nodes;
            runSeries[1][j] = (double)duration; // in seconds
            nodesSet.add(result.getNodes());
            double m = ((double)duration/testSize);
            calc.putMultipler(nodes, m);
            j++;
        }
        List<Integer> iterationsChoices = Arrays.asList(new Integer[]{100000, 200000, 300000, 400000, 500000, 1000000, 1200000, 1300000, 1400000, 1500000, 2000000, 3000000, 4000000, 5000000, 6000000, 7000000, 8000000, 9000000, 10000000});
        Form calcForm = new Form("calcForm", new CompoundPropertyModel(calc));
        calcForm.setVisible(false);
        // only render image if test is completed with all jobs stopped
        boolean isResultsReady = percentageDone==100;
        if (isResultsReady) {
            DefaultXYDataset xyData = new DefaultXYDataset();
            xyData.addSeries(testName, runSeries);
            JFreeChart scaleChart = ChartFactory.createScatterPlot("Scale test results for " + testName,
                    "Number of nodes",
                    "Time to run in seconds",
                    xyData,
                    PlotOrientation.VERTICAL,
                    true,
                    true,
                    true);
            add(new ScaleTestResultsChart("chart", scaleChart, 600, 300));
            calc.setIterations(iterationsChoices.get(0));
            calc.setNodes(nodesSet.first());
            calcForm.setVisible(true);
        } else {
            add(new Image("chart"));
        }

        final Label durationText = new Label("durationString");
        durationText.setOutputMarkupId(true);
        DropDownChoice iterationsDropDown = new DropDownChoice("iterations",
                iterationsChoices);
        iterationsDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(durationText);
                }
            });
        DropDownChoice nodesDropDown = new DropDownChoice("nodes", Arrays.asList(nodesSet.toArray()));
        nodesDropDown.add(new AjaxFormComponentUpdatingBehavior("onchange") {
                @Override
                protected void onUpdate(AjaxRequestTarget target) {
                    target.addComponent(durationText);
                }
            });
        calcForm.add(iterationsDropDown);
        calcForm.add(nodesDropDown);
        calcForm.add(durationText);
        add(calcForm);
        ScaleTestComputeJobsDataProvider scaleTestComputeJobsDataProvider = new ScaleTestComputeJobsDataProvider(testID);
        add(new ScaleTestComputeJobsDataView("jobDetails", scaleTestComputeJobsDataProvider, testID));
        if (!isResultsReady) {
            AjaxSelfUpdatingTimerBehavior timer = new AjaxSelfUpdatingTimerBehavior(Duration.seconds(15)) {
                @Override
                protected void onPostProcessTarget(AjaxRequestTarget target) {
                    ScaleTest refreshedScaleTest = ScaleTestsDataProvider.get(testID);
                    int pcDone = refreshedScaleTest.getPercentageDone();
                    get("pcDone").replaceWith(new Label("pcDone", pcDone + "%"));
                    if (pcDone==100) {
                        setResponsePage(ScaleTestDetail.class, new PageParameters("id=" + testID));
                    }
                }
            };
            add(timer);
        }
    }

    public ScaleTestDetail() {
        setResponsePage(ScaleTests.class);
    }

    private class CalculatorState implements IClusterable {
        int iterations = 0;
        int nodes = 0;
        Hashtable<Integer, Double> multipliers = new Hashtable<Integer, Double>();

        public void putMultipler(int nodes, double mult) {
            multipliers.put(nodes, mult);
        }

        public int getDuration() {
            int result = 0;
            Double multiplier = multipliers.get(nodes);
            if (multiplier!=null) {
                result = (int) ((double)iterations * multiplier);
            }
            return result;
        }

        public void setDuration(int duration) {
            // do nowt
        }

        public String getDurationString() {
            String resultString = "undefined"; // hours:minutes:seconds
            int duration = getDuration();
            String timeString = TimeUtil.millisToLongDHMS(duration * 1000);
            resultString = timeString;
            return resultString;
        }

        public void setDurationString(String s) {
            // do nowt
        }

        public int getIterations() {
            return iterations;
        }

        public void setIterations(int iterations) {
            this.iterations = iterations;
        }

        public int getNodes() {
            return nodes;
        }

        public void setNodes(int nodes) {
            this.nodes = nodes;
        }

    }
}
