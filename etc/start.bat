cd ..\target\jetty
java -Djetty.port=8484 -Djtrac.home=home -Dfile.encoding=UTF-8 -DSTOP.PORT=8079 -DSTOP.KEY=jtrac -jar start.jar