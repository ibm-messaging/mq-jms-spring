# IBM MQ JMS Spring Components

This repository contains code to help to provide Spring developers with easy configuration of the IBM MQ JMS package.

The library contains:
* `mq-jms-spring-boot-starter` for [Spring Boot 2](https://projects.spring.io/spring-boot/) applications

## Installation and Usage
If the VERSION file contains "LOCAL" in the version definition, then the gradle build process puts the
generated jar into your local directory tree. It can then be referenced from the application build.

Otherwise it can be automatically downloaded from Maven Central.

### Spring Boot Applications


Gradle:

```
repositories {
   mavenLocal()
   mavenCentral()
}

dependencies {
    compile group: 'com.ibm.mq', name: 'mq-jms-spring-boot-starter', version: '2.0.5'
}
```

Maven:

``` xml
<dependency>
  <groupId>com.ibm.mq</groupId>
  <artifactId>mq-jms-spring-boot-starter</artifactId>
  <version>2.0.5</version>
</dependency>
```

**Note** This repository and the corresponding Maven Central artifact has now been upgraded for
Spring Boot 2 applications. For Spring Boot 1, you should continue to use the previously-released
artifact at version 0.0.4.

## Design Approach
The approach taken here is to follow the model for JMS applications shown in the
[Spring Getting Started Guide for JMS](https://spring.io/guides/gs/messaging-jms/). That in turn is based on using the [JmsTemplate Framework](https://docs.spring.io/spring/docs/4.3.13.RELEASE/spring-framework-reference/htmlsingle/#jms)

The same application code from that example ought to work with MQ, with the simple replacement of the messaging provider in its dependency to point at this package, and changing the queue name ("mailbox" in that example) to "DEV.QUEUE.1", which is created automatically in the Docker-packaged MQ server.

Essentially what gets configured from this package is a ConnectionFactory which Spring's JmsTemplate implementation
exploits to provide a simpler interface.

## Getting Started

To get started quickly, you can use the default configuration settings in this package along with the
IBM MQ for Developers container which runs the server processes.

### Default Configuration
The default options have been selected to match the
[MQ Docker container](https://github.com/ibm-messaging/mq-docker) development configuration.

This means that you can run a queue manager using that Docker environment and connect to it. This script
will run the container on a Linux system.

~~~
docker run --env LICENSE=accept --env MQ_QMGR_NAME=QM1 \
           --publish 1414:1414 \
           --publish 9443:9443 \
           --detach \
           ibmcom/mq
~~~

The default attributes are

    ibm.mq.queueManager=QM1
    ibm.mq.channel=DEV.ADMIN.SVRCONN
    ibm.mq.connName=localhost(1414)
    ibm.mq.user=admin
    ibm.mq.password=passw0rd

### Extended Configuration Options
If you already have a running MQ queue manager that you want to use, then you can easily modify the
default configuration to match by providing override values.

The queue manager name is given as
* `ibm.mq.queueManager`

For client connections to a queue manager, you must also set either
* `ibm.mq.channel`
* `ibm.mq.connName`
or
* `ibm.mq.ccdtUrl`
If both the channel and connName are not supplied, and the CCDTURL is not supplied,
then a local queue manager is assumed. The CCDTURL property is taken in preference to
the channel and connName.

Optionally you can provide a [client id](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_9.0.0/com.ibm.mq.ref.dev.doc/q112000_.html) if required.
* `ibm.mq.clientId`

You will probably also need to set
* `ibm.mq.user`
* `ibm.mq.password`
to override the default values. These attributes can be set to an empty value, to use the local OS userid
automatically with no authentication (if the queue manager has been set up to allow that).

For example in an `application.properties` file:

    ibm.mq.queueManager=QM1
    ibm.mq.channel=SYSTEM.DEF.SVRCONN
    ibm.mq.connName=server.example.com(1414)
    ibm.mq.user=user1
    ibm.mq.password=passw0rd

Spring Boot will then create a ConnectionFactory that can then be used to interact with your queue manager.

#### TLS related options
The following options all default to null, but may be used to assist with configuring TLS

| Option                      | Description                                                                     |
| --------------------------- | -----------                                                                     |
| ibm.mq.sslCipherSuite       | Cipher Suite, sets connectionFactory property WMQConstants.WMQ_SSL_CIPHER_SUITE |
| ibm.mq.sslCipherSpec        | Cipher Spec,  sets connectionFactory property WMQConstants.WMQ_SSL_CIPHER_SPEC  |
| ibm.mq.sslPeerName          | Peer Name,  sets connectionFactory property WMQConstants.WMQ_SSL_PEER_NAME      |
| ibm.mq.useIBMCipherMappings | Sets System property com.ibm.mq.cfg.useIBMCipherMappings                        |

## Related documentation
* [MQ documentation](https://www.ibm.com/support/knowledgecenter/en/SSFKSJ_9.0.0/com.ibm.mq.helphome.v90.doc/WelcomePagev9r0.htm)
* [Spring Boot documentation](https://projects.spring.io/spring-boot/)
* [Spring Framework documentation](https://projects.spring.io/spring-framework/)

# Development
### Building for a Maven repository
The VERSION file in this directory contains the version number associated with the build.
For example, "0.1.2-SNAPSHOT".

Output from the build can be uploaded to a Maven repository.

The uploadArchives task controls publishing of the output. It uses the VERSION number to
determine what to do, along with an environment variable.
This means that we can build a non-SNAPSHOT version while still not pushing it out and
the github version of the file can match exactly what was built.

* If the version contains 'SNAPSHOT' that we will use that temporary repo in the Central Repository.
else we push to the RELEASE repository
* If the version contains 'LOCAL'  or the environment variable "PushToMaven" is not set
** then the output will be copied to a local Maven repository
under the user's home directory (~/.m2/repository).
** else we attempt to push the jar files to the Nexus Central Repository.

If pushing to the Nexus Release area, then once the build has been successfully transferred
you must log into Nexus to do the final promotion (CLOSE/RELEASE) of the artifact. Although it is
possible to automate that process, I am not doing it in this build file so we do a manual check
that the build has been successful and to check validity before freezing a version number.

Using Nexus Central Repository requires authentication and authorisation. The userid and password
associated with the account are held in a local file (gradle.properties) that is not part
of this public repository. That properties file also holds information about the signing key that Nexus
requires.

    ---- Example gradle.properties file --------
    # These access the GPG key and certificate
    signing.keyId=AAA111BB
    signing.password=MyPassw0rd
    signing.secretKeyRingFile=/home/user/.gnupg/secring.gpg
    # This is the authentication to Nexus
    ossrhUsername=myNexusId
    ossrhPassword=MyOtherPassw0rd
    --------------------------------------------

### Pull requests
Contributions to this package can be accepted under the terms of the
IBM Contributor License Agreement, found in the file CLA.md of this repository.

When submitting a pull request, you must include a statement stating you accept the terms in CLA.md.

### Using in Other Projects

The preferred approach for using this package in other projects will be to use the Gradle or Maven dependency as described above.

### License

Copyright © 2018 IBM Corp. All rights reserved.

Licensed under the apache license, version 2.0 (the "license"); you may not use this file except in compliance with the license.  you may obtain a copy of the license at

    http://www.apache.org/licenses/LICENSE-2.0.html

Unless required by applicable law or agreed to in writing, software distributed under the license is distributed on an "as is" basis, without warranties or conditions of any kind, either express or implied. See the license for the specific language govern
ng permissions and limitations under the license.

### Issues

Before opening a new issue please consider the following:
* Please try to reproduce the issue using the latest version.
* Please check the [existing issues](https://github.com/ibm-messaging/mq-spring/issues)
to see if the problem has already been reported. Note that the default search
includes only open issues, but it may already have been closed.
* When opening a new issue [here in github](../../issues) please complete the template fully.
