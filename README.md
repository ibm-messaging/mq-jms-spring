# IBM MQ JMS Spring Components

This repository contains code to help to provide Spring developers with easy configuration of the IBM MQ JMS package.

The library contains:

-   `mq-jms-spring-boot-starter` for [Spring Boot](https://projects.spring.io/spring-boot/) applications

NOTE: Spring Boot 2 has now reached its end of non-commercial service life.
So version 2.7.18 is the last update based on Spring 2. Further updates will
follow the Spring 3 path only. If you want to continue to use Spring 2 with future versions of the MQ jars,
then overriding the version inherited from the mq-jms-spring-boot in your parent pom.xml should be possible.
However, this would not give easy access via configuration to any new features available in the MQ client.

## Installation and Usage

The compiled versions of this package can be automatically downloaded from Maven Central.

For local modifications and building it yourself, you can use the `RUNME.sh` script. It uses gradle as the build
mechanism and has tasks that can push compiled jars to either a local repository (typically under `$HOME/.m2`) or to
Maven Central. When signing/authentication of modules is required, use the `gradle.properties.template` file as a
starter for your own `gradle.properties`.

Java 17 is required as the compiler level when building this package, as that is the baseline for Spring 3.

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

## Design Approach

The approach taken here is to follow the model for JMS applications shown in the
[Spring Getting Started Guide for JMS](https://spring.io/guides/gs/messaging-jms/). That in turn
is based on using the [JmsTemplate Framework](https://docs.spring.io/spring/docs/current/spring-framework-reference/integration.html#jms-jmstemplate)

Some simple example programs using Spring Boot and JMS interfaces can be found in the samples directory. The RUNME.sh program in
each subdirectory compiles and executes it. The application.properties files in that tree may need modification for your environment.

Essentially what gets configured from this package are a ConnectionFactory which Spring's JmsTemplate implementation
exploits to provide a simpler interface, and a MessageListener.

## Getting Started

To get started quickly, you can use the default configuration settings in this package along with the
IBM MQ for Developers container which runs the server processes.

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
               ibmcom/mq

The default attributes are

    ibm.mq.queueManager=QM1
    ibm.mq.channel=DEV.ADMIN.SVRCONN
    ibm.mq.connName=localhost(1414)
    ibm.mq.user=
    ibm.mq.password=

### Connection security

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


#### JWT Tokens
If your queue manager supports authentication with JWT tokens, then you can either set `ibm.mq.token` or the password to
the token. If you use the `password` attribute, then the `user` must also be set to the empty value (which is now the
default anyway). Obtaining the token is outside the scope of this package; there are a number of ways that can be done
through Spring Boot making HTTP calls to the JWT server.

One approach to handling this without needing to put the token into the external configuration properties file is to make the
HTTP call and then `System.setProperty("ibm.mq.token",token)`.

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

    ibm.mq.queueManager=QM1
    ibm.mq.channel=SYSTEM.DEF.SVRCONN
    ibm.mq.connName=server.example.com(1414)
    ibm.mq.user=user1
    ibm.mq.password=passw0rd

Spring Boot will then create a ConnectionFactory that can then be used to interact with your queue manager.

| Option                      | Description                                                                     |
| --------------------------- | ------------------------------------------------------------------------------- |
| ibm.mq.queueManager         | Name of queue manager                                                           |
| ibm.mq.channel              | Channel Name for SVRCONN                                                        |
| ibm.mq.connName             | Connection Name, which can be comma-separated list                              |
| ibm.mq.ccdtUrl              | Location of the MQ CCDT file (URL can reference http/ftp location)              |
| ibm.mq.user                 | User Name. Default is empty string                                              |
| ibm.mq.password             | Password. Default is empty string                                               |
| ibm.mq.token                | JWT token                                                                       |
| ibm.mq.clientId             | ClientId uniquely identifies the app connection for durable subscriptions       |
| ibm.mq.applicationName      | Application Name used for Uniform Cluster balancing                             |
| ibm.mq.userAuthenticationMQCSP| Control authentication mechanism for old queue managers (default true)        |
| ibm.mq.tempQPrefix          | The prefix to be used to form the name of an MQ dynamic queue                   |
| ibm.mq.tempTopicPrefix      | The prefix to be used to form the name of an MQ dynamic topic                   |
| ibm.mq.tempModel            | The name of a model queue for creating temporary destinations                   |
| ibm.mq.reconnect            | Whether app tries automatic reconnect. Options of YES/NO/QMGR/DISABLED/DEFAULT  |
| ibm.mq.reconnectTimeout     | Timeout in seconds before automatic reconnect gives up                          |
| ibm.mq.autoConfigure        | If explicitly set to "false", then the autoconfigure bean is disabled           |
| ibm.mq.balancingApplicationType | Hint how uniform clusters should treat the app. Options of SIMPLE/REQREP    |
| ibm.mq.balancingTimeout     | Uniform cluster timer. Options NEVER/DEFAULT/IMMEDIATE or integer seconds       |
| ibm.mq.balancingOptions     | Rebalancing options. Options of NONE/IGNORETRANS. Default NONE.                 |

The `reconnect` option was previously named `defaultReconnect` but both names work in the configuration.

#### TLS related options

The following options all default to null, but may be used to assist with configuring TLS

| Option                      | Description                                                                     |
| --------------------------- | ------------------------------------------------------------------------------- |
| ibm.mq.sslCipherSuite       | Cipher Suite, sets connectionFactory property WMQConstants.WMQ_SSL_CIPHER_SUITE |
| ibm.mq.sslCipherSpec        | Cipher Spec,  sets connectionFactory property WMQConstants.WMQ_SSL_CIPHER_SPEC  |
| ibm.mq.sslPeerName          | Peer Name,    sets connectionFactory property WMQConstants.WMQ_SSL_PEER_NAME    |
| ibm.mq.useIBMCipherMappings | Sets System property com.ibm.mq.cfg.useIBMCipherMappings                        |
| ibm.mq.outboundSNI          | Sets property com.ibm.mq.cfg.SSL.OutboundSNI (use HOSTNAME for Openshift qmgrs) |
| ibm.mq.channelSharing       | Sets strategy for TCP/IP connection sharing - CONNECTION or GLOBAL              |

We also have

| Option                      | Description                                                                     |
| --------------------------- | ------------------------------------------------------------------------------- |
| ibm.mq.sslFIPSRequired      | Force FIPS-compliant algorithms to be used (default false)                      |
| ibm.mq.sslKeyResetCount     | How many bytes to send before resetting the TLS keys                            |
| ibm.mq.sslCertificateValPolicy | If "none", do not check the server certificate is trusted                    |

and

| Option                          | Description                                                                  |
| ------------------------------- | ---------------------------------------------------------------------------- |
| ibm.mq.jks.trustStore           | Where is the store holding trusted certificates                              |
| ibm.mq.jks.trustStorePassword   | Password for the trustStore                                                  |
| ibm.mq.jks.keyStore             | Where is the keystore with a personal key and certificate                    |
| ibm.mq.jks.keyStorePassword     | Password for the keyStore                                                    |

These JKS options are an alternative to setting the `javax.net.ssl` system properties, usually done on the command line.

An alternative preferred approach for setting the key/truststores is available from Spring 3.1, which introduced the
concept of "SSL Bundles". This makes it possible to have different SSL configurations - keystores, truststores etc - for
different components executing in the same Spring-managed process. See
[here](https://spring.io/blog/2023/06/07/securing-spring-boot-applications-with-ssl) for a description of the options
available. Each bundle has an identifier with the `spring.ssl.bundle.jks.<key>` tree of options. The key can be
specified for this package with `ibm.mq.sslBundle` which then uses the Spring elements to create the connection
configuration. The default value for this key is empty, meaning that `SSLBundles` will not be used; the global SSL
configuration is used instead. However the `ibm.mq.jks` properties are now marked as deprecated.

| Option                          | Description                                                                  |
| ------------------------------- | ---------------------------------------------------------------------------- |
| ibm.mq.sslBundle                | Spring Boot option (from 3.1) for granular certificate configuration         |

To achieve the same effect with Spring 2.x, you could use your own code to create an `SSLSocketFactory` object
which can be applied to the MQ Connection Factory in a `customise` method before the CF is invoked.

#### Caching connection factory options

You may want to use the default Spring Caching connection factory with the default Spring JMS properties. This is now the
preferred method in Spring for holding JMS objects open, rather than the Pooling options described below.

| Option                              | Description                                      |
| ----------------------------------- | ------------------------------------------------ |
| spring.jms.cache.enabled            | Whether to cache sessions (default true)         |
| spring.jms.cache.consumers          | Whether to cache message consumers               |
| spring.jms.cache.producers          | Whether to cache message producers               |
| spring.jms.cache.session-cache-size | Size of the session cache (per JMS Session type) |

#### Pooled connection factory options

Alternatively you may configure a pooled connection factory by using these properties:

| Option                                 | Description                                                                                                                              |
| -------------------------------------- | ---------------------------------------------------------------------------------------------------------------------------------------- |
| ibm.mq.pool.enabled                    | Enabled Pooled connection factory usage                                                                                                  |
| ibm.mq.pool.blockIfFull                | Blocks a connection request when the pool is full. Default is false                                                                      |
| ibm.mq.pool.blockIfFullTimeout         | Blocking period before throwing an exception if the pool is still full                                                                   |
| ibm.mq.pool.idleTimeout                | Connection idle timeout. Default to 30 seconds                                                                                           |
| ibm.mq.pool.maxConnections             | Maximum number of pooled connections. Default is 1                                                                                       |
| ibm.mq.pool.maxSessionsPerConnection   | Maximum number of pooled sessions. Default is 500                                                                                        |
| ibm.mq.pool.timeBetweenExpirationCheck | Time to sleep between runs of the idle connection eviction thread. Disable when negative. Default is -1                                  |
| ibm.mq.pool.useAnonymousProducers      | Whether to use only one anonymous "MessageProducer" instance. Set it to false to create one "MessageProducer" every time one is required |

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

You can set the `ibm.mq.jndi.providerUrl` and `ibm.mq.jndi.providerContextFactory` attributes to define how the lookup
is to be carried out. For example,

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

Copyright © 2018, 2023 IBM Corp. All rights reserved.

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
