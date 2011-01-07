README: pyhi 1.0
============================================================

The pyhi package is a pedagogical example of how to organize a Python
package for distribution and use.

pyhi 1.0 consists of a single package, pyhi, made up of a single
module, pyhi.greet, containing two functions, hello() and goodbye().

The package is fully configured with scripts to unit test, build and
install the package, and run a greetings program from a script.  The
package contains full documentation, including installation 
and use instructions and code doc strings.


Installation
------------------------------------------------------------
Download pyhi-1.0.tar.gz into directory $DL, then:

    % cd $DL
    % tar -xzf pyhi-1.0.tar.gz
    % cd pyhi-1.0
    % python setup.py install

Unfortunately, uninstalling requires finding the directories under
your python distribution and removing them by hand.  Look in:

    $PYTHON_HOME/lib/site-packages/

where $PYTHON_HOME is the directory in which Python is installed.


Running pyhi Command
------------------------------------------------------------
After installation, you can run the script from anywhere:

    % python $DL/pyhi-1.0/bin/hello.py Big Kahuna

If you haven't installed pyhi, you will only be able to
run from the root of the package, $DL/pyhi-1.0.  


Programming with pyhi 1.0
------------------------------------------------------------
After installation, you can use pyhi like any other Python
module.  For instance, you can run the following interactively
or from a script:

    import pyhi.greet
    pyhi.greet.hello(first="Big",last="Kahuna")


Building
------------------------------------------------------------
The distribution itself may be built as a zip archive (.zip)
and gzipped tarball (.tar.gz) using:

    % python setup.py sdist --formats=gztar,zip

This will place the distribution files in directory dist.


Running Unit Tests
------------------------------------------------------------

The unit tests are based on the python module unnittest.  They are
located in pyhi-1.0/bin/runtests.py and may be run with:

   % pyhthon runtests.py

or

   % runtests.py

if your python is installed in /usr/bin/python.

It'll print out how many tests passed or indicate failure.


Cleaning
------------------------------------------------------------

To avoid clutter, there's no clean (or build) scripts.  The build
generates a file (MANIFEST) and two directories (build, dist).  After
installation, these may all be removed.


About
------------------------------------------------------------
by Bob Carpenter, Alias-i, Inc.  carp@lingpipe.com
