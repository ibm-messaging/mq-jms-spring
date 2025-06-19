
#
# The program puts a pair of messages to the DEV.QUEUE.1 queue on QM1. It then
# uses XA controls to move the first message to DEV.QUEUE.1 on QM2 and committing that
# operation, and then trying to do it again, but doing a backout.
#
# You may need to modify the application.properties file to get it to
# connect to your queue manager.

###### Cleanup from previous runs
# Kill any old instances of the application
ps -ef|grep gradle | grep sample4n.Application | awk '{print $2}' | xargs kill -9 >/dev/null 2>&1

# Set the environment
. setmqenv -m QM2 -k
export PATH=$MQ_INSTALLATION_PATH/samp/bin:$PATH

# Try to clear the queue (assuming it's using local queue managers)
echo "CLEAR QLOCAL(DEV.QUEUE.1)" | runmqsc -e QM1 >/dev/null 2>&1
echo "CLEAR QLOCAL(DEV.QUEUE.1)" | runmqsc -e QM2 >/dev/null 2>&1

# These files are created when tracing is enabled - see the application.yml resources file
mkdir -p /tmp/jms
rm -f /tmp/jms/*
rm -f mqjms.log*
touch /tmp/jms/trace.log
rm -rf transaction-logs

curdir=`pwd`
# Now run the program. Build using the gradle wrapper in parent directory
cd ../..

./gradlew -p samples/s4n bootRun 2>&1 | tee -a /tmp/jms/trace.log  # $curdir/t.out

# And optionally look to see what's on each the queues on each queue manager. Should have one message on each
if true 
then
(
echo "=== QM1 ==="
echo "DIS QS(DEV.QUEUE.1)" | runmqsc QM1 | grep -E "CURDEP|UNCOM"
amqsbcg DEV.QUEUE.1 QM1 | grep -E "messages|BackoutCount" | grep -v No

echo "=== QM2 ==="
echo "DIS QS(DEV.QUEUE.1)" | runmqsc QM2 | grep -E "CURDEP|UNCOM"
amqsbcg DEV.QUEUE.1 QM2 | grep -E "messages|BackoutCount" | grep -v No
) | tee -a $curdir/t.out
fi

