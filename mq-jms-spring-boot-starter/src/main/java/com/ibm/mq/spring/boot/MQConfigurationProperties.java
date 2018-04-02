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

/** There are many properties that can be set on an MQ Connection Factory, but these are the most commonly-used
* for both direct and client connections. If you use TLS for client connectivity, most properties related to that
* (keystore, certificates etc) must be set independently. 
* 
* This class allows for setting the CipherSuite/CipherSpec property, and an indication of whether or not 
* to use the IBM JRE maps for Cipher names - that's not something that is standardised.
*
* The default values have been set to match the settings of the 
* <a href="https://github.com/ibm-messaging/mq-docker">MQ Docker</a> 
* container. 
* 
* <ul>
* <li>queueManager = QM1
* <li>connName = localhost(1414)
* <li>channel = DEV.ADMIN.SVRCONN
* <li>user = admin
* <li>password = passw0rd
* </ul>
* 
*/
@ConfigurationProperties(prefix = "ibm.mq")
public class MQConfigurationProperties {
  
	/** MQ Queue Manager name */
	private String queueManager = "QM1";

	/** Channel - for example "SYSTEM.DEF.SVRCONN" **/
	private String channel = "DEV.ADMIN.SVRCONN";

	/** Connection Name - hostname or address and port. Format like 'system.example.com(1414)'  **/
	private String connName = "localhost(1414)";

	/** MQ user name */
	private String user = "admin";

	/** MQ password */
	private String password = "passw0rd";
        
        private boolean userAuthentificationMQCSP = true;

	/** For TLS connections, you can set either the sslCipherSuite or sslCipherSpec property.
	 * For example, "SSL_ECDHE_RSA_WITH_AES_256_GCM_SHA384"
	 * @see <a href="https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_9.0.0/com.ibm.mq.dev.doc/q113210_.htm">the KnowledgeCenter</a>    
	 */
	private String sslCipherSuite;

	/** For TLS connections, you can set either the sslCipherSuite or sslCipherSpec property.
   * For example, "ECDHE_RSA_AES_256_GCM_SHA384"
   * @see <a href="https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_9.0.0/com.ibm.mq.dev.doc/q113210_.htm">the KnowledgeCenter</a>  
   */
	private String sslCipherSpec;

  /** Set to true for the IBM JRE CipherSuite name maps; set to false to use the Oracle JRE CipherSuite mapping */
	private boolean useIBMCipherMappings = true;
	
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

	public String getSslCipherSuite() {
		return sslCipherSuite;
	}

	public void setSslCipherSuite(String sslCipherSuite) {
		this.sslCipherSuite = sslCipherSuite;
	}

	public String getSslCipherSpec() {
		return sslCipherSpec;
	}

	public void setSslCipherSpec(String sslCipherSpec) {
		this.sslCipherSpec = sslCipherSpec;
	}

	public boolean isUseIBMCipherMappings() {
		return useIBMCipherMappings;
	}

	public void setUseIBMCipherMappings(boolean useIBMCipherMappings) {
  		System.setProperty("com.ibm.mq.cfg.useIBMCipherMappings", Boolean.toString(useIBMCipherMappings));
		this.useIBMCipherMappings = useIBMCipherMappings;
	}
        
        public boolean isUserAuthentificationMQCSP() {
		return userAuthentificationMQCSP;
	}

	public void setUserAuthentificationMQCSP(boolean userAuthentificationMQCSP) {
  		System.setProperty("com.ibm.mq.cfg.userAuthentificationMQCSP", Boolean.toString(userAuthentificationMQCSP));
		this.userAuthentificationMQCSP = userAuthentificationMQCSP;
	}
}
