from distutils.core import setup 

setup(name="pyhi",
      version="1.0",
      packages = ['pyhi'],
      py_modules = ['pyhi.greet'],
      scripts = ['bin/hello.py',
                 'bin/runtests.py'],
      author = ['Bob Carpenter'],
      author_email = [ 'carp@lingpipe.com' ],
      maintainer = ['Bob Carpenter'],
      maintainer_email = ['carp@lingpipe.com'],
      description = ['Package demonstrating how to organize python packages.'],
      url = ['http://alias-i.com/python/pyhi/index.html'],
      download_url = ['http://alias-i.com/python/pyhi/index.html'])
