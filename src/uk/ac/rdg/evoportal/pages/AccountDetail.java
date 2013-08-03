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

import org.apache.wicket.markup.html.basic.Label;
import org.apache.wicket.markup.html.form.Form;
import org.apache.wicket.markup.html.form.PasswordTextField;
import org.apache.wicket.markup.html.form.TextField;
import org.apache.wicket.markup.html.panel.FeedbackPanel;
import org.apache.wicket.model.PropertyModel;
import org.apache.wicket.util.value.ValueMap;
import org.apache.wicket.validation.validator.EmailAddressValidator;
import org.hibernate.Session;
import org.hibernate.Transaction;
import uk.ac.rdg.evoportal.LoginSession;
import uk.ac.rdg.evoportal.beans.PortalUser;
import uk.ac.rdg.util.HibernateUtil;
import uk.ac.rdg.util.SecurityUtil;

/**
 *
 * @author david
 */
public final class AccountDetail extends AuthenticatedBasePage {

    public AccountDetail() {
        super ();
        add(new AccountDetailsForm("accountDetailsForm"));
        add(new FeedbackPanel("feedback"));
    }

    class AccountDetailsForm extends Form {

        private final ValueMap properties = new ValueMap();
        private PortalUser user = null;

        public AccountDetailsForm(String id) {
            super(id);
            String username = ((LoginSession)getSession()).getUsername();
            Session s = HibernateUtil.getSessionFactory().openSession();
            Transaction tx = s.beginTransaction();
            Object result = s.createQuery("from PortalUser user where user.username='" + username + "'").uniqueResult();
            tx.commit();
            if (tx.wasCommitted()) {
                if (result!=null && result instanceof PortalUser) {
                    user = (PortalUser)result;
                    properties.put("email", user.getEmailAddress());
                } else {
                    properties.put("email", "error loading email address!");
                }
            }
            properties.put("pass1", "");
            properties.put("pass2", "");
            properties.put("password", "");
            add(new Label("username", username));
            add(new TextField("email", new PropertyModel(properties, "email"))
                    .setRequired(true)
                    .setType(String.class)
                    .add(EmailAddressValidator.getInstance()));
            add(new PasswordTextField("pass1", new PropertyModel(properties, "pass1"))
                    .setResetPassword(true)
                    .setRequired(false)
                    .setType(String.class));
            add(new PasswordTextField("pass2", new PropertyModel(properties, "pass2"))
                    .setResetPassword(true)
                    .setRequired(false)
                    .setType(String.class));
            add(new PasswordTextField("password", new PropertyModel(properties, "password"))
                    .setResetPassword(true)
                    .setRequired(true)
                    .setType(String.class));
        }

        @Override
        protected void onSubmit() {
            super.onSubmit();
            String pwdHash = SecurityUtil.hashPass(properties.getString("password"));
            if (user.getPasswordHash().equals(pwdHash)) {
                String pass1 = properties.getString("pass1");
                String pass2 = properties.getString("pass2");
                if (pass1 != null && pass2 != null) {
                    if (pass1.equals(pass2)) {
                        if (pass1.length() < 8 | pass1.length() > 16) {
                            // notify not long enough
                            error("Password must be 8-16 characters long");
                            return;
                        } else {
                            user.setPasswordHash(SecurityUtil.hashPass(pass1));
                        }
                    } else {
                        // notify new password not cofirmed
                        error("Proposed new password does not match confirmation");
                        return;
                    }
                }
                // set email to new one
                user.setEmailAddress(properties.getString("email"));
                Session s = HibernateUtil.getSessionFactory().openSession();
                Transaction tx = s.beginTransaction();
                s.update(user);
                tx.commit();
                if (tx.wasCommitted()) {
                    info("Your account was updated");
                } else {
                    tx.rollback();
                    error("There was a problem updating your account, no changes made");
                }

            } else {
                // notify wrong password!
                error("Your password is incorrect, did not make changes");
            }
        }
    }

    private static String computeHash(String x)
            throws Exception {
        java.security.MessageDigest d = null;
        d = java.security.MessageDigest.getInstance("SHA-1");
        d.reset();
        d.update(x.getBytes());
        return new String(d.digest());
    }

    private static String byteArrayToHexString(byte[] b) {
        StringBuffer sb = new StringBuffer(b.length * 2);
        for (int i = 0; i < b.length; i++) {
            int v = b[i] & 0xff;
            if (v < 16) {
                sb.append('0');
            }
            sb.append(Integer.toHexString(v));
        }
        return sb.toString().toUpperCase();
    }

}

