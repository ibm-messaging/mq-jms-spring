# This script compiles and runs the sample program in this directory.
#
# The program puts a pair of messages to the DEV.QUEUE.1 queue on QM1. It then
# uses XA controls to move the first message to DEV.QUEUE.1 on QM2 and committing that
# operation, and then trying to do it again, but doing a backout.
# 
# You may need to modify the application.properties file to get it to 
# connect to your queue manager.

###### Cleanup from previous runs
# Kill any old instances of the application
ps -ef|grep gradle | grep sample5.Application | awk '{print $2}' | xargs kill -9 >/dev/null 2>&1

# Set the environment
. setmqenv -m QM1 -k
export PATH=$MQ_INSTALLATION_PATH/samp/bin:$PATH

# Try to clear the queue (assuming it's using local queue managers)
echo "CLEAR QLOCAL(DEV.QUEUE.1)" | runmqsc -e QM1 >/dev/null 2>&1
echo "CLEAR QLOCAL(DEV.QUEUE.1)" | runmqsc -e QM2 >/dev/null 2>&1

d=`date`
echo "COMMIT   Message for Spring $d" | amqsput DEV.QUEUE.1 QM1 >/dev/null 2>&1
sleep 1 # So we get slightly different dates
d=`date`
echo "ROLLBACK Message for Spring $d" | amqsput DEV.QUEUE.1 QM1 >/dev/null 2>&1

######

# Now run the program. Build using the gradle wrapper in parent directory
cd ../..

./gradlew -p samples/s4a.jms3 bootRun

# And optionally look to see what's on each the queues on each queue manager. Should have one message on each
if false
then
echo "Reading from QM1"
amqsbcg DEV.QUEUE.1 QM1

echo "----------------"
echo "Reading from QM2"
amqsbcg DEV.QUEUE.1 QM2
fi
