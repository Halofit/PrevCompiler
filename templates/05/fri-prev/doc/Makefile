lang-spec.pdf: lang-spec.tex ../prg/src/compiler/phase/synan/prev-lr.grammar
	./grammar.sh
	pdflatex lang-spec.tex

clean:
	'rm' -fr grammar.tex
	'rm' -fr *~ *.aux *.log *.pdf *.gz
