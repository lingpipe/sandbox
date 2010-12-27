# Library Module
from math import log

def dawid_skene_mle_em(item,    # int[N]
                       anno,    # int[N]
                       label):  # int[N]

    I = max(item)+1
    J = max(anno)+1
    K = max(label)+1
    N = len(item)

    Is = range(I)
    Js = range(J)
    Ks = range(K)
    Ns = range(N)

    warn_missing_vals("item",item)
    warn_missing_vals("anno",anno)
    warn_missing_vals("label",label)

    # initialize params
    init_accuracy = 0.6
    prevalence = alloc_vec(K,1.0/K)
    category = alloc_mat(I,K,1.0/K)
    accuracy = alloc_tens(J,K,K,(1.0 - init_accuracy)/(K-1.0))
    for j in Js:
        for k in Ks:
            accuracy[j][k][k] = init_accuracy
    
    while True:

        # E: p(cat[i]|...) 
        for i in Is:
            list_copy(prevalence,category[i],Ks)
        for n in Ns:
            for k in Ks:
                category[item[n]][k] *= accuracy[anno[n]][k][label[n]]

        # log likelihood here to reuse intermediate category calc
        log_likelihood = 0.0
        for i in Is:
            likelihood_i = 0.0
            for k in Ks:
                likelihood_i += category[i][k]
            log_likelihood_i = log(likelihood_i)
            log_likelihood += log_likelihood_i

        for i in Is:
            prob_norm(category[i],Ks)

        # return here with E[cat|prev,acc] and LL(prev,acc;y)
        yield (log_likelihood,prevalence,category,accuracy)

        # M: prevalence* + accuracy*
        fill_vec(prevalence,0.0)
        for i in Is:
            for k in Ks:
                prevalence[k] += category[i][k]
        prob_norm(prevalence,Ks)

        fill_tens(accuracy,0.0)
        for n in Ns:
            for k in Ks:
                accuracy[anno[n]][k][label[n]] += category[item[n]][k]
        for j in Js:
            for k in Ks:
                prob_norm(accuracy[j][k],Ks)




def em_ds_prior(item,            # int[N]
                anno,            # int[N]
                label,           # int[N]
                alpha=None,      # float[K][K]
                beta=None,       # float[K]
                supervis=None):  # int[I]
    I = max(item)+1
    J = max(anno)+1
    K = max(label)+1
    N = len(item)

    if supervis != None:
        K = max(K,max(supervis))

    Is = range(I)
    Js = range(J)
    Ks = range(K)
    Ns = range(N)

    if alpha == None:
        alpha = alloc_vec(K)
    if beta == None:
        beta = alloc_mat(K,K)
    if supervis == None:
        supervis = alloc_vec(I,None)

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
    if len(supervis) != I:
        raise ValueError("len(cat) != I")

    prevalence = alloc_vec(K,1.0/K)
    category = alloc_mat(I,K,1.0/K)
    for i in Is:
        if supervis[i] != None:
            for k in Ks:
                category[i][k] = 1.0 if k == supervis[k] else 0.0
            category[i][supervis[i]] = 1.0
    accuracy = alloc_tens(J,K,K,0.3/(K-1.0))
    for j in Js:
        for k in Ks:
            accuracy[j][k][k] = 0.7

    while True:
        # E Step: p(cat[i]|...)
        for i in Is:
            list_copy(prevalence,category[i],Ks)
        for n in Ns:
            for k in Ks:
                if supervis[item[n]] == None:
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


def warn_missing_vals(varname,xs):
    missing = set(xs) - set(range(max(xs)+1))
    if len(missing) > 0:
        print "Missing values in ",varname,"=",missing

def list_copy(froms,tos,indexes):
    for i in indexes:
        tos[i] = froms[i]


def fill_vec(xs,y):
    i = 0
    while i < len(xs):
        xs[i] = y
        i += 1

def fill_mat(xs,y):
    i = 0
    while i < len(xs):
        fill_vec(xs[i],y)
        i += 1

def fill_tens(xs,y):
    i = 0
    while i < len(xs):
        fill_mat(xs[i],y)
        i += 1

def prob_norm(theta,indexes):
    Z = sum(theta)
    n = len(theta) - 1
    while n >= 0:
        theta[n] /= Z
        n -= 1


def alloc_vec(N,x=0.0):
    result = []
    n = 0
    while n < N:
        result.append(x)
        n += 1
    return result


def alloc_mat(M,N,x=0.0):
    result = []
    m = 0
    while m < M:
        result.append(alloc_vec(N,x))
        m += 1
    return result
                   
    
def alloc_tens(M,N,J,x=0.0):
    result = []
    m = 0
    while m < M:
        result.append(alloc_mat(N,J,x))
        m += 1
    return result
            
        
    
