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

import org.loopa.recipient.IRecipient;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class DeltaIoTRecepient implements IRecipient {

	private String id;

	private List<String> typeOfData;

	private String recipient;
	

	public String getId() {
		return id;
	}

	public void setId(String id) {
		this.id = id;
	}

	public void setTypeOfData(List<String> typeOfData) {
		this.typeOfData = typeOfData;
	}

	public void setRecipient(String recipient) {
		this.recipient = recipient;
	}

	@Override
	public String getRecipient() {
		return this.recipient;
	}

	@Override
	public String getrecipientId() {
		return this.id;
	}

	@Override
	public List<String> getTypeOfData() {
		return this.typeOfData;
	}
}
