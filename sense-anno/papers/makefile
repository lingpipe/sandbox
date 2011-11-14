wordsense-annotation.pdf: wordsense-annotation.tex wordsense-multilabels.bib
	pdflatex wordsense-annotation.tex
	bibtex wordsense-annotation
	pdflatex wordsense-annotation.tex
	pdflatex wordsense-annotation.tex

clean:
	rm *.aux *.log *.out *.pdf *.bbl *.blg