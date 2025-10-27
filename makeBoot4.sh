#!/bin/bash

# Generate a Boot4-compatible source from the primary tree (JMS3/Boot3):
#

curdir=`pwd`
in="$1"
out="$2"

if [ -z "$in" -o -z "$out" ]
then
  echo "Usage: makeBoot4.sh inDir outDir"
  exit 1
fi
# echo "Copying files for Boot4 build from $in to $out"

if [ ! -d $in ]
then
  echo "Cannot find input directory $in"
  exit 1
fi

mkdir $out >/dev/null 2>&1
cd $in
# Create the structure
find . -type f |\
   grep -v bin/ |\
   cpio -upad $out
# And recopy the Java files doing any modifications as we go
find . -type f -name "*.java" |\
 grep -v bin/ |\
 while read f
do
   # Boot4 moved a bunch of imported classes
   cat $f |\
   sed "s/org.springframework.boot.autoconfigure.jms/org.springframework.boot.jms.autoconfigure/g" |\
   sed "s/org.springframework.boot.autoconfigure.transaction.jta/org.springframework.boot.transaction.jta.autoconfigure/g" > $out/$f 
done

