# This script compiles and runs the sample program in this directory.
#
# The program puts a single message to the DEV.QUEUE.1 queue and uses transactional operations 
# first commit that operation, and then rollback a "move" stage.
# So you should end up with one message on DEV.QUEUE.1 whose BackoutCount is 1
#
# You may need to modify the application.properties file to get it to 
# connect to your queue manager.

###### Cleanup from previous runs
# Kill any old instances of the application
ps -ef|grep gradle | grep sample2.Application | awk '{print $2}' | xargs kill -9 >/dev/null 2>&1
# and try to clear the queue (assuming it's a local queue manager)
echo "CLEAR QLOCAL(DEV.QUEUE.1)" | runmqsc -e QM1 >/dev/null 2>&1
######

# Now run the program. Build using the gradle wrapper in parent directory
cd ../..

./gradlew -p samples/s2 bootRun
