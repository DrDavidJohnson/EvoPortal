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

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.apache.wicket.PageParameters;
import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.basic.MultiLineLabel;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.util.time.Duration;

/**
 *
 * @author david
 */
public final class ComputeJobOutputTailPage extends AuthenticatedBasePage {

    transient Logger LOG = Logger.getLogger(ComputeJobOutputTailPage.class.getName());

    public ComputeJobOutputTailPage(final int jobID, final File file) {
        super();
        Link backLink = new Link("backLink") {
            @Override
            public void onClick() {
                setResponsePage(ComputeJobDetail.class, new PageParameters("jobID=" + jobID));
            }
        };
        backLink.add(new Label("jobID", Integer.toString(jobID)));
        add(backLink);
        add(new Label("tailFile", file.getName()));
        add(new MultiLineLabel("tailOutput", getTailOutput(file)));
        add(new AjaxSelfUpdatingTimerBehavior(Duration.seconds(5)) {

            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                super.onPostProcessTarget(target);
                get("tailOutput").replaceWith(new MultiLineLabel("tailOutput", getTailOutput(file)));
            }
        });
    }

    private String getTailOutput(File file) {
        String result = "";
        try {
            // Currently uses passwordless SSH keys to login to sword
            String canonicalPath = file.getCanonicalPath();
            String[] cmd = new String[]{"tail", "-n", "20", canonicalPath};
            ProcessBuilder pb = new ProcessBuilder(cmd);
            Process p = pb.start();
            int val = p.waitFor();
            if (val != 0) {
                try {
                    throw new IOException("Exception during tail -n 20; return code = " + val);
                } catch (IOException ex) {
                    LOG.log(Level.SEVERE, null, ex);
                }
            } else {
                String tailOutput = "";
                InputStream is = p.getInputStream();
                InputStreamReader isr = new InputStreamReader(is);
                BufferedReader br = new BufferedReader(isr);
                String line;
                while ((line = br.readLine()) != null) {
                    tailOutput += line + "\n";
                }
                result = tailOutput;
            }
        } catch (InterruptedException ex) {
            LOG.log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, null, ex);
        }
        return result;
    }
}

