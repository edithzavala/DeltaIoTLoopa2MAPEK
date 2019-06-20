/*******************************************************************************
 * Copyright (c) 2019 Universitat Politécnica de Catalunya (UPC)
 *   
 * Licensed under the Apache License, Version 2.0 (the "License"); you may 
 * not use this file except in compliance with the License. You may obtain a 
 * copy of the License at
 * 
 * http://www.apache.org/licenses/LICENSE-2.0
 *  
 * Unless required by applicable law or agreed to in writing, software 
 * distributed under the License is distributed on an "AS IS" BASIS, ITHOUT WARRANTIES 
 * OR CONDITIONS OF ANY KIND, either express or implied. See the License for the specific 
 * language governing permissions and limitations under the License.
 * 
 * Contributors: Edith Zavala
 ******************************************************************************/

package model;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MonitorFleConfiguration {
	private int monFreq;
	private List<MonitorConfiguration> monitorsl1;

	public int getMonFreq() {
		return monFreq;
	}

	public void setMonFreq(int monFreq) {
		this.monFreq = monFreq;
	}

	public List<MonitorConfiguration> getMonitorsl1() {
		return monitorsl1;
	}

	public void setMonitorsl1(List<MonitorConfiguration> monitorsl1) {
		this.monitorsl1 = monitorsl1;
	}
}
