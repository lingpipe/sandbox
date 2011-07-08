These are my notes for an install done on Mac OS X 10.6 (snow leopard),  
running MacTeX-2010 distribution as downloaded from 
 http://www.tug.org/mactex/
following the install instructions:
http://www.tug.org/store/lucida/README.TUG

To install Lucida fonts for all users

1. unpack the lucimatx.zip.tpm file in the local fonts dir /usr/local/texlive/textmf-local
This directory is only writeable by root, therefore all commands must be run via sudo.

$ cd /usr/local/texlive/texmf-local/
$ ls -ld .
drwxr-xr-x  11 root  wheel  374 Jul  8 11:39 .

$ sudo unzip  <path_to_lpbook>/fonts/lucimatx.zip.tpm 

2. Run the command mktexlsr to remake the filename database

$ sudo mktexlsr

3. Enable the lucida map files.
   If using the mtpro2 (MathTimePro II fonts) , disable belleek Times fonts

$ sudo updmap-sys --enable Map=lucida.map
$ sudo updmap-sys --enable Map=mtpro2.map
$ sudo updmap-sys --disable Map=belleek.map

4. Re-run the command mktexlsr, also texhash

$ sudo mktexlsr
$ sudo texhash

Troubleshooting:  if tex complains about missing hlcrima.tfm,
then you need to set the environment variable TEXMFLOCAL
to point to the texmf-local tree:

$ kpsewhich hlcrima.tfm
$ export TEXMFLOCAL='{/usr/local/texlive/texmf-local/}'
$ kpsewhich hlcrima.tfm

