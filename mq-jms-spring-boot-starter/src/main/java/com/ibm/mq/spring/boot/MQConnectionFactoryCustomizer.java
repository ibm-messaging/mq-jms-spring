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

import com.ibm.mq.jms.MQConnectionFactory;

/**
 * Callback interface that can be implemented by beans wishing to customize the
 * {@link MQConnectionFactory} while retaining default auto-configuration.
 */
@FunctionalInterface
public interface MQConnectionFactoryCustomizer {

	/**
	 * Customize the {@link MQConnectionFactory}.
	 * @param factory the factory to customize
	 */
	void customize(MQConnectionFactory factory);

}
