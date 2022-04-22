#!/bin/sh

group=$1
artifact=$2
version=$3
rc=0
code=0

# Make sure that the named artifact exists in the public central repository so
# we don't rely on any oddly-named versions in private trees
if [ ! -z "$prereqCheck" ]
then
  groupTree=`echo $group | sed "s/\./\//g"`

  url="https://repo.maven.apache.org/maven2/$groupTree/$artifact/$version/$artifact-$version.pom"
  code=`curl -s -w "%{http_code}\n" $url -o /dev/null`
  rc=$?
  if [ $rc -eq 0 -a $code -eq 200 ]
  then
    echo "Successful check of $group:$artifact:$version"
  else
    echo "ERROR: Failed check of $group:$artifact:$version"
    rc=1
  fi
else
  echo "Not checking existence of $group:$artifact:$version"
fi
exit $rc
