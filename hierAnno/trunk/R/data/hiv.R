confusion.matrix <-
    c(rep(c(0,0,0,0), times=170),
      rep(c(0,0,0,1), times=15),
      rep(c(0,1,0,0), times=6),
      rep(c(1,0,0,0), times=4),
      rep(c(1,0,0,1), times=17),
      rep(c(1,0,1,1), times=83),
      rep(c(1,1,0,0), times=1),
      rep(c(1,1,0,1), times=4),
      rep(c(1,1,1,1), times=128))
J <- 4
I <- length(confusion.matrix) / J
x <- matrix(confusion.matrix,nrow=I,ncol=J,byrow=TRUE)
