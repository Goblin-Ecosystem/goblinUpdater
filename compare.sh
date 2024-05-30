#!/bin/bash

FILE1=/tmp/info1.txt
FILE2=/tmp/info2.txt

./getInfo.sh $1 > $FILE1

./getInfo.sh $2 > $FILE2

diff $FILE1 $FILE2

rm -f $FILE1

rm -f $FILE2



