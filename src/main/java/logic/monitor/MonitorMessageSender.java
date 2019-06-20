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
package logic.monitor;

import java.util.Map;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;
import org.loopa.element.sender.messagesender.AMessageSender;
import org.loopa.generic.element.ILoopAElement;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class MonitorMessageSender extends AMessageSender {
	protected final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
	private final Sensor deltaIoTSensors = new Sensor();

	@Override
	public void processMessage(IMessage t) {
//		LOGGER.info("Send message - code:" + t.getMessageCode() + " type:" + t.getMessageCode() + " from:"
//				+ t.getMessageFrom() + " to:" + t.getMessageTo() + " body:" + t.getMessageBody().toString());
		IMessage m = process(t);
		if (m != null)
			sendMessage(m);
	}

	protected IMessage process(IMessage m) {
		return new Message(this.getComponent().getComponentId(), getRecipientFromPolicy(m.getMessageBody()),
				Integer.parseInt(this.getComponent().getElement().getElementPolicy().getPolicyContent()
						.get(LoopAElementMessageCode.MSSGINFL.toString())),
				m.getMessageType(), m.getMessageBody());
	}

	protected String getRecipientFromPolicy(Map<String, String> messageBody) {
		return this.getPolicyVariables().get(messageBody.get("type"));
	}

	protected void sendMessage(IMessage m) {
		if (m.getMessageBody().get("type").equals("ANALYZE_iotanalyzerl2")) {
			((ILoopAElement) this.getComponent().getComponentRecipient(m.getMessageTo()).getRecipient()).getReceiver()
					.doOperation(m);
		} else if (this.getComponent().getComponentRecipient(m.getMessageTo()) != null) {//check if it has not been removed by adaptation
			m.getMessageBody().put("url",
					(String) this.getComponent().getComponentRecipient(m.getMessageTo()).getRecipient());
			deltaIoTSensors.getMonitorSatus(this.getComponent().getElement(), m);
		}
	}

}
