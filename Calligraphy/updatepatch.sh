#!/bin/bash

TARGET="Calligraphy"
CURPATH=`pwd`

PROJNAME=""
for name in ${TARGET}
do
	echo $CURPATH | grep $name 
	if [ $? -eq 0 ];
	then 
		PROJNAME=$name
		break
	fi
done

if [ "$PROJNAME" = "" ];
then
	echo "Error: no project name matched"
	exit
else
	echo "Current project name is $PROJNAME"
fi
#empty the http_proxy environment
http_proxy=
export http_proxy


#begin to fetch
git fetch origin

if [ $? -eq 0 ];
then 
	echo "Git fetch succeed!"
else
	echo "Warning: git fetch failed !!!!"
	exit
fi

#begin to rebase
git rebase origin/master

if [ $? -eq 0 ];
then 
	echo "Git rebase succeed!"
else
	git rebase --abort
	echo "ERROR: git rebase failed !!!!"
	echo "The rebase operation has been aborted."
	exit
fi

CURTIME=`LC_ALL=C LANG=C date +%Y-%m-%d-%H-%M`
TEMPDIR=$CURTIME/$PROJNAME
mkdir -p $TEMPDIR

PATCHLIST=`git format-patch origin/master`
if [ $? -eq 0 ];
then
	echo "Git generate patches successfully!"
else
	echo "Error: git format patch failed !"
	exit
fi

for patch in ${PATCHLIST}
do
	echo "patch file name "$patch
	mv $patch $TEMPDIR/$patch
done

cd $CURTIME
wput $PROJNAME ftp://c7soft:12345678@192.168.2.22
if [ $? -eq 0 ];
then
	echo "Patches upload successfully!"
else
	echo "Error: patches upload failed !"
	exit
fi

cd $CURPATH
rm -rf $CURTIME
