#
# START GOOGLE CHROME WITH --allow-file-access-from-files
# or just use Firefox ;)

bindir = ../out/production/Compiler
xsldir = ./xsl


%.imcode.xml	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=imcode --loggedphases=imcode $<

%.frames.xml	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=frames --loggedphases=frames $<

%.seman.xml	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=seman --loggedphases=seman $<

%.abstr.xml	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=abstr --loggedphases=abstr $<

%.synan.xml	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=synan --loggedphases=synan $<

%.lexan.xml	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=lexan --loggedphases=lexan $<

	
clean:
	del *.xml
