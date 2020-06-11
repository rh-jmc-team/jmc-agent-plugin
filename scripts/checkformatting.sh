#!/bin/sh -l

set -e
echo "======== Checking formatting ================="

echo "======== Running spotless for plugin ========="
sh -c "mvn spotless:check"

echo "======== Finished checking formatting ========"