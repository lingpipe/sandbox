var pi[K], z[I], theta[J,K], alpha[K], phi[K], tau, y[N], unitK[K], preferK[K,K];

data {
    for (k in 1:K) {
        unitK[k] = 1
    }
    for (k in 1:K) {
        for (k2 in 1:(k-1)) {
           prefer[k,k2] = 1
        }
        for (k2 in (k+1):K) {
            prefer[k,k2] = 1
        }
        prefer[k,k] = K
    }
}


