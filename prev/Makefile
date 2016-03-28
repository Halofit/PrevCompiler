#
# START GOOGLE CHROME WITH --allow-file-access-from-files
#

bindir = ../out/production/Compiler
xsldir = ./xsl

%.abstr.xml	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=abstr --loggedphases="abstr" $<

%.synan.xml	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=synan --loggedphases=synan $<

%.lexan.xml	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=lexan --loggedphases=lexan $<

clean:
	del *.xml
