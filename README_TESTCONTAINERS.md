# Testcontainers for IBM MQ

This repo introduces enablement for the [testcontainers](https://testcontainers.org) project.

The project simplifies use of various resources as part of an application's automated testing, making it convenient for
use in things like CI pipelines.

For this MQ update, there are two new jar files created and required, one for the base `testcontainers` capability and
one to integrate that with Spring.
* com.ibm.com:mq-java-testcontainer
* com.ibm.com:mq-jms-spring-testcontainer

I'm not going to go into details about using the `testcontainers` framework and how it fits into testing strategies. The
intention is that this MQ support enables all of the common testing patterns in the same way as other resources.

While the testcontainers project has packages for a variety of languages, for now we are only providing a Java version.

## Container image licensing
*NOTE:* The default container referenced in the `MQContainer` class points at the MQ Advanced for Developers image. That
image has license restrictions, constraining it to internal development and unit testing. See
[here](https://www.ibm.com/support/customer/csol/terms/?id=L-HYGL-6STWD6&lc=en) for full terms.

Other licensed container images can be used that do not have the same restrictions. Just name the appropriate image in the
test configuration and provide credentials for authenticating to the container registry.

## Known limitations
The only prebuilt public MQ container image released by IBM is for Linux/x64 systems. Therefore this `testcontainers`
function can only use the default image on that platform. However, it is possible to reference non-public IBM images or
to build your own containers for other platforms - in particular for Linux on Arm64 systems for MacOS developers - and
then refer to that private container image.

Some of the controls assume options from an image built by the
[`mq-container`](https://github.com/ibm-messaging/mq-container) scripts. That includes the default developer objects
such as `DEV.QUEUE.1` and the `app`/`admin` userids.

If you are not using the MQ Advanced for Developers image, then application authentication (in particular) may need some
consideration.

## Sample Test program
The *s5* sample has a basic "normal" application, which is not very interesting. But there is also a "test" option that
uses the SpringRunner framework to execute a simple test. To speed things up in initial repeated testing of the tests,
we make use of a docker volume that you need to pre-create:

```
docker volume create varmqm
cd samples/s5
./RUNME.sh test
```

You should see the container being started, along with the test itself being executed.

Look at the _src/test/java/TestContainersTest.java_ file to see the initial configuration and execution phases. This
test program assumes existence of a docker volume called `varmqm`. While many test scenarios will want to start from
scratch on each run, reusing the data volume can speed things up if we know the queue manager does not need to be
recreated. Remove the line that mounts that volume if you prefer.

The _src/test/resources/99-startup.mqsc_ file has additional MQSC commands to be executed on each startup. The path to
that file is included by default for the Classpath resource loaders at runtime. Also in that directory is some
configuration for the logger, to show execution information.

Look at _build.gradle_ for the required dependencies.

## The MQContainer class
The core of the container management is in the `com.ibm.mq.testcontainers.MQContainer` class. This ends up in the
`mq-java-testcontainer.jar` package.

An image name must always be passed to the constructor for this class. You can use the `MQContainer.DEFAULT_IMAGE` to
select the MQ Advanced for Developers image at whatever level the `latest` tag points at.

There are then methods to apply some simple configuration changes. Not every possible option for connectivity is applied
or exposed; the intention here is to have just enough for basic testing. In particular, nothing is done about TLS
configurations. Public methods include:

* acceptLicense: acknowledge that you understand the license terms for the image
* withAppUser, withAdminUser, withAppPassword, withAdminPassword: control known userids and authentication. The
  default passwords are "app" and "admin".
* withChannel, withQueueManager
* withWebServer: start the web server during container initialisation (default does not start it). Enable this feature
  if you want to test an application using one of the MQ REST APIs for messaging or administration.
* withStartupMQSC: names a single MQSC file on the Classpath that gets loaded into the container for automatic execution
  during queue manager startup. This allows you to create additional objects or reset state if you are reusing an
  existing queue manager (eg because you have mounted an external docker volume).

The `appPassword` ends up being the `password` value used for the Spring Boot JMS connection.

For example:

```
@Bean
@ServiceConnection
public MQContainer mqContainer() {

  // This constructor uses the default image (which has a :latest tag)
  // It also mounts a pre-created Docker volume and starts the web server
  return new MQContainer(MQContainer.DEFAULT_IMAGE)
         .acceptLicense()
         .withStartupMQSC("99-startup.mqsc")
         .withWebServer()
         .withAppPassword("passw0rd")
         .withCreateContainerCmdModifier(cmd ->
            cmd.getHostConfig()
            .withBinds(Bind.parse("varmqm:/var/mqm")));
}
```

### Additional configuration
Many more capabilities can be applied to a container when your test program starts. You may want to set more environment
variables, provide `ini` file updates, configure TLS or setup authentication methods. These can all be done as part of
the container creation process. But many of those options are perhaps going beyond what is necessary for a basic testing
setup.

#### Acknowledgements
Matthias Bechtold (mbechto): Implementation PoC