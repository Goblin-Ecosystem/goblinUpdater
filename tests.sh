#!/bin/sh

FILES=("test_CVE-0_COST-0" "test_CVE-0_COST-1" "test_CVE-1_COST-0" "test_CVE-1_COST-1")
NOW=$(date +"%Y-%m-%d_%H:%M:%S")
OUTPUT="test_${NOW}.txt"
EXPECTED=$1
ACTUAL=$2

touch ${OUTPUT}
echo "# Tests at ${NOW}" >> ${OUTPUT}	
for f in ${FILES[@]}; do
	expected=${f}${EXPECTED}.log
	actual=${f}${ACTUAL}.log
	echo "## Comparison for ${f} (actual: ${actual} vs expected: ${expected})" >> ${OUTPUT}
	echo "" >> ${OUTPUT}
	./compare.sh ${expected} ${actual} >> ${OUTPUT}
	echo "" >> ${OUTPUT}
done

