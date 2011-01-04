# create /dist/curate-1.0.zip and /MANIFEST
# -------------------------------------------------
# % python setup.py sdist

# install package into local Python distribution
# ----------------------------------------------
# % unzip pyanno-1.0.zip
# % cd pyanno-1.0
# % python setup.py install

from distutils.core import setup 

setup(name="pyanno",
      version="1.0",
      packages = ['pyanno'],
      py_modules = ['pyanno.kappa',
                    'pyanno.multinom',
                    'pyanno.util'],
      scripts = ['unit_test.py',
                 'examples/mle_sim.py', 
                 'examples/mle_rzhetsky_2009.py'],
      author = ['Bob Carpenter', 'Andrey Rzhetsky', 'James Evans' ],
      author_email = [ 'carp@lingpipe.com' ],
      maintainer = ['Bob Carpenter'],
      maintainer_email = ['carp@lingpipe.com'],
      description = ['Packages for curating data annotation efforts.'],
      url = ['http://alias-i.com/lingpipe/web/sandbox.html'],
      download_url = ['https://aliasi.devguard.com/svn/sandbox/pyanno']
      );
