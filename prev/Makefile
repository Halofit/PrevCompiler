#
# START GOOGLE CHROME WITH --allow-file-access-from-files
# or just use Firefox ;)
#

bindir = ../out/production/Compiler
xsldir = ./xsl

%.db	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=fin --loggedphases=fin,lincode $<
	mmixal -x $<.mms 
	mmix -q $<.mmo

%.mmix	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=fin --loggedphases=fin,lincode $<

%.regalloc.xml	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=regalloc --loggedphases=codegen,regalloc $<

%.codegen.xml	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=codegen --loggedphases=imcode,codegen,liveness $<

%.lincode.xml	: %.prev
	java -classpath $(bindir) compiler.Main --xsldir=$(xsldir) --phase=lincode --loggedphases=lincode,imcode $<

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
	del *.xml || rm *.xml || echo ""
	del *.mmix || rm *.mmix || echo ""
	del *.graph || rm *.graph || echo ""
	del *.mms || rm *.mms || echo ""
