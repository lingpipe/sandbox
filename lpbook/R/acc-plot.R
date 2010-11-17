p <- 0.5
g <- function(q) {
    p*q + (1-p)*(1-q)
}
curve(g, xlim=c(0,1))