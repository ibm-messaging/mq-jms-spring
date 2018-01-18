/*
 * Copyright © 2018 IBM Corp. All rights reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file
 * except in compliance with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the
 * License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND,
 * either express or implied. See the License for the specific language governing permissions
 * and limitations under the License.
 */

package com.ibm.mq.spring.boot;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "mq")
public class MQConfigurationProperties {
  
  // There are many properties that can be set on an MQ Connection Factory, but these are the most commonly-used
  // for both direct and client connections. If you use TLS for client connectivity, properties related to that
  // (keystore, certificates, ciphers etc) must be set independently.

	/** MQ Queue Manager. **/
	private String queueManager;

	/** Channel - for example "SYSTEM.DEF.SVRCONN". Default to null, for connection to local queue manager **/
	private String channel = null;

	/** Connection Name (eg 'system.example.com(1414)' ) **/
	private String connName = "localhost(1414)";

	/** MQ user */
	private String user;

	/** MQ password */
	private String password;
	
  public String getQueueManager() {
		return queueManager;
	}

	public void setQueueManager(String queueManager) {
		this.queueManager = queueManager;
	}

	public String getChannel() {
		return channel;
	}

	public void setChannel(String channel) {
		this.channel = channel;
	}

	public String getConnName() {
		return connName;
	}

	public void setConnName(String connName) {
		this.connName = connName;
	}

	public String getUser() {
		return user;
	}

	public void setUser(String user) {
		this.user = user;
	}

	public String getPassword() {
		return password;
	}

	public void setPassword(String password) {
		this.password = password;
	}
	
}
