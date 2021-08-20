# Changelog
Newest updates are at the top of this file

## 2.5.4 (2021-08-20)
- Update dependencies to Spring Boot 2.5.4

## 2.5.3 (2021-06-26)
- Update dependencies to MQ 9.2.3
- Update dependencies to Spring Boot 2.5.3
- Add some JNDI properties for CF configuration (see README) (#72)

## 2.5.0 (2021-05-21)
- Update dependencies to Spring Boot 2.5.0

## 2.4.5 (2021-04-16)
- Update dependencies to Spring Boot 2.4.5

## 2.4.4 (2021-03-26)
- Update dependencies to Spring Boot 2.4.4
- Update dependencies to MQ 9.2.2
- Additional trace points

## 2.4.3 (2021-02-19)
- Update dependencies to Spring Boot 2.4.3
- Add trace points for logger

## 2.4.2 (2021-01-16)
- Update dependencies to Spring Boot 2.4.2

## N/A   (2020-12-16)
- Add a request/reply sample

## 2.4.1 (2020-12-13)
- Update dependencies to Spring Boot 2.4.1

## 2.4.0 (2020-12-04)
- Update dependencies to Spring Boot 2.4.0
- Update dependencies to MQ 9.2.1
- Move original sample to a subdirectory and add another sample showing transactions

## 2.3.5 (2020-10-30)
- Update KnowledgeCenter URLs to refer to 'latest' version
- Update dependencies to Spring Boot 2.3.5

## 2.3.4 (2020-09-18)
- Update dependencies to Spring Boot 2.3.4

## 2.3.3 (2020-08-13)
- Update dependencies to Spring Boot 2.3.3
- Split CF configuration into separate method

## 2.3.2 (2020-07-23)
- Update dependencies to Spring Boot 2.3.2
- Update dependencies to MQ V9.2

## 2.3.1 (2020-06-12)
- Update dependencies to Spring Boot 2.3.1
- More flexibility in using the additionalProperties CF configuration

## 2.3.0 (2020-05-24)
- Update dependencies to Spring Boot 2.3.0

## 2.2.7 (2020-05-08)
- Update dependencies to Spring Boot 2.2.7

## 2.2.6 (2020-04-02)
- Update dependencies to MQ V9.1.5
- Update dependencies to Spring Boot 2.2.6
- Update dependencies to Spring Framework 5.2.4
- Remove dependency on jta property (#49)
- Additional configuration properties (#50)

## 2.2.5 (2020-03-02)
- Add proxyBeanMethods=false (#45)
- Update dependencies to Spring Boot 2.2.5

## 2.2.4
- Update dependencies to Spring Boot 2.2.4

## 2.2.3
- Update dependencies to Spring Boot 2.2.3
- Update dependencies to Spring Framework 5.2.3

## 2.2.2 (still) (2019-12-20)
- Add a sample program (no new version needed)

## 2.2.2 (2019-12-05)
- Update dependencies to MQ V9.1.4
- Update dependencies to Spring Boot 2.2.2
- Update dependencies to Spring Framework 5.2.2
- Update dependencies to PooledJMS 1.1.0

## 2.2.1 (2019-11-08)
- Update dependencies to Spring Boot 2.2.1
- Update dependencies to Spring Framework 5.2.1

## 2.2.0 (2019-10-17)
- Update dependencies to spring boot 2.2.0
- Override for polling listener default timeout

## 2.1.4 (2019-10-10)
- Replace the short-lived 2.1.3 that was corrupted by OSSRH problems
- Update dependencies to Spring Boot 2.1.9
- Update dependencies to MQ 9.1.3
- Update dependencies to PooledJMS 1.0.6

## 2.1.2
- Add bean instantiation conditions keeping the correct order - XAConnectionFactoryWrapper and after this IBM connection factory
- Add XA wrapper functionality for external JTA providers and assign the nonXA connection factory
- Update dependencies to spring boot 2.1.4

## 2.1.1
- Update dependencies to MQ 9.1.2
- Update dependencies to spring boot 2.1.3
- Add applicationName configuration property (#20)

## 2.1.0
- Simplify connection pool creation using spring boot 2.1.0 resources
- Update dependencies to MQ 9.1.1

## 2.0.9
- Update dependencies to spring boot 2.1.0

## 2.0.8
- Add pooled connection factory option

## 2.0.7 (2018-10-19)
- Replace a broken 2.0.6

## 2.0.5 (2018-10-03)
- Update dependencies to spring boot 2.0.5
- Add CCDTUrl and SSLPeer to configurable properties
- Make MQConnectionFactoryFactory a public class (see issue #7)

## 2.0.1 (2018-09-14)
- Added ability to set a client id on the connection

## 2.0.0 (2018-05-27)
- Upgrade the spring boot dependency to spring boot 2.0.2
- Upgrade plugin version to 2.0.0 (according to spring boot version 2.x)

## 0.0.4 (2018-04-02)
- Allow USER_AUTHENTICATION_MQCSP to be configured from properties

## 0.0.3 (2018-03-08)
- Change configuration prefix to "ibm.mq" to reduce ambiguities if you have other messaging attributes in the same application
- Modify build.gradle to make pushing to Maven a more explicit operation

## 0.0.2 (2018-01-22)
- Modify default config to match MQ Docker container image defaults
- add TLS related properties

## 0.0.1 (2018-01-18)
- [NEW] Initial skeleton release.
