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
import org.apache.wicket.PageParameters;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;

/**
 *
 * @author david
 */
public final class Login extends BasePage {
    public Login() {
        super ();
        add(new LoginForm("loginForm"));
        add(new FeedbackPanel("feedback"));
    }

    public Login(PageParameters params) {
        //
    }

    class LoginForm extends Form {

        private final ValueMap properties = new ValueMap();

        public LoginForm(String id) {
            super(id);
            add(new TextField("username", new PropertyModel(properties, "username"))
                    .setType(String.class) // text field accepts strings
                    .setRequired(true)); // must not be null/empty
            add(new PasswordTextField("password", new PropertyModel(properties, "password"))
                    .setResetPassword(true) // tells text field to reset page on refresh
                    .setType(String.class) // text field accepts strings
                    .setRequired(true)); // must not be null/empty
        }

        @Override
        protected void onSubmit() {
            {
                // Get session info
                LoginSession session = (LoginSession) getSession();

                // Sign the user in
                if (session.authenticate(properties.getString("username"), properties.getString("password"))) {
                    setResponsePage(ComputeJobs.class);
                } else {
                    error("Unable to sign you in");
                }
            }
        }

    }

}

