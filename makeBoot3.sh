#!/bin/bash

# Generate a Boot3-compatible source from the primary tree (JMS3/Boot3):
# This is a straight copy as we don't need to modify the code at all.
# Boot3 is currently the primary development level.

curdir=`pwd`
in="$1"
out="$2"

if [ -z "$in" -o -z "$out" ]
then
  echo "Usage: makeBoot3.sh inDir outDir"
  exit 1
fi
# echo "Copying files for Boot3 build from $in to $out"

if [ ! -d $in ]
then
  echo "Cannot find input directory $in"
  exit 1
fi

mkdir $out >/dev/null 2>&1
cd $in
# Create the structure
find . -type f | cpio -upad $out
# And recopy the Java files doing any modifications as we go
find . -type f -name "*.java" | while read f
do
   cp $f $out/$f 
done

