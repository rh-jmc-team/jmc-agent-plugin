#!/bin/sh -l

set -e
echo "======== Checking formatting ================="

echo "======== Running spotless for plugin ========="
sh -c "mvn --batch-mode spotless:check"

echo "======== Finished checking formatting ========"