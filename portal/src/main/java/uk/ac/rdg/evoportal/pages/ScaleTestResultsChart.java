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

import org.apache.wicket.Resource;
import org.apache.wicket.markup.html.image.Image;
import org.apache.wicket.markup.html.image.resource.DynamicImageResource;
import org.apache.wicket.model.Model;
import org.apache.wicket.protocol.http.WebResponse;
import org.jfree.chart.JFreeChart;

/**
 *
 * @author david
 */
public class ScaleTestResultsChart extends Image {

    private int width;
    private int height;
    private JFreeChart chart;

	public ScaleTestResultsChart(String id, JFreeChart chart, int width, int height){
		super(id, new Model(chart));
                this.chart = chart;
		this.width = width;
		this.height = height;
	}

	@Override
    protected Resource getImageResource() {
        return new DynamicImageResource(){
			@Override
			protected byte[] getImageData() {
                            return toImageData(chart.createBufferedImage(width, height));
			}

			@Override
		    protected void setHeaders(WebResponse response) {
		        if (isCacheable()) {
		            super.setHeaders(response);
		        } else {
		            response.setHeader("Pragma", "no-cache");
		            response.setHeader("Cache-Control", "no-cache");
		            response.setDateHeader("Expires", 0);
		        }
		    }
        };
    }
}