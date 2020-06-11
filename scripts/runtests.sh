#!/bin/sh -l

set -e
echo "======== Running tests ================="

echo "======== Running verify for plugin ========="
sh -c "mvn --batch-mode verify"

echo "======== Finished running tests ========"