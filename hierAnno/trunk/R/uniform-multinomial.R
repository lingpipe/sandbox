library("R2WinBUGS")


anno <- function(alpha,beta,y,jj,ii) {
    K <- max(y);
    J <- max(jj);
    I <- max(ii);
    alpha <- rep(0,K);
    

    data <- list("alpha","beta","y","jj","ii","J","K","N")
    parameters <- c("pi","z","theta")
    inits <- function() {
        list(pi=runif(1,0.4,6),
             alpha
    anno <- bugs(data, inits, parameters,
                 "c:/carp/sandbox/hierAnno/R/bugs/beta-binomial-item.bug",
                 n.chains=3, n.iter=100,
                 debug=TRUE, clearWD=TRUE,
                 bugs.directory="c:\\WinBUGS\\WinBUGS14")

}