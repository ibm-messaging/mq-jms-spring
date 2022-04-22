# Sample Programs
This directory contains programs demonstrating some features of Spring Boot JMS programs.

The programs all have a `RUNME.sh`script that uses `gradle` to compile and run the
programs. They also have a resource file with externally-configured properties that
control how the connection is made to a queue manager. You may need to modify those
properties to match your environment. The `README` in the root of this repository lists
the available properties.

## Contents
* s1 - The simplest example that creates two connections to MQ: one to put a message,
and the other to act as a JMSListener that retrieves the message.
* s2 - Demonstrates use of local transactional control to commit and rollback changes
* s3 - A request/reply program, with both the requester and responder in the same application.
The responder side shows how transactions can be controlled within a JMSListener.
* s3.jms3 - Identical to s3 but changed to use the Jakarta packages and corresponding
       MQ and Spring dependencies

## Other resources
Many other sample programs for MQ can be found in [this repository](https://github.com/ibm-messaging/mq-dev-patterns).
Those examples have demonstrations of additional features and alternative ways to build and
run JMS programs. For example, using `maven` instead of `gradle`.
