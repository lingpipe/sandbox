\chapter{Escapes}\label{chapter:escapes}

\section{Escapes All The Way Down}

One of the problems with running text-processing programs is that
almost every level of processing has a syntax in which some characters
are interpreted specially.  These characters must then be escaped to
be treated literally.  For instance, in Java string literals, we need
to write \code{"{\bk}n"} for a newline character, because we can't
have a newline in the code, because string literals may not break
lines.  In XML, we cannot have a less-than character, \charmention{<},
as a text value, so we use the escape \code{\&lt;}.

We often wind up processing with pipelines of processors.  For
instance, we may call a Java program from Ant and pass in properties
from the command line.  Most of the tools we deal with are smart
enough to pass input strings as-is to the next part of the pipeline.
For instance, calling the Java command from Ant doesn't create a
string that gets executed, but rather calls the \code{main(String[])}
method with the arguments as whole strings.  Thus as long as the
string is well-formed in Ant, meaning in XML, it will get passed
as is to the \code{main()} method.

We often have to deal with data generated in comma-separated value
(CSV) format or in XML, or in Penn Treebank format, all of which have
their own way of escaping special characters.  Often such
transformations are context-specific.  For instance, quoting a field
is optional in CSV if it doesn't contain any quote characters or
newlines.  In the context of a quoted field, newlines do not separate
rows, but act as characters in the field's value.  In XML, the pair of
elements \code{<![CDATA[} and \code{]]>} act like quotes in CSV, and all
characters between those boundaries are interpreted literally.

Getting the intended string to Java intact is often just the first
stage of the process.  That Java program may accept strings that
represent regular expressions, which have their own escape syntax.  On
the output side, the program may need to generate well-formed XML,
which requires further escaping outputs so that they may be
interpreted.

The basic principle is the same in handling all of these cases.  We
just need to make sure every program at every stage gets the string
the way it expects it.  

\section{DOS Shell}

If the following characters do not occur within quotes (\code{"}),
they must be escaped by preceding them with a carat (\code{\^{}}).

\begin{verbatim}
& | ( ) < > ^
\end{verbatim}


\section{Unix Shell}