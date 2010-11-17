p <- 0.7
g <- function(q) {
    2 * p * q / (p + q)
}
curve(g, xlim=c(0,1))