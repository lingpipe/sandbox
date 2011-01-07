# !/usr/bin/python

# USAGE:  (python) hello.py [FirstName [SecondName]]
# You only need (python) if your python install isn't
# at /user/bin/python (e.g. on Windows not in Cygwin)

import sys
try:
    import pyhi.greet
except ImportError, e:
    print e
    print ""
    print "Need to install pyhi first.  See README.txt"

if len(sys.argv) == 1:
    print pyhi.greet.hello()
elif len(sys.argv) == 2:
    print pyhi.greet.hello(sys.argv[1])
elif len(sys.argv) == 3:
    print pyhi.greet.hello(sys.argv[1],sys.argv[2])
else:
    print "USAGE python hello.py [FirstName [SecondName]]"
    System.exit(1)

print pyhi.greet.goodbye()
