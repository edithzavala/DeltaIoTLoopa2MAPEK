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

package logic.knowledgebase;

import java.util.Map;

import org.loopa.element.functionallogic.enactor.knowledgebase.IKnowledgeBaseFleManager;
import org.loopa.generic.element.component.ILoopAElementComponent;

public class KnowledgeBaseFleManager implements IKnowledgeBaseFleManager {
	private ILoopAElementComponent owner;
	
	@Override
	public ILoopAElementComponent getComponent() {
		return this.owner;
	}

	@Override
	public void setComponent(ILoopAElementComponent c) {
		this.owner = c;

	}

	@Override
	public void setConfiguration(Map<String, String> arg0) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void processLogicData(Map<String, String> data) {
		// TODO Auto-generated method stub
		
	}

}
