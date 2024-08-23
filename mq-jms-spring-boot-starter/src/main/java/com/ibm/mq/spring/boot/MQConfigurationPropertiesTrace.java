/*
 * Copyright © 2024 IBM Corp. All rights reserved.
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

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * This class gives a way of configuring MQ JMS trace and logging parameters in a Spring style
 * rather than needing additional external files and properties.
 *
 * See
 * https://www.ibm.com/docs/en/ibm-mq/latest?topic=tmcja-collecting-mq-classes-java-trace-by-using-mq-classes-java-configuration-file#q131540_
 * and https://www.ibm.com/docs/en/ibm-mq/latest?topic=mcjcf-using-java-standard-environment-trace-configure-java-trace
 *
 * These properties can be in a file pointed to by the com.ibm.msg.client.config.location system property whose value is
 * a URL. If it's in a file, the URL must begin "file:" - there's no default of the protocol. But here, we're setting things
 * directly as system properties.
 *
 * Not all of the available properties are exposed. On the other hand, we have options that refer to both FFST and FFDC
 * as there is inconsistency in the Java packages. Both variables will map to the same known property.
 * For consistency in this code, I'm always really working with the FFDC variables.
 */
public class MQConfigurationPropertiesTrace {

  private static Logger logger = LoggerFactory.getLogger(MQConfigurationPropertiesTrace.class);

  public static final String propertyTraceStatus = "com.ibm.msg.client.commonservices.trace.status";

  public static final String propertyTraceFileName = "com.ibm.msg.client.commonservices.trace.outputName";
  public static final String propertyTraceFileLimit = "com.ibm.msg.client.commonservices.trace.limit";
  public static final String propertyTraceFileCount = "com.ibm.msg.client.commonservices.trace.count";
  public static final String propertyParameterTrace = "com.ibm.msg.client.commonservices.trace.parameter";
  public static final String propertyMaxTraceBytes = "com.ibm.msg.client.commonservices.trace.maxBytes";

  public static final String propertyFFDCSuppress = "com.ibm.msg.client.commonservices.ffst.suppress";
  public static final String propertyFFDCSuppressProbeIDs = "com.ibm.msg.client.commonservices.ffst.suppress.probeIDs";
  public static final String propertyLogFileName = "com.ibm.msg.client.commonservices.log.outputName";

  // These documented properties are not exposed for various reasons including duplication or confusion with the Diagnostics values
  // The "startup" option can only be used from the command line settings because it's used too early, before this method
  // gets a chance.

  // public static final String propertyTraceStartup = "com.ibm.msg.client.commonservices.trace.startup";
  // public static final String propertyTraceLevel = "com.ibm.msg.client.commonservices.trace.level";
  // public static final String propertyErrorStream = "com.ibm.msg.client.commonservices.trace.errorStream";
  // public static final String compressedTraceProperty = "com.ibm.msg.client.commonservices.trace.compress";
  // public static final String includedPackagesProperty = "com.ibm.msg.client.commonservices.trace.include";
  // public static final String excludedPackagesProperty = "com.ibm.msg.client.commonservices.trace.exclude";
  // public static final String excludedPackagesProperty_headers_etc = "com.ibm.mq.headers;com.ibm.mq.pcf";
  // public static final String searchStringProperty = "com.ibm.msg.client.commonservices.trace.searchString";
  // public static final String dumpOnFFST = "com.ibm.msg.client.commonservices.dumponffst";
  // public static final String dumpLoc = "com.ibm.msg.client.commonservices.dumplocation";
  // public static final String dumpCompressed = "com.ibm.msg.client.commonservices.dumpcompressed";

  // See https://www.ibm.com/docs/en/ibm-mq/latest?topic=components-using-comibmmqcommonservices
  // These give further override for trace, error logs and FDC locations. They will usually be
  // pointed to by the com.ibm.mq.commonservices system property which references a filename. Not a URL. Unlike the
  // above set of properties, this filename does NOT begin with a "file:". Not all of the known properties are exposed.

