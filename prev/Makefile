#
# START GOOGLE CHROME WITH --allow-file-access-from-files
#

#D:\Ostalo\Idea\Projects\Compiler\out\production\Compiler
#D:\Ostalo\Idea\Projects\Compiler\prev

binPath = ../out/production/Compiler


%.synan.xml	: %.prev
	java -classpath $(binPath) compiler.Main --xsldir=. --phase=synan --loggedphases=synan $<

%.lexan.xml	: %.prev
	java -classpath $(binPath) compiler.Main --xsldir=. --phase=lexan --loggedphases=lexan $<

clean		:
#	mv test.prev .test.prev
#	'rm' -fr *.prev
#	mv .test.prev test.prev
	del *.xml
