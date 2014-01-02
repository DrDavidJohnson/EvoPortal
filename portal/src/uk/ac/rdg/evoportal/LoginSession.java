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

package uk.ac.rdg.evoportal;

import org.apache.wicket.Request;
import org.apache.wicket.protocol.http.WebApplication;
import org.apache.wicket.protocol.http.WebSession;
import org.hibernate.Session;
import org.hibernate.Transaction;
import uk.ac.rdg.evoportal.beans.PortalUser;
import uk.ac.rdg.util.HibernateUtil;

/**
 *
 * @author david
 */
public class LoginSession extends WebSession {

    private String username = null;
    private long lastTouch = -1;

    public LoginSession(WebApplication application, Request request) {
        super(application, request);
    }

    public boolean authenticate(final String username, final String password) {
        Session s = HibernateUtil.getSessionFactory().openSession();
        Transaction tx = s.beginTransaction();
        Object result = s.createQuery("from PortalUser u where u.username='" + username + "' and u.passwordHash='" + password + "'").uniqueResult();
        // FIXME Hashing disabled as does not match PasswordHasher on commandline
//        String passwordHash = SecurityUtil.hashPass(password);
//        Object result = s.createQuery("from PortalUser u where u.username='" + username + "' and u.passwordHash='" + passwordHash + "'").uniqueResult();
        tx.commit();
        if (tx.wasCommitted()) {
            if (result!=null && result instanceof PortalUser) {
                PortalUser userObj = (PortalUser)result;
                this.username = userObj.getUsername();
                this.lastTouch = userObj.getLastTouch();

                // this bit updates user record to reflect last login
                userObj.setLastTouch(System.currentTimeMillis());
                tx = s.beginTransaction();
                s.update(userObj);
                tx.commit();
                if (!tx.wasCommitted()) {
                    // TODO implement rollback and notify user
                }
            }
        }
        return this.username != null;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public long getLastTouch() {
        return lastTouch;
    }

    public void setLastTouch(long lastTouch) {
        this.lastTouch = lastTouch;
    }



}
