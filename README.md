# IBM MQ JMS Spring Components

This repository contains code to help to provide Spring developers with easy configuration and testing of the IBM MQ JMS
package.

The library contains:

-   `mq-jms-spring-boot-starter` for [Spring Boot](https://projects.spring.io/spring-boot/) applications
-   `mq-jms-spring-testcontainer` for testing Spring Boot applications
-   `mq-java-testcontainer` for integration with the [Testcontainers](https://testcontainers.org) project

NOTE: Spring Boot 2 has now reached its end of non-commercial service life. So version 2.7.18 is the last update based
on Spring 2. Further updates will follow the Spring 3 path only. If you want to continue to use Spring 2 with future
versions of the MQ jars, then overriding the version inherited from the mq-jms-spring-boot in your parent pom.xml should
be possible. However, this would not give easy access via configuration to any new features available in the MQ client.

NOTE: Spring Boot 4 is under development planned for release later in 2025. While this package does not currently do
anything specific for Boot 4, the pieces are already in place to be able to build against it once it is available.

## Installation and Usage

The compiled versions of this package can be automatically downloaded from Maven Central.

For local modifications and building it yourself, you can use the `RUNME.sh` script. It uses gradle as the build
mechanism and has tasks that can push compiled jars to either a local repository (typically under `$HOME/.m2`) or to
Maven Central. When signing/authentication of modules is required, use the `gradle.properties.template` file as a
starter for your own `gradle.properties`.

Java 17 is required as the compiler level when building this package, as that is the baseline for Spring Boot 3.

### Spring Boot Applications

Gradle:

    repositories {
       mavenLocal()
       mavenCentral()
    }

    dependencies {
        compile group: 'com.ibm.mq', name: 'mq-jms-spring-boot-starter', version: 'x.y.z'
    }

Maven:

```xml
<dependency>
  <groupId>com.ibm.mq</groupId>
  <artifactId>mq-jms-spring-boot-starter</artifactId>
  <version>x.y.x</version>
</dependency>
```

**Note** This repository and the corresponding Maven Central artifacts requires Spring Boot 3. Maven
Central continues to provide older versions that work with Spring Boot 2.

## Testcontainers
For testing Spring Boot applications, you may need to provision a queue manager as part of the process. This can be
done by using the Testcontainers framework. For more information about the MQ package, see [here](README_TESTCONTAINERS.md).
Also look at the `samples/s5` directory for a demonstration.

## Design Approach

The approach taken here is to follow the model for JMS applications shown in the
[Spring Getting Started Guide for JMS](https://spring.io/guides/gs/messaging-jms/). That in turn
is based on using the [JmsTemplate Framework](https://docs.spring.io/spring/docs/current/spring-framework-reference/integration.html#jms-jmstemplate)

Some simple example programs using Spring Boot and JMS interfaces can be found in the samples directory. The RUNME.sh
program in each subdirectory compiles and executes it. The _application.properties_ or _application.yml_ files in that
tree may need modification for your environment.

Essentially what gets configured from this package are a ConnectionFactory which Spring's JmsTemplate implementation
exploits to provide a simpler interface, and a MessageListener.

## Getting Started

To get started quickly, you can use the default configuration settings in this package along with the
IBM MQ Advanced for Developers container which runs the server processes.

### Default Configuration

The default options have been selected to match the
[MQ container](https://github.com/ibm-messaging/mq-container) development configuration.

This means that you can run a queue manager using that environment and connect to it without special
configuration.

This script will run the container on a Linux system.

    docker run --env LICENSE=accept --env MQ_QMGR_NAME=QM1 \
               --publish 1414:1414 \
               --publish 9443:9443 \
               --detach \
               icr.io:ibm-messaging/mq:latest

The default attributes are

    ibm.mq.queueManager=QM1
    ibm.mq.channel=DEV.ADMIN.SVRCONN
    ibm.mq.connName=localhost(1414)
    ibm.mq.user=
    ibm.mq.password=

### Authentication with passwords

The default userid and password have been removed from this package, as the corresponding default configuration has been
removed from the MQ Developer images. Authentication must now be explicitly defined both for the queue manager, and for
the Spring applications.

To revert to the previous default user/password checking, perhaps if you are still using older Developer images, you
must now set the `ibm.mq.user` and `ibm.mq.password` attribute.

```
   ibm.mq.user=admin
   ibm.mq.password=passw0rd
```
Configuration of secure connections with TLS are discussed below.


### Authentication with JWT tokens
If the queue manager has been configured to authenticate applications based on JWT tokens, then those tokens can be
provided through the JMS layer.

#### MQ-retrieved tokens
The JMS client code can generate and retrieve tokens from a server automatically, without needing the application to
explicitly contact the token server itself. The application needs configuration of how to contact the server, but then
the communication to the token server (eg Keycloak) is handled automatically. The `ibm.mq.tokenServer` section of the
configuration provides the route and authentication mechanism for the server.

**Note:** There are some current constraints on how this mechanism works, with respect to the HTTPS connection that needs
to be made to the token server:
* The keystore/truststore uses process-wide environment variables that might affect other components of the application
  if they also use the `javax.net.ssl` properties.
* The keystore/truststore must be local JKS files, not embedded in the jar and its classpath
* The `validateCertificatePolicy` setting cannot be used to always trust a token server's certificate


#### Explicity-provided tokens
Having the token provided directly in the application configuration continues to work, but is not the recommended
approach, now that the MQ JMS layer can do that retrieval itself. To use this method, you can either set `ibm.mq.token`
or the password to the token. If you use the `password` attribute, then the `user` must also be set to the empty value
(which is now the default anyway).

### Configuration Options

If you already have a running MQ queue manager that you want to use, then you can easily modify the default
configuration to match by providing override values.

The queue manager name is given as

-   `ibm.mq.queueManager`

For client connections to a queue manager, you must also have either

-   `ibm.mq.channel`
-   `ibm.mq.connName`
    or
-   `ibm.mq.ccdtUrl`

If both the channel and connName are empty, and the CCDTURL is not supplied, then a local queue manager is assumed. The
CCDTURL property is taken in preference to the channel and connName. The channel and connName have non-blank defaults,
so must be explicitly set to empty strings if you do not wish them to be used.

Optionally you can provide a [client id](https://www.ibm.com/docs/en/ibm-mq/latest?topic=objects-clientid)
and [application name](https://www.ibm.com/docs/en/ibm-mq/latest?topic=applications-specifying-application-name-in-supported-programming-languages) if required.

-   `ibm.mq.clientId`
-   `ibm.mq.applicationName`

You will probably also need to set

-   `ibm.mq.user`
-   `ibm.mq.password`

to override the default values.

For example in an `application.properties` file:

```
    ibm.mq.queueManager=QM1
    ibm.mq.channel=SYSTEM.DEF.SVRCONN
    ibm.mq.connName=server.example.com(1414)
    ibm.mq.user=user1
    ibm.mq.password=passw0rd
```

Or in the equivalent `application.yml` file:

```
    ibm:
      mq:
        queueManager: QM1
        channel: SYSTEM.DEF.SVRCONN
        connName: server.example.com(1414)
        user: user1
        password: passw0rd
```

Spring Boot will then create a ConnectionFactory that can then be used to interact with your queue manager.

| Option (ibm.mq)          | Description                                                                     |
| -------------------------| ------------------------------------------------------------------------------- |
| queueManager             | Name of queue manager                                                           |
| channel                  | Channel Name for SVRCONN                                                        |
| connName                 | Connection Name, which can be comma-separated list                              |
| ccdtUrl                  | Location of the MQ CCDT file (URL can reference http/ftp location)              |
| user                     | User Name. Default is empty string                                              |
| password                 | Password. Default is empty string                                               |
| token                    | JWT token                                                                       |
| clientId                 | ClientId uniquely identifies the app connection for durable subscriptions       |
| applicationName          | Application Name used for Uniform Cluster balancing                             |
| userAuthenticationMQCSP  | Control authentication mechanism for old queue managers (default true)          |
| tempQPrefix              | The prefix to be used to form the name of an MQ dynamic queue                   |
| tempTopicPrefix          | The prefix to be used to form the name of an MQ dynamic topic                   |
| tempModel                | The name of a model queue for creating temporary destinations                   |
| reconnect                | Whether app tries automatic reconnect. Options of YES/NO/QMGR/DISABLED/DEFAULT  |
| reconnectTimeout         | Timeout in seconds before automatic reconnect gives up                          |
| autoConfigure            | If explicitly set to "false", then the autoconfigure bean is disabled           |
| balancingApplicationType | Hint how uniform clusters should treat the app. Options of SIMPLE/REQREP        |
| balancingTimeout         | Uniform cluster timer. Options NEVER/DEFAULT/IMMEDIATE or integer seconds       |
| balancingOptions         | Rebalancing options. Options of NONE/IGNORETRANS. Default NONE.                 |
| balancingInstanceMode    | Rebalancing. Set to JVM to treat all app names in this process as equivalent.   |

The `reconnect` option was previously named `defaultReconnect` but both names work in the configuration.

For contacting a Token Server, these options define its address and authentication. All three properties
have to be supplied if you are using this authentication mechanism:

| Option (ibm.mq.tokenServer)  | Description                                                                    |
| -----------------------------| ------------------------------------------------------------------------------ |
| endpoint                     | URL pointing at the token server (eg https://my.keycloak.server)               |
| clientId                     | The ClientId for authentication to the server (unrelated to ibm.mq.clientId)   |
| clientSecret                 | The ClientSecret for authentication to the server                              |

#### TLS related options

The preferred approach for setting the key/truststores is available from Spring 3.1, which introduced the
concept of "SSL Bundles". This makes it possible to have different SSL configurations - keystores, truststores etc - for
different components executing in the same Spring-managed process. See
[here](https://spring.io/blog/2023/06/07/securing-spring-boot-applications-with-ssl) for a description of the options
available. Each bundle has an identifier with the `spring.ssl.bundle.jks.<key>` tree of options. The key can be
specified for this package with `ibm.mq.sslBundle` which then uses the Spring elements to create the connection
configuration. The default value for this key is empty, meaning that `SSLBundles` will not be used; the global SSL
configuration is used instead. However the `ibm.mq.jks` properties are now marked as deprecated.

| Option (ibm.mq)      | Description                                                                  |
| -------------------- | ---------------------------------------------------------------------------- |
| sslBundle            | Spring Boot option (from 3.1) for granular certificate configuration         |

The following options all default to null, but may also be used to assist with configuring TLS

| Option (ibm.mq)      | Description                                                                     |
| -------------------- | ------------------------------------------------------------------------------- |
| sslCipherSuite       | Cipher Suite, sets connectionFactory property WMQConstants.WMQ_SSL_CIPHER_SUITE |
| sslCipherSpec        | Cipher Spec,  sets connectionFactory property WMQConstants.WMQ_SSL_CIPHER_SPEC  |
| sslPeerName          | Peer Name,    sets connectionFactory property WMQConstants.WMQ_SSL_PEER_NAME    |
| useIBMCipherMappings | Sets System property com.ibm.mq.cfg.useIBMCipherMappings                        |
| outboundSNI          | Sets property com.ibm.mq.cfg.SSL.OutboundSNI (use HOSTNAME for Openshift qmgrs) |
| channelSharing       | Sets strategy for TCP/IP connection sharing - CONNECTION or GLOBAL              |

We also have

| Option (ibm.mq)         | Description                                                     |
| ----------------------- | ----------------------------------------------------------------|
| sslFIPSRequired         | Force FIPS-compliant algorithms to be used (default false)      |
| slKeyResetCount         | How many bytes to send before resetting the TLS keys            |
| sslCertificateValPolicy | If "none", do not check the server certificate is trusted       |

and

| Option (ibm.mq.jks)  | Description                                                                  |
| ---------------------| ---------------------------------------------------------------------------- |
| trustStore           | Where is the store holding trusted certificates                              |
| trustStorePassword   | Password for the trustStore                                                  |
| keyStore             | Where is the keystore with a personal key and certificate                    |
| keyStorePassword     | Password for the keyStore                                                    |

These deprecated JKS options are an alternative to setting the `javax.net.ssl` system properties, usually done on the
command line. They are not used if you have set the `sslBundle` property.

#### Caching connection factory options

You may want to use the default Spring Caching connection factory with the default Spring JMS properties. This is now the
preferred method in Spring for holding JMS objects open, rather than the Pooling options described below.

| Option (spring.jms.cache)   | Description                                      |
| --------------------------- | ------------------------------------------------ |
| enabled                     | Whether to cache sessions (default true)         |
| consumers                   | Whether to cache message consumers               |
| producers                   | Whether to cache message producers               |
| session-cache-size          | Size of the session cache (per JMS Session type) |

#### Pooled connection factory options

Alternatively you may configure a pooled connection factory by using these properties:

| Option (ibm.mq.pool)       | Description                                                                                                                              |
| -------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| enabled                    | Enabled Pooled connection factory usage                                                                                                  |
| blockIfFull                | Blocks a connection request when the pool is full. Default is false                                                                      |
| blockIfFullTimeout         | Blocking period before throwing an exception if the pool is still full                                                                   |
| idleTimeout                | Connection idle timeout. Default to 30 seconds                                                                                           |
| maxConnections             | Maximum number of pooled connections. Default is 1                                                                                       |
| maxSessionsPerConnection   | Maximum number of pooled sessions. Default is 500                                                                                        |
| timeBetweenExpirationCheck | Time to sleep between runs of the idle connection eviction thread. Disable when negative. Default is -1                                  |
| useAnonymousProducers      | Whether to use only one anonymous "MessageProducer" instance. Set it to false to create one "MessageProducer" every time one is required |

These pooling options make use of the [PooledJMS](https://github.com/messaginghub/pooled-jms) implementation. More documentation on
the options can be found [here](https://github.com/messaginghub/pooled-jms/blob/master/pooled-jms-docs/Configuration.md).

### JMS Polling Listener Timer configuration

The Spring AbstractPollingMessageListenerContainer interface has a default polling timer of 1 second. This can be
configured with the `spring.jms.listener.receiveTimeout` property. If the property is not explicitly set, then this MQ
Spring Boot component resets the initial timeout value to 30 seconds which has been shown to be more cost-effective.
Application code can still set its own preferred value.

| Option                              | Description                                                                                                                   |
| ----------------------------------- | ----------------------------------------------------------------------------------------------------------------------------- |
| spring.jms.listener.receiveTimeout  | How frequently to poll for received messages. Default is 1s. Given as a Duration string: "1m", "60s", "60000" are equivalent  |

### MQ JMS Tracing and Logging

The MQ JMS client libraries have a large set of options to control their own tracing and logging behaviour. See for
example
[this page](https://www.ibm.com/docs/en/ibm-mq/latest?topic=mcjcf-using-java-standard-environment-trace-configure-java-trace).
This package exposes some of these options, so they can be set using regular Spring properties, without needing to be
put into System properties and/or additional external files.Some of these options interact in potentially surprising
ways, as to what gets printed where (to files, stdout/stderr etc). So you might need to experiment, or revert to the
full control of setting the separate MQ-documented properties files. If the `ffdcPath` attribute is not set, then FFDCs are
created in the FFDC directory under the `traceFile` directory. Note that some of the documented MQ attributes use "ffst", while some
use "ffdc". I've tried to be consistent here and used "ffdc" as that is how the files are actually named.

| Option (ibm.mq.trace)  | Description                                                                                                |
| -----------------------| ---------------------------------------------------------------------------------------------------------- |
| status                 | ON or OFF to control overall tracing                                                                       |
| maxTraceBytes          | Limits on the trace output                                                                                 |
| traceFileLimit         | Limits on the trace output                                                                                 |
| traceFileCount         | Limits on the trace output                                                                                 |
| parameterTrace         | true or false to control level of tracing                                                                  |
| logFile                | Error log filename                                                                                         |
| traceFile              | Trace log filename or directory. Can use %PID% in the name as a placeholder                                |
| ffdcSuppress           | Suppress repeated instances of each FFDC                                                                   |
| ffdcSuppressProbeIDs   | Completely suppress these specific FFDC Probes                                                             |
| ffdcPath               | Directory for FFDCs. Generation of FFDCs cannot be fully suppressed - this directory must be writable.     |

### Additional properties

Additional properties that are not in the recognised sets listed here can be put onto the Connection Factory via a map
in the external properties definitions. Use the format `ibm.mq.additionalProperties.CONSTANT_NAME=value`. The
CONSTANT_NAME can be either the real string for the property, and will often begin with "XMSC", or it can be the
variable as known in the WMQConstants class.

For example, the constant `WMQConstants.WMQ_SECURITY_EXIT` has the value `"XMSC_WMQ_SECURITY_EXIT"` and can be written
in the properties file either as `ibm.mq.additionalProperties.XMSC_WMQ_SECURITY_EXIT=com.example.SecExit` or as
`ibm.mq.additionalProperties.WMQ_SECURITY_EXIT=com.example.SecExit`

There is no error checking on the property name or value. This may help with enabling rarely-used properties and reduce
the need for a customizer method in application code. See
[the KnowledgeCenter](https://www.ibm.com/docs/en/ibm-mq/latest?topic=messaging-mqconnectionfactoryconnectionfactoryproperty)
for a list of all the currently-recognised properties that may be set on a CF - though note that many are now
deprecated.

If the value looks like a number, it is treated as such. You can use hex constants beginning "0X" or decimals for a
number. Similarly if the value is TRUE/FALSE then that is processed as a boolean. So you cannot try to set a string
property that appears to be an integer. Symbols representing the value of integer attributes cannot be used - the real
number must be given.

## JNDI
Spring already has configuration parameters for the use of a JNDI repository with a JMS program. See the
[Spring documentation](https://docs.spring.io/spring-framework/docs/3.2.x/spring-framework-reference/html/jms.html) for
more details.

However this package also enables some simple use of JNDI for Connection definitions (but not Destinations, as they are
still always handled by the core Spring classes).

| Option (ibm.mq.jndi)   | Description                                 |
| -----------------------| ------------------------------------------- |
| providerUrl            | Location of the directory                   |
| providerContextFactory | Class implementing the directory            |
| additionalProperties   | For further configuration - class-dependent |

For example,

```
  ibm.mq.jndi.providerUrl=file:///home/username/mqjms/jndi
  ibm.mq.jndi.providerContextFactory=com.sun.jndi.fscontext.RefFSContextFactory
```

If you choose to use this mechanism, all of the other queue manager properties that might be defined in your resource
definitions are ignored and not traced in order to avoid confusion. They will instead be picked up from the
ConnectionFactory definition in JNDI. The `queueManager` property is then more accurately used as the ConnectionFactory
name used as the lookup. If you are using an LDAP JNDI provider, then the CF name will be modified if necessary to
always begin with `cn=`.

The `ibm.mq.jndi.additionalProperties` prefix can be used for any other JNDI-related properties that need to be applied
to the *Context* object. The symbolic name of the field from that Java class can be used. For example,

```
ibm.mq.jndi.additionalProperties.SECURITY_CREDENTIALS=passw0rd
```
results in

```
  env.put(Context.SECURITY_CREDENTIALS,"passw0rd")
```

The `MQConnectionFactoryFactory.getJndiContext` method is public so you can use it with your own constructed properties
object and get access to a JNDI Context object - it might make it easier to work with Destinations if you can reuse the
same way of getting directory access.

## Logging & Tracing
The package makes use of the logging capabilities within Spring. You can enable tracing of this specific component in
your application's properties file by setting `logging.level.com.ibm.mq.spring.boot=TRACE`. Otherwise it uses the
standard inheritance of logging configuration from `logging.level.root`downwards.

## Related documentation

-   [MQ documentation](https://www.ibm.com/docs/en/ibm-mq/latest)
-   [Spring Boot documentation](https://projects.spring.io/spring-boot/)
-   [Spring Framework documentation](https://projects.spring.io/spring-framework/)


### Contributions and Pull requests

Contributions to this package can be accepted under the terms of the Developer's Certificate of Origin, found in the
[DCO file](DCO1.1.txt) of this repository. When submitting a pull request, you must include a statement stating you
accept the terms in the DCO.

### Using in Other Projects

The preferred approach for using this package in other projects will be to use the Gradle or Maven dependency as described above.

### License

Copyright © 2018, 2025 IBM Corp. All rights reserved.

Licensed under the apache license, version 2.0 (the "license"); you may not use this file except in compliance with the
license. You may obtain a copy of the license at

    http://www.apache.org/licenses/LICENSE-2.0.html

Unless required by applicable law or agreed to in writing, software distributed under the license is distributed on an
"as is" basis, without warranties or conditions of any kind, either express or implied. See the license for the specific
language governing permissions and limitations under the license.

### Health Warning
This package is provided as-is with no guarantees of support or updates. You cannot use IBM formal support channels
(Cases/PMRs) for assistance with material in this repository. There are also no guarantees of compatibility with any
future versions of the package; the API is subject to change based on any feedback. Versioned releases are made to
assist with using stable APIs.

### Issues

Before opening a new issue please consider the following:

-   Please try to reproduce the issue using the latest version.
-   Please check the [existing issues](https://github.com/ibm-messaging/mq-jms-spring/issues)
    to see if the problem has already been reported. Note that the default search
    includes only open issues, but it may already have been closed.
-   When opening a new issue [here in github](https://github.com/ibm-messaging/mq-jms-spring/issues) please complete the template fully.
