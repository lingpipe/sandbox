confusion.matrix <-
    c(rep(c(0,0,0,0,0),times=1880),
      rep(c(0,0,0,0,1),times=789),
      rep(c(0,0,0,1,0),times=43),
      rep(c(0,0,0,1,1),times=75),
      rep(c(0,0,1,0,0),times=23),
      rep(c(0,0,1,0,1),times=63),
      rep(c(0,0,1,1,0),times=8),
      rep(c(0,0,1,1,1),times=22),
      rep(c(0,1,0,0,0),times=188),
      rep(c(0,1,0,0,1),times=191),
      rep(c(0,1,0,1,0),times=17),
      rep(c(0,1,0,1,1),times=67),
      rep(c(0,1,1,0,0),times=15),
      rep(c(0,1,1,0,1),times=85),
      rep(c(0,1,1,1,0),times=8),
      rep(c(0,1,1,1,1),times=56),
      rep(c(1,0,0,0,0),times=22),
      rep(c(1,0,0,0,1),times=26),
      rep(c(1,0,0,1,0),times=6),
      rep(c(1,0,0,1,1),times=14),
      rep(c(1,0,1,0,0),times=1),
      rep(c(1,0,1,0,1),times=20),
      rep(c(1,0,1,1,0),times=2),
      rep(c(1,0,1,1,1),times=17),
      rep(c(1,1,0,0,0),times=2),
      rep(c(1,1,0,0,1),times=20),
      rep(c(1,1,0,1,0),times=6),
      rep(c(1,1,0,1,1),times=27),
      rep(c(1,1,1,0,0),times=3),
      rep(c(1,1,1,0,1),times=72),
      rep(c(1,1,1,1,0),times=1),
      rep(c(1,1,1,1,1),times=100))
J <- 5
I <- length(confusion.matrix) / J
x.ordered <- matrix(confusion.matrix,nrow=I,ncol=J,byrow=TRUE)
x <- x.ordered[sample(I),]

