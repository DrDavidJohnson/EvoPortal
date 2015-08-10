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

/*
 * Copyright 2008-2010 David Johnson. All rights reserved.
 */

package uk.ac.rdg.util;

import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

/**
 *
 * @author David Johnson
 */
public class ExpiringObjectTable {

    private Map hashtable = Collections.synchronizedMap(new Hashtable());
    private long timeout = Long.MAX_VALUE;
    private Timer timer = new Timer(true);

    public ExpiringObjectTable(long timeout) {
        this.timeout = timeout;
        timer.schedule(new TimerTask() {
            public void run() {
                Set keySet = hashtable.keySet();
                for(Iterator keys = keySet.iterator();keys.hasNext();) {
                    Object keyObj = keys.next();
                    ExpiringObject expiringObj = (ExpiringObject)hashtable.get(keyObj);
                    if (expiringObj!=null && expiringObj.hasExpired()) {
                        hashtable.remove(keyObj);
                    }
                }
            }
        }, 0, timeout); // run timer task every timeout length to make it granular
    }

    public synchronized Object get(Object obj) {
        ExpiringObject expiringObj = (ExpiringObject)hashtable.get(obj);
        return expiringObj.getObj();
    }

    public synchronized Object put(Object obj, Object obj1) {
        return hashtable.put(obj, new ExpiringObject(obj1));
    }

    public synchronized Object remove(Object obj) {
        return hashtable.remove(obj);
    }

    public synchronized void touch(Object obj) {
        ExpiringObject expiringObj = (ExpiringObject)hashtable.get(obj);
        expiringObj.touch();
    }

    public synchronized int size() {
        return hashtable.size();
    }

    private class ExpiringObject {

        private long lastTouched = System.currentTimeMillis();
        private Object obj;

        public ExpiringObject(Object obj) {
            this.obj = obj;
        }

        public void touch() {
           lastTouched = System.currentTimeMillis();
        }

        public boolean hasExpired() {
            return (System.currentTimeMillis() - lastTouched) > timeout;
        }

        public Object getObj() {
            return obj;
        }

    }

}