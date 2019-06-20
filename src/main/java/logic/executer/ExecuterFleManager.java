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

package logic.executer;

import java.util.HashMap;
import java.util.Map;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageBody;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.comm.message.MessageType;
import org.loopa.element.functionallogic.enactor.executer.IExecuterFleManager;
import org.loopa.generic.element.component.ILoopAElementComponent;
import org.loopa.policy.IPolicy;
import org.loopa.policy.Policy;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ExecuterFleManager implements IExecuterFleManager {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
	private IPolicy allPolicy = new Policy(this.getClass().getName(), new HashMap<String, String>());
	private ILoopAElementComponent owner;

	@Override
	public void processLogicData(Map<String, String> data) {
		LOGGER.info("Receive data");
		sendAdaptationToEffect(data.get("content"));
	}
	
	private void sendAdaptationToEffect(String adaptation) {
		LoopAElementMessageBody messageContent = new LoopAElementMessageBody("ADAPT", adaptation);
		String code = getComponent().getElement().getElementPolicy().getPolicyContent()
				.get(LoopAElementMessageCode.MSSGOUTFL.toString());
		IMessage mssg = new Message(owner.getComponentId(), allPolicy.getPolicyContent().get(code),
				Integer.parseInt(code), MessageType.REQUEST.toString(), messageContent.getMessageBody());
		((ILoopAElementComponent) owner.getComponentRecipient(mssg.getMessageTo()).getRecipient()).doOperation(mssg);
	}

	@Override
	public void setConfiguration(Map<String, String> config) {
		this.allPolicy.update(new Policy(this.allPolicy.getPolicyOwner(), config));
	}

	@Override
	public ILoopAElementComponent getComponent() {
		return this.owner;
	}

	@Override
	public void setComponent(ILoopAElementComponent c) {
		this.owner = c;

	}
}
