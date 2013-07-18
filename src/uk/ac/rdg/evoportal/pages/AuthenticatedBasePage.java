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

import uk.ac.rdg.evoportal.*;
import uk.ac.rdg.evoportal.panels.NavigationPanel;
import org.apache.wicket.model.IModel;

/**
 *
 * @author david
 */
public class AuthenticatedBasePage extends BasePage {

    public AuthenticatedBasePage() {
        this(null);
    }

    public AuthenticatedBasePage(IModel model) {
        super(model);
        // if session is not yet authenticated, redirect to login page, otherwise touch session
        LoginSession session = (LoginSession)getSession();
        if (session.getUsername()==null) {
            setResponsePage(Login.class); // redirect to Login if not logged in, save redirect is params
        } else {
            ((EvoPortal)getApplication()).touchSession(session.getUsername());
        }
        add(new NavigationPanel("mainNavigation"));
    }
}