  private static final String propertyDiagnosticsFile = "com.ibm.mq.commonservices";
  private static final String propertyTraceDetail = "Diagnostics.Java.Trace.Detail"; // high/medium/low - always set to high when
                                                                                     // trace is on
  private static final String propertyTraceFile = "Diagnostics.Java.Trace.Destination.File"; // always enabled when trace is on
  private static final String propertyTraceFilePath = "Diagnostics.Java.Trace.Destination.Pathname"; // Can be directory or full
                                                                                                     // filename. Can include %PID%
                                                                                                     // token
  private static final String propertyFFDCPath = "Diagnostics.Java.FFDC.Destination.Pathname"; // Must be a directory
  private static final String propertyErrorFileName = "Diagnostics.Java.Errors.Destination.Filename";

  // Again, some properties do not need to be user-settable
  // private static final String DiagnosticsMQ = "Diagnostics.MQ";
  // private static final String DiagnosticsJava = "Diagnostics.Java";
  // private static final String propertyTraceConsole = "Diagnostics.Java.Trace.Destination.Console"; // always disabled when trace
  // is on

  /**
   * Set to ON or OFF
   */
  private String status = "";

  private String maxTraceBytes = "";
  private String traceFileLimit = "";
  private String traceFileCount = "";
  private String parameterTrace = "";
  private String logFile = ""; // Cannot use %PID% in this one

  /**
   * Suppress: -1=Show only first occurrence for this probeId
   *            0=Show all
   *         <int>=Show only those where the number is a multiple of this
   * Define properties with both ffst and ffdc prefixes as the real code seems
   * to use a mixture of both       
   */
  private String ffdcSuppress = "";
  private String ffstSuppress = ""; // Not really used

  private String ffdcSuppressProbeIDs = "";
  private String ffstSuppressProbeIDs = ""; // Not really used

  // These are options corresponding to the Diagnostics block
  private String traceFile = ""; // Can contain %PID% variable
  private String ffdcPath = "";
  private String ffstPath = ""; // Not really used

  HashMap<String, String> setProps = new HashMap<>();

  // Dynamically created temporary filename
  private static File tempFile = null;
  // private static FileWriter writer = null;

  public void setStatus(String status) {
    // Spring seems to convert an unquoted ON in the properties file to a boolean-equivalent string, but we don't want that.
    // Make sure that we set the right format for the MQ JMS classes
    if (status.equalsIgnoreCase("true") || status.equalsIgnoreCase("ON")) {
      this.status = "ON";
    }
    else {
      this.status = "OFF";
    }
  }

  public void setMaxTraceBytes(String maxTraceBytes) {
    this.maxTraceBytes = maxTraceBytes;
  }

  public void setTraceFileLimit(String traceFileLimit) {
    this.traceFileLimit = traceFileLimit;
  }

  public void setTraceFileCount(String traceFileCount) {
    this.traceFileCount = traceFileCount;
  }

  public void setParameterTrace(String parameterTrace) {
    this.parameterTrace = parameterTrace;
  }

  // Always set the FFDC variables regardless of the app's choice of an FFDC/FFST attribute
  public void setFfdcSuppress(String ffdcSuppress) {
    this.ffdcSuppress = ffdcSuppress;
  }

  public void setFfdcSuppressProbeIDs(String ffdcSuppressProbeIDs) {
    this.ffdcSuppressProbeIDs = ffdcSuppressProbeIDs;
  }

  public void setFfstSuppress(String ffstSuppress) {
    this.ffdcSuppress = ffstSuppress;
  }

  public void setFfstSuppressProbeIDs(String ffstSuppressProbeIDs) {
    this.ffdcSuppressProbeIDs = ffstSuppressProbeIDs;
  }

  public void setLogFile(String logFile) {
    this.logFile = logFile;
  }

  // This file seems to also be used for some of the additional output when
  // an FFDC is created
  public void setTraceFile(String traceFile) {
    this.traceFile = traceFile;
  }

  public void setFFDCPath(String ffdcPath) {
    this.ffdcPath = ffdcPath;
  }

  public void setFFSTPath(String ffstPath) {
    this.ffdcPath = ffstPath; // Note that we always set FFDC path (not the FFST variable)
  }

  // This is weird, but we need a file to point at for the Diagnostics* properties. The file can be empty, in which
  // case values are read from System Properties, but it must exist and be readable.
  // We create it in a static locked method so there's only going to be one file. And it is automatically deleted on exit.
  private static synchronized void createTempFile() throws IOException {
    if (tempFile == null) {
      tempFile = File.createTempFile("ibmmqsbtc", ".tmp");
      tempFile.deleteOnExit();
    }
  }

