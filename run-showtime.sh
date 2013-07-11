docear_home=tmp/docearhome
rm -rf ${docear_home}
mkdir -p ${docear_home}/projects
echo "<config><user><username>Michael</username><accessToken>Michael-token</accessToken></user></config>" > ${docear_home}/projects/config.xml
sbt -Ddaemon.client.baseurl=https://192.168.0.1:4443/api -Ddaemon.docear.home=${docear_home} run 