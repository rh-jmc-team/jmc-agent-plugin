#!/bin/sh -l

set -e
echo "======== Building JMC ======================="
cd $HOME
sh -c "git clone --depth 1 https://github.com/openjdk/jmc.git"

echo "======== Building p2 repo ==================="
cd jmc/releng/third-party
sh -c "mvn --quiet p2:site"

echo "======== Starting p2 repo ==================="
sh -c "nohup mvn --quiet jetty:run &"

cd ../../core
echo "======== Installing core ===================="
sh -c "mvn --quiet install"

echo "======== Installing application ============="
cd ..
sh -c "mvn --quiet install"

echo "======== Installing jmc ====================="
cd agent
sh -c "mvn --quiet install"

echo "======== Finished building JMC =============="