#!/bin/bash

pushd src
echo "------------------------------------------------------------------------"
echo "1"
cat ../input/input001.txt | java Main
echo ""
cat ../output/output001.txt
echo ""
echo "------------------------------------------------------------------------"
echo "2"
cat ../input/input002.txt | java Main
echo ""
cat ../output/output002.txt
echo ""
echo "------------------------------------------------------------------------"
echo "3"
cat ../input/input003.txt | java Main
echo ""
cat ../output/output003.txt
echo ""

popd
