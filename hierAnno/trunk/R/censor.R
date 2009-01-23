# Bob Carpenter
# Alias-i
# 8 September 2008

# Code used in paper submitted to NYAS ML Symposium
# http://lingpipe.files.wordpress.com/2008/09/hierarchical-data-annotation-nyas-08.pdf

p.annotated <- 0.5
for (i in 1:I)
  for (j in 1:J)
    if (rbinom(1,1,1-p.annotated))
      x[i,j] <- NA
