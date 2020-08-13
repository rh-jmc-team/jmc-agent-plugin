#!/bin/sh -l

set -e

echo "======== Building JMC with Agent Plugin ================="
sh -c "git clone --depth 1 https://github.com/openjdk/jmc.git"

echo "======== Copying Agent Plugin files ====================="
cp -r ../org.openjdk.jmc* jmc/application

echo "======== Applying patch ================================="
cd jmc
patch -p0 < ../scripts/diff.patch
cd ..

echo "======== Building p2 repo ==============================="
cd jmc/releng/third-party
sh -c "mvn --batch-mode p2:site"

echo "======== Starting p2 repo ==============================="
sh -c "nohup mvn --batch-mode jetty:run &"

cd ../../core
echo "======== Installing core ================================"
sh -c "mvn --batch-mode install -DskipTests=true -Dspotbugs.skip=true"

echo "======== Installing application ========================="
cd ..
sh -c "mvn --batch-mode install -DskipTests=true -Dspotbugs.skip=true"

echo "======== Finished building JMC with Agent Plugin ========"