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

package uk.ac.rdg.evoportal.panels;

import org.apache.wicket.ajax.AjaxRequestTarget;
import org.apache.wicket.ajax.AjaxSelfUpdatingTimerBehavior;
import org.apache.wicket.markup.html.basic.Label;
import uk.ac.rdg.evoportal.pages.Login;
import org.apache.wicket.markup.html.link.Link;
import org.apache.wicket.markup.html.panel.Panel;
import org.apache.wicket.util.time.Duration;
import uk.ac.rdg.evoportal.EvoPortal;
import uk.ac.rdg.evoportal.LoginSession;

/**
 *
 * @author david
 */
public final class NavigationPanel extends Panel {

    public NavigationPanel(String id) {
        super (id);
        add(new Link("logoutLink") {
            @Override
            public void onClick() {
                // immediately remove from counter
                ((EvoPortal)getApplication()).invalidateSession(((LoginSession)getSession()).getUsername());
                // invalidate session object
                getSession().invalidate();
                // return to login page
                setResponsePage(Login.class);
            }
        });
        String stats = ((EvoPortal)getApplication()).getPBSStats();
        if (stats.length()>0) {
            stats = "Cluster status: " + stats;
        } else {
            stats = "Cluster status not currently available" + stats;
        }
        add(new Label("serverstats", stats));
        AjaxSelfUpdatingTimerBehavior timer = new AjaxSelfUpdatingTimerBehavior(Duration.seconds(15)) {
            @Override
            protected void onPostProcessTarget(AjaxRequestTarget target) {
                String refreshedStats = ((EvoPortal)getApplication()).getPBSStats().trim();
                if (refreshedStats.length()>0) {
                    refreshedStats = "Cluster status: " + refreshedStats;
                } else {
                    refreshedStats = "Cluster status not currently available";
                }
                get("serverstats").replaceWith(new Label("serverstats", refreshedStats));
            }
        };
        add(timer);
    }
}
