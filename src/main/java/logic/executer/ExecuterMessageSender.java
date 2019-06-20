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

import java.util.Map;

import org.loopa.comm.message.IMessage;
import org.loopa.comm.message.LoopAElementMessageCode;
import org.loopa.comm.message.Message;

import logic.MAPMessageSender;

public class ExecuterMessageSender extends MAPMessageSender {
	// private final Logger LOGGER = LoggerFactory.getLogger(getClass().getName());
	private final Effector deltaIoTEffector = new Effector();
	// private boolean monitormnUp = false;

	@Override
	public void processMessage(IMessage t) {
		IMessage m = process(t);
		if (m != null) {
			if (m.getMessageTo().equals("iotmonitorl1m2")) {
				deltaIoTEffector.effect(m);
			}
			// ObjectMapper mapper = new ObjectMapper();
			// try {
			// AdaptationPlan ap = mapper.readValue(m.getMessageBody().get("content"),
			// AdaptationPlan.class);
			// if (ap.getMonitorsToAdapt().keySet().contains(AdaptationType.UP) &&
			// !monitormnUp) {
			// this.monitormnUp = true;
			// deltaIoTEffector.effect(m);
			// } else if
			// (ap.getMonitorsToAdapt().keySet().contains(AdaptationType.CONFIGURE)) {
			// deltaIoTEffector.effect(m);
			// }
			// } catch (IOException e) {
			// e.printStackTrace();
			// }
		}
	}

	protected IMessage process(IMessage m) {
		return new Message(this.getComponent().getComponentId(), getRecipientFromPolicy(m.getMessageBody()),
				Integer.parseInt(this.getComponent().getElement().getElementPolicy().getPolicyContent()
						.get(LoopAElementMessageCode.MSSGADAPT.toString())),
				m.getMessageType(), m.getMessageBody());
	}

	protected String getRecipientFromPolicy(Map<String, String> messageBody) {
		return this.getPolicyVariables().get(messageBody.get("type"));
	}
}
