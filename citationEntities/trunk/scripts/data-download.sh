# DOWNLOAD ALL PROJECT DATA
# =========================

# TREC 2006
# ---------
# Proceedings of the Fifteenth Text Retrieval Conference (TREC 2006)
# contents: http://trec.nist.gov/pubs/trec15/t15_proceedings.html
# papers:   http://trec.nist.gov/pubs/trec15/papers/*.pdf
# bibtex:   n/a

wget -nc -w 5 -r -l 1 --accept "*.pdf" -P../data/citations/raw http://trec.nist.gov/pubs/trec15/papers/ &


# ACL 2006
# --------
# Proceedings of the 21st International Conference on Computational Linguistics and 44th Annual Meeting of the Association for Computational Linguistics
# (includes main conference, posters, student session and demos)
# contents: http://acl.ldc.upenn.edu/P/P06/index.html
# papers:   http://acl.ldc.upenn.edu/P/P06/*.pdf
# bibtex:   http://acl.ldc.upenn.edu/P/P06/*.bib

wget -nc -w 5 -r -l 1 --accept "*.pdf" -P../data/citations/raw http://acl.ldc.upenn.edu/P/P06/ &


# NIPS 2006
# ---------
# Advances in Neural Information Processing Systems 19
# contents:         http://books.nips.cc/nips19.html
# papers:           http://books.nips.cc/papers/files/nips19/*.pdf
# papers gzipped:   http://books.nips.cc/papers/files/nips19/nips2006_pdf.zip
# bibtex:           http://books.nips.cc/papers/files/nips19/*.bib
# bibtex all:       http://books.nips.cc/nips19/nips19.bib

wget -nc -w 5 -r -l 1 --accept "*.pdf" -P../data/citations/raw http://books.nips.cc/papers/files/nips19/


# PROPRIETARY PROCEEDINGS
# -----------------------
# ICSLP 2006      (members only)
# ICASSP 2006     (members only)
# ASRU 2005       (members only)
# Eurospeech 2005 (members only)
# SIGIR           (members only)


# wget options
#
# -nc      Don't clobber if already there; 
#            allows to rerun script after partial completion
#
# -w 60    Wait 60 seconds between downloads
# 
# -r       Download recursively through HTML and directories
#
# -l 1     Don't leave the specified directory on the server (one level of recursion)
#
# --accept <pattern>  Only accept files matching pattern
#
# -P<dir>  Write output to specified directory path
#
# <url>    Where to start the download