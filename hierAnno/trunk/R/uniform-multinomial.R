library("R2WinBUGS")

mybugs <- function(data,inits,parameters,iter=1000,chains=3) {
    bugs(data, inits, parameters,
         "c:/carp/sandbox/hierAnno/R/bugs/beta-binomial-item.bug",
          n.chains=chains, 
          n.iter=iter,
          debug=TRUE, 
          clearWD=TRUE,
          bugs.directory="c:\\WinBUGS\\WinBUGS14"
          DIC=FALSE)
}


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
    mybugs(data,init,parameters);
}