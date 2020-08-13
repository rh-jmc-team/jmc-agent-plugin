#!/bin/sh -l

set -e
echo "======== Building JMC ======================="
sh -c "git clone --depth 1 https://github.com/openjdk/jmc.git"

echo "======== Building p2 repo ==================="
cd jmc/releng/third-party
sh -c "mvn --batch-mode p2:site"

echo "======== Starting p2 repo ==================="
sh -c "nohup mvn --batch-mode jetty:run &"

cd ../../core
echo "======== Installing core ===================="
sh -c "mvn --batch-mode install -DskipTests=true -Dspotbugs.skip=true"

echo "======== Installing application ============="
cd ..
sh -c "mvn --batch-mode install -DskipTests=true -Dspotbugs.skip=true"

echo "======== Finished building JMC =============="