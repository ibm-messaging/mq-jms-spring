# This script compiles and runs the sample program in this directory.
#
# The program puts a single message to the DEV.QUEUE.1 queue and tries to 
# retrieve it via a JMS Listener.
# 
# It uses a TLS-enabled channel with the configuration partly
# in the application.properties file, and partly in the build.gradle
# file where system properties are set to point at keystores.
#
# In a standalone execution environment, those properties will be 
# set with -D options on the java command line. Or you might prefer
# to use System.setProperty() in your application code as one way to 
# manage the password if it is needed for the jks access. Or you might
# use alternative Spring solutions for configuration properties.
# 
# You may need to modify the application.properties file to get it to 
# connect to your queue manager.
#
# For the simple requirement of one-way authentication here, we have extracted
# the queue manager's public certificate into qm1.arm. And then imported it. The   
# buildJks script can be used as an example of doing that.

###### Cleanup from previous runs
# Kill any old instances of the application
ps -ef|grep gradle | grep sample2.Application | awk '{print $2}' | xargs kill -9 >/dev/null 2>&1
# and try to clear the queue (assuming it's a local queue manager)
echo "CLEAR QLOCAL(DEV.QUEUE.1)" | runmqsc -e QM1 >/dev/null 2>&1
######
#
curdir=`pwd`

# Now run the program. Build using the gradle wrapper in parent directory
cd ../..

./gradlew -p samples/s2.tls.jms3 bootRun
