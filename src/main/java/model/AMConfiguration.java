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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class AMConfiguration {
    private String autonomicManagerId;
    private Map<String, String> elementsId;
    private Map<String, String> elementsMessagesCodes;
    private Map<String, String> elementsMessageBodyTypes;
	private List<DeltaIoTRecepient> recipients;
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

	public List<DeltaIoTRecepient> getRecipients() {
		return recipients;
	}

	public void setRecipients(List<DeltaIoTRecepient> recipients) {
		this.recipients = recipients;
	}

    public String getAutonomicManagerId() {
	return autonomicManagerId;
    }

    public void setAutonomicManagerId(String autonomicManagerId) {
	this.autonomicManagerId = autonomicManagerId;
    }

    public Map<String, String> getElementsId() {
	return elementsId;
    }

    public void setElementsId(HashMap<String, String> elementsId) {
	this.elementsId = elementsId;
    }

    public Map<String, String> getElementsMessagesCodes() {
	return elementsMessagesCodes;
    }

    public void setElementsMessagesCodes(HashMap<String, String> elementsMessagesCodes) {
	this.elementsMessagesCodes = elementsMessagesCodes;
    }

    public Map<String, String> getElementsMessageBodyTypes() {
	return elementsMessageBodyTypes;
    }

    public void setElementsMessageBodyTypes(HashMap<String, String> elementsMessageBodyTypes) {
	this.elementsMessageBodyTypes = elementsMessageBodyTypes;
    }
}
