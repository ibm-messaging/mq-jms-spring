/*
 * Copyright © 2025 IBM Corp. All rights reserved.
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

@SpringBootTest(classes= {MQConfigurationPropertiesTrace.class})
@RunWith(SpringRunner.class)
public class MQConfigurationPropertiesTraceTest {
	
	
	@Autowired
	private MQConfigurationPropertiesTrace mQConfigurationPropertiesTrace;
	
	@Test
	public void testMQConfigurationPropertiesTraceNotNull() {
		assertThat(mQConfigurationPropertiesTrace).isNotNull();
	}
	
	@Test
	public void testFdcPath() {
		String fdcPath = "/opt/mqm/errors";
		assertThat(mQConfigurationPropertiesTrace.getFFDCPath()).isBlank();
		mQConfigurationPropertiesTrace.setFFDCPath(fdcPath);
		assertThat(mQConfigurationPropertiesTrace.getFFDCPath()).isEqualTo(fdcPath);
	}
	
	@Test
	public void testGetFfdcSuppress() {
		String fdcSuppressString = "test";
		assertThat(mQConfigurationPropertiesTrace.getFfdcSuppress()).isBlank();
		mQConfigurationPropertiesTrace.setFfdcSuppress(fdcSuppressString);
		assertThat(mQConfigurationPropertiesTrace.getFfdcSuppress()).isEqualTo(fdcSuppressString);
	}
	
	@Test
	public void testGetStatus() {
		String status = "OFF";
		mQConfigurationPropertiesTrace.setStatus(status);
		assertThat(mQConfigurationPropertiesTrace.getStatus()).isEqualTo(status);
		status = "ON";
		mQConfigurationPropertiesTrace.setStatus(status);
		assertThat(mQConfigurationPropertiesTrace.getStatus()).isEqualTo(status);
		status = "true";
		mQConfigurationPropertiesTrace.setStatus(status);
		assertThat(mQConfigurationPropertiesTrace.getStatus()).isEqualTo("ON");
	}
	
	@Test
	public void testGetMaxTraceBytes() {
		assertThat(mQConfigurationPropertiesTrace.getMaxTraceBytes()).isBlank();
		mQConfigurationPropertiesTrace.setMaxTraceBytes("1025");
		assertThat(mQConfigurationPropertiesTrace.getMaxTraceBytes()).isEqualTo("1025");
	}
	
	@Test
	public void testGetParameterTrace() {
		assertThat(mQConfigurationPropertiesTrace.getParameterTrace()).isBlank();
		mQConfigurationPropertiesTrace.setParameterTrace("trace");
		assertThat(mQConfigurationPropertiesTrace.getParameterTrace()).isEqualTo("trace");
	}
	
	@Test
	public void testGetTraceFileCount() {
		assertThat(mQConfigurationPropertiesTrace.getTraceFileCount()).isBlank();
		mQConfigurationPropertiesTrace.setTraceFileCount("12");
		assertThat(mQConfigurationPropertiesTrace.getTraceFileCount()).isEqualTo("12");
	}
	
	@Test
	public void testGetTraceFileLimit() {
		assertThat(mQConfigurationPropertiesTrace.getTraceFileLimit()).isBlank();
		mQConfigurationPropertiesTrace.setTraceFileLimit("23");
		assertThat(mQConfigurationPropertiesTrace.getTraceFileLimit()).isEqualTo("23");
	}
	
	@Test
	public void testSetFfstSuppressProbeIDs() {
		assertThat(mQConfigurationPropertiesTrace.getFfstSuppressProbeIDs()).isBlank();
		mQConfigurationPropertiesTrace.setFfdcSuppressProbeIDs("suppress");
		mQConfigurationPropertiesTrace.setFfstSuppressProbeIDs("suppress");
		assertThat(mQConfigurationPropertiesTrace.getFfstSuppressProbeIDs()).isBlank();
		assertThat(mQConfigurationPropertiesTrace.getFfdcSuppressProbeIDs()).isEqualTo("suppress");
	}
	
	@Test
	public void testSetLogFile() {
		assertThat(mQConfigurationPropertiesTrace.getLogFile()).isBlank();
		mQConfigurationPropertiesTrace.setLogFile("file.log");
		assertThat(mQConfigurationPropertiesTrace.getLogFile()).isEqualTo("file.log");
	}
	
	@Test
	public void testSetTraceFile() {
		assertThat(mQConfigurationPropertiesTrace.getTraceFile()).isBlank();
		mQConfigurationPropertiesTrace.setTraceFile("file.trace");
		assertThat(mQConfigurationPropertiesTrace.getTraceFile()).isEqualTo("file.trace");
	}
	
	@Test
	public void testGetFFSTPath() {
		assertThat(mQConfigurationPropertiesTrace.getFFSTPath()).isBlank();
		mQConfigurationPropertiesTrace.setFFSTPath("QM1/trace/hello");
		assertThat(mQConfigurationPropertiesTrace.getFFDCPath()).isEqualTo("QM1/trace/hello");
	}
	
	@Test
	public void testFfstSuppress() {
		assertThat(mQConfigurationPropertiesTrace.getFfstSuppress()).isBlank();
		mQConfigurationPropertiesTrace.setFfstSuppress("QM1/trace/hello");
		assertThat(mQConfigurationPropertiesTrace.getFfstSuppress()).isBlank();
		assertThat(mQConfigurationPropertiesTrace.getFfdcSuppress()).isEqualTo("QM1/trace/hello");
	}
	
	@Test
	public void testSetSysProp() {
		mQConfigurationPropertiesTrace.setProps.clear();
		assertThat(mQConfigurationPropertiesTrace.setProps.size()).isEqualTo(0);
		mQConfigurationPropertiesTrace.setSysProp("ibm.mq", "trace");
		assertThat(mQConfigurationPropertiesTrace.setProps.size()).isEqualTo(1);
		mQConfigurationPropertiesTrace.setDiagProp("ibm.dia", "dia");
		assertThat(mQConfigurationPropertiesTrace.setProps.size()).isEqualTo(2);
	}
	
	@Test
	public void testTraceProperties() {
		 Logger mockLogger = Mockito.mock(Logger.class);
		 mQConfigurationPropertiesTrace.setProps.put("abc", "def");
		 mQConfigurationPropertiesTrace.setProps.put("abc", "def2");
		 when(mockLogger.isTraceEnabled()).thenReturn(true);
		 mQConfigurationPropertiesTrace.traceProperties(mockLogger);
		 verify(mockLogger).isTraceEnabled();
		 verify(mockLogger, times(1)).isTraceEnabled();
	}
	
	@Test
	public void testPropertiesWithStatusON() {
		mQConfigurationPropertiesTrace.setProps.clear();
		assertThat(mQConfigurationPropertiesTrace.setProps.size()).isEqualTo(0);
		mQConfigurationPropertiesTrace.setFFDCPath("path");
		assertThat(mQConfigurationPropertiesTrace.getFFDCPath()).isEqualTo("path");
		String status = "ON";
		mQConfigurationPropertiesTrace.setStatus(status);
		assertThat(mQConfigurationPropertiesTrace.getStatus()).isEqualTo(status);
		mQConfigurationPropertiesTrace.setProperties();
		mQConfigurationPropertiesTrace.setFFDCPath("");
	}
	
	@Test
	public void testPropertiesWithStatusOFF() {
		mQConfigurationPropertiesTrace.setProps.clear();
		assertThat(mQConfigurationPropertiesTrace.setProps.size()).isEqualTo(0);
		mQConfigurationPropertiesTrace.setFFDCPath("path");
		assertThat(mQConfigurationPropertiesTrace.getFFDCPath()).isEqualTo("path");
		String status = "OFF";
		mQConfigurationPropertiesTrace.setStatus(status);
		assertThat(mQConfigurationPropertiesTrace.getStatus()).isEqualTo(status);
		mQConfigurationPropertiesTrace.setProperties();
		mQConfigurationPropertiesTrace.setFFDCPath("");
	}
}
