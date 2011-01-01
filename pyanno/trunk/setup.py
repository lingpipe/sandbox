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
      py_modules = ['pyanno.multinom','pyanno.util','pyanno.kappa'],
      packages = ['pyanno'],
      scripts = ['eg_mle_sim.py','unit_test.py'],
      author = ['Bob Carpenter', 'Andrey Rzhetsky', 'James Evans' ],
      author_email = [ 'carp@lingpipe.com' ],
      maintainer = ['Bob Carpenter'],
      maintainer_email = ['carp@lingpipe.com'],
      description = ['Packages for curating data annotation efforts.'],
      url = ['http://alias-i.com/lingpipe/web/sandbox.html'],
      download_url = ['https://aliasi.devguard.com/svn/sandbox/pyanno']
      );