  // Set a system property, and record in a local map that we have done it so it can be traced later
  void setSysProp(String k, String v) {
    if (U.isNotNullOrEmpty(v)) {
      System.setProperty(k, v);
      setProps.put(k, v);
    }
  }

  // Split out the diagnostics options in case we want to do
  // anything special. But for now, just go direct to the 
  // setSysProp option.
  void setDiagProp(String k, String v) {
    setSysProp(k, v);
  }

  // Set all the values needed to control tracing
  void setProperties() {

    setProps.clear();

    // The FFDC path is the only property that forces us to use the Diagnostics set of properties. If
    // we are using that, then we need to create a temporary file and point at it. We also (optionally) turn on trace
    // with that set of properties. If FFDC path is not set, then any FDCs are created under the directory containing the
    // trace files. 
    if (U.isNotNullOrEmpty(ffdcPath)) {
      try {
        createTempFile();

        // Setting this will cause creation of a small trace file, even if trace itself is not enabled
        setSysProp(propertyDiagnosticsFile, tempFile.getAbsolutePath());
        setDiagProp(propertyTraceFilePath, traceFile);
        setDiagProp(propertyFFDCPath, ffdcPath); // Either attribute maps to the same external property
        setSysProp(propertyErrorFileName, logFile);

        if (status.equalsIgnoreCase("ON")) {
          setDiagProp(propertyTraceDetail, "high");
          setDiagProp(propertyTraceFile, "enabled");
        }
        else {
          setDiagProp(propertyTraceDetail, "low");
          setDiagProp(propertyTraceFile, "disabled");
        }

        //writer.close();
      }
      catch (IOException e) {
        logger.error("Problem creating temporary file:", e);
      }
    }
    else {
      // Otherwise we turn on trace using the more common property.
      setSysProp(propertyTraceStatus, status);
      setSysProp(propertyTraceFileName, traceFile);
    }

    setSysProp(propertyMaxTraceBytes, maxTraceBytes);
    setSysProp(propertyTraceFileLimit, traceFileLimit);
    setSysProp(propertyTraceFileCount, traceFileCount);
    setSysProp(propertyParameterTrace, parameterTrace.toUpperCase());
    setSysProp(propertyFFDCSuppress, ffdcSuppress);

    setSysProp(propertyFFDCSuppressProbeIDs, ffdcSuppressProbeIDs);
    setSysProp(propertyLogFileName, logFile);

  }

  /*
   * The getters are never used, but we need them so that Spring can discover the attributes properly
   */
  public String getStatus() {
    return status;
  }

  public String getMaxTraceBytes() {
    return maxTraceBytes;
  }

  public String getTraceFileLimit() {
    return traceFileLimit;
  }

  public String getTraceFileCount() {
    return traceFileCount;
  }

  public String getParameterTrace() {
    return parameterTrace;
  }

  public String getFfdcSuppress() {
    return ffdcSuppress;
  }

  public String getFfdcSuppressProbeIDs() {
    return ffdcSuppressProbeIDs;
  }

  public String getFfstSuppress() {
    return ffstSuppress;
  }

  public String getFfstSuppressProbeIDs() {
    return ffstSuppressProbeIDs;
  }

  public String getLogFile() {
    return logFile;
  }

  public String getTraceFile() {
    return traceFile;
  }

  public String getFFDCPath() {
    return ffdcPath;
  }

  public String getFFSTPath() {
    return ffstPath;
  }

  // Pass in the logger so it appears to be coming from the parent
  public void traceProperties(Logger parentLogger) {
    if (!parentLogger.isTraceEnabled())
      return;

    if (setProps.size() == 0) {
      parentLogger.trace("Trace Props Map: {}", "Empty");
    }
    else {
      parentLogger.trace("Trace Props Map:");
      ArrayList<String> ks = new ArrayList<String>(setProps.keySet());
      Collections.sort(ks);
      for (String s : ks) {
        parentLogger.trace("  {} : {}", s, setProps.get(s));
      }
    }
  }
}
