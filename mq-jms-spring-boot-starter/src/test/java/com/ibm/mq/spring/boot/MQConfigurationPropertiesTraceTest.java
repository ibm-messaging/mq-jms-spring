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

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.slf4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(classes= {MQConfigurationPropertiesTrace.class})
public class MQConfigurationPropertiesTraceTest {


  @Autowired
  private MQConfigurationPropertiesTrace mQConfigurationPropertiesTrace;

  @Test
  public void testMQConfigurationPropertiesTraceNotNull() {
    Assertions.assertNotNull(mQConfigurationPropertiesTrace);
  }

  @Test
  public void testFdcPath() {
    String fdcPath = "/opt/mqm/errors";
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getFFDCPath().isBlank());
    mQConfigurationPropertiesTrace.setFFDCPath(fdcPath);
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getFFDCPath(),fdcPath);
  }

  @Test
  public void testGetFfdcSuppress() {
    String fdcSuppressString = "test";
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getFfdcSuppress().isBlank());
    mQConfigurationPropertiesTrace.setFfdcSuppress(fdcSuppressString);
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getFfdcSuppress(),fdcSuppressString);
  }

  @Test
  public void testGetStatus() {
    String status = "OFF";
    mQConfigurationPropertiesTrace.setStatus(status);
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getStatus(),status);
    status = "ON";
    mQConfigurationPropertiesTrace.setStatus(status);
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getStatus(),status);
    status = "true";
    mQConfigurationPropertiesTrace.setStatus(status);
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getStatus(),"ON");
  }

  @Test
  public void testGetMaxTraceBytes() {
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getMaxTraceBytes().isBlank());
    mQConfigurationPropertiesTrace.setMaxTraceBytes("1025");
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getMaxTraceBytes(),"1025");
  }

  @Test
  public void testGetParameterTrace() {
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getParameterTrace().isBlank());
    mQConfigurationPropertiesTrace.setParameterTrace("trace");
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getParameterTrace(),"trace");
  }

  @Test
  public void testGetTraceFileCount() {
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getTraceFileCount().isBlank());
    mQConfigurationPropertiesTrace.setTraceFileCount("12");
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getTraceFileCount(),"12");
  }

  @Test
  public void testGetTraceFileLimit() {
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getTraceFileLimit().isBlank());
    mQConfigurationPropertiesTrace.setTraceFileLimit("23");
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getTraceFileLimit(),"23");
  }

  @Test
  public void testSetFfstSuppressProbeIDs() {
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getFfstSuppressProbeIDs().isBlank());
    mQConfigurationPropertiesTrace.setFfdcSuppressProbeIDs("suppress");
    mQConfigurationPropertiesTrace.setFfstSuppressProbeIDs("suppress");
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getFfstSuppressProbeIDs().isBlank());
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getFfdcSuppressProbeIDs(),"suppress");
  }

  @Test
  public void testSetLogFile() {
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getLogFile().isBlank());
    mQConfigurationPropertiesTrace.setLogFile("file.log");
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getLogFile(),"file.log");
  }

  @Test
  public void testSetTraceFile() {
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getTraceFile().isBlank());
    mQConfigurationPropertiesTrace.setTraceFile("file.trace");
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getTraceFile(),"file.trace");
  }

  @Test
  public void testGetFFSTPath() {
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getFFSTPath().isBlank());
    mQConfigurationPropertiesTrace.setFFSTPath("QM1/trace/hello");
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getFFDCPath(),"QM1/trace/hello");
  }

  @Test
  public void testFfstSuppress() {
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getFfstSuppress().isBlank());
    mQConfigurationPropertiesTrace.setFfstSuppress("QM1/trace/hello");
    Assertions.assertTrue(mQConfigurationPropertiesTrace.getFfstSuppress().isBlank());
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getFfdcSuppress(),"QM1/trace/hello");
  }

  @Test
  public void testSetSysProp() {
    mQConfigurationPropertiesTrace.setProps.clear();
    Assertions.assertEquals(mQConfigurationPropertiesTrace.setProps.size(),0);
    mQConfigurationPropertiesTrace.setSysProp("ibm.mq", "trace");
    Assertions.assertEquals(mQConfigurationPropertiesTrace.setProps.size(),1);
    mQConfigurationPropertiesTrace.setDiagProp("ibm.dia", "dia");
    Assertions.assertEquals(mQConfigurationPropertiesTrace.setProps.size(),2);
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
    Assertions.assertEquals(mQConfigurationPropertiesTrace.setProps.size(),0);
    mQConfigurationPropertiesTrace.setFFDCPath("path");
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getFFDCPath(),"path");
    String status = "ON";
    mQConfigurationPropertiesTrace.setStatus(status);
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getStatus(),status);
    mQConfigurationPropertiesTrace.setProperties();
    mQConfigurationPropertiesTrace.setFFDCPath("");
  }

  @Test
  public void testPropertiesWithStatusOFF() {
    mQConfigurationPropertiesTrace.setProps.clear();
    Assertions.assertEquals(mQConfigurationPropertiesTrace.setProps.size(),0);
    mQConfigurationPropertiesTrace.setFFDCPath("path");
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getFFDCPath(),"path");
    String status = "OFF";
    mQConfigurationPropertiesTrace.setStatus(status);
    Assertions.assertEquals(mQConfigurationPropertiesTrace.getStatus(),status);
    mQConfigurationPropertiesTrace.setProperties();
    mQConfigurationPropertiesTrace.setFFDCPath("");
  }
}
