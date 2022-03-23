# This script compiles and runs the sample program in this directory.
#
# The program implements both a Requester and Responder pattern: the 
# main program puts a message and waits for a reply. While there is also
# a JMS listener that gets a message, sending a reply back. The listener
# is transactional - the first time it executes, the GET/PUT sequence is
# rolled-back, resulting in the listener being redriven. The second time, 
# it commits the transaction, so the main requester thread can see the reply. 
# 
# You may need to modify the application.properties file to get it to 
# connect to your queue manager.

###### Cleanup from previous runs
# Kill any old instances of the application
ps -ef|grep gradle | grep sample3.Application | awk '{print $2}' | xargs kill -9 >/dev/null 2>&1
# and try to clear the queue (assuming it's a local queue manager)
echo "CLEAR QLOCAL(DEV.QUEUE.1)" | runmqsc -e QM1 >/dev/null 2>&1
######

# Now run the program. Build using the gradle wrapper in parent directory
cd ../..

./gradlew -p samples/s3.jms3 bootRun
