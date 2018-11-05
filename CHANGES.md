# 0.0.1 (2018-01-18)
- [NEW] Initial skeleton release.

# 0.0.2 (2018-01-22)
- Modify default config to match MQ Docker container image defaults
- add TLS related properties

# 0.0.3 (2018-03-08)
- Change configuration prefix to "ibm.mq" to reduce ambiguities if you have other messaging attributes in the same application
- Modify build.gradle to make pushing to Maven a more explicit operation

# 0.0.4 (2018-04-02)
- Allow USER_AUTHENTICATION_MQCSP to be configured from properties

# 2.0.0 (2018-05-27)
- Upgrade the spring boot dependency to spring boot 2.0.2
- Upgrade plugin version to 2.0.0 (according to spring boot version 2.x)

# 2.0.1 (2018-09-14)
- Added ability to set a client id on the connection

# 2.0.5 (2018-10-03)
- Update dependencies to spring boot 2.0.5
- Add CCDTUrl and SSLPeer to configurable properties
- Make MQConnectionFactoryFactory a public class (see issue #7)

# 2.0.7 (2018-10-19)
- Replace a broken 2.0.6

# 2.0.8
- Add pooled connection factory option
