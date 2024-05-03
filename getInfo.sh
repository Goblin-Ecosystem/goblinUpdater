#!/bin/bash
grep -i -e "\[INFO" -e "\[WARN" $1 | cut -d " " -f 5-
