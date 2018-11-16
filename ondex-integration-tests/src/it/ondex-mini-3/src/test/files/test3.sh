#!/bin/bash
cd ..
mkdir ondex-testing
mv -v test.resources/ ondex-testing/
cd ondex-testing/
wget "http://ondex.rothamsted.ac.uk/nexus/service/local/artifact/maven/redirect?r=snapshots&g=net.sourceforge.ondex.apps&a=ondex-mini&v=0.5.0-SNAPSHOT&e=zip&c=packaged-distro"
unzip ondex-mini-0.5.0-*.zip
rm ondex-mini-0.5.0-*.zip
cd ondex-mini-0.5.0-SNAPSHOT/plugins/
wget "http://ondex.rothamsted.ac.uk/nexus/service/local/artifact/maven/redirect?r=snapshots&g=net.sourceforge.ondex.modules&a=iah&v=0.5.0-SNAPSHOT&e=jar&c=jar-with-dependencies"
wget "http://ondex.rothamsted.ac.uk/nexus/service/local/artifact/maven/redirect?r=snapshots&g=net.sourceforge.ondex.modules&a=interaction&v=0.5.0-SNAPSHOT&e=jar&c=jar-with-dependencies"
#DEBUG START#
#cd ../lib/
#cp -v ~/Downloads/ondex-mini-0.5.0-SNAPSHOT/lib/workflow-base-0.5.0-SNAPSHOT.jar .
##DEBUG END##
cd ..
echo "Running ondex on test data"
./runme.sh ../test.resources/workflow_test_3.xml 2>&1 workflow.output && echo "Success!" && ls -l yeast_test.xml.gz

