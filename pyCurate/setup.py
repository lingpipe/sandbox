# creates package curate-1.0.zip in directory dist/
# -------------------------------------------------
# % python setup.py sdist

# install package into local Python distribution
# ----------------------------------------------
# % unzip curate-1.0.zip
# % cd curate-1.0
# % python setup.py install

from distutils.core import setup 

setup(name="curate",
      version="1.0",
      py_modules = ['libcurate'],
      packages = ['curatepkg'],
      scripts = ['runcurate.py']);
