# Library Module
from math import log

def em_dawid_skene(item,   #int[N]
                   anno,   #int[N]
                   label): #int[N]
    K = max(label)+1
    alpha = fill_vec(K)
    beta = fill_mat(K,K)
    for x in em_ds_prior(item,anno,label,alpha,beta):
        yield x


def em_ds_prior(item,    # int[N]
                anno,    # int[N]
                label,   # int[N]
                alpha,   # float[K][K]
                beta):   # float[K]
    I = max(item)+1
    J = max(anno)+1
    K = max(label)+1
    N = len(item)

    Is = range(I)
    Js = range(J)
    Ks = range(K)
    Ns = range(N)

    if len(alpha) != K:
        raise ValueError("len(alpha) != K")
    for k in Ks:
        if len(alpha[k]) != K:
            raise ValueError("len(alpha[k]) != K")
    if len(beta) != K:
        raise ValueError("len(beta) != K")
    if len(anno) != N:
        raise ValueError("len(anno) != N")
    if len(label) != N:
        raise ValueError("len(label) != N")

    prevalence = fill_vec(K,1.0/K)
    category = fill_mat(I,K,1.0/K)
    accuracy = fill_tens(J,K,K,0.3/(K-1.0))
    for j in Js:
        for k in Ks:
            accuracy[j][k][k] = 0.7

    while True:
        # E Step: p(cat[i]|...)
        for i in Is:
            list_copy(prevalence,category[i],Ks)
        for n in Ns:
            for k in Ks:
                category[item[n]][k] *= accuracy[anno[n]][k][label[n]]

        log_likelihood = 0.0
        for i in Is:
            likelihood_i = 0.0
            for k in Ks:
                likelihood_i += category[i][k]
            log_likelihood_i = log(likelihood_i)
            # print("log_likelihood_",i,"=",log_likelihood_i)
            log_likelihood += log_likelihood_i

        for i in Is:
            prob_norm(category[i],Ks)

        yield (log_likelihood,prevalence,category,accuracy)  

        # M step 1: prevalence*
        list_copy(beta,prevalence,Ks)
        for i in Is:
            for k in Ks:
                prevalence[k] += category[i][k]
        prob_norm(prevalence,Ks)

        # M step 2: accuracy*
        for j in Js:
            for k in Ks:
                list_copy(alpha[k],accuracy[j][k],Ks)
        for n in Ns:
            for k in Ks:
                accuracy[anno[n]][k][label[n]] += category[item[n]][k]
        for j in Js:
            for k in Ks:
                prob_norm(accuracy[j][k],Ks)


def list_copy(froms,tos,indexes):
    for i in indexes:
        tos[i] = froms[i]


def prob_norm(theta,indexes):
    Z = sum(theta)
    for i in indexes:
        theta[i] /= Z


def fill_vec(N,x=0.0):
    result = []
    n = 0
    while n < N:
        result.append(x)
        n += 1
    return result


def fill_mat(M,N,x=0.0):
    result = []
    m = 0
    while m < M:
        result.append(fill_vec(N,x))
        m += 1
    return result
                   
    
def fill_tens(M,N,J,x=0.0):
    result = []
    m = 0
    while m < M:
        result.append(fill_mat(N,J,x))
        m += 1
    return result
            
        
    
