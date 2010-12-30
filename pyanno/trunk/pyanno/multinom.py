# Library Module
from math import log
import pymc
from .util import *

def sim_ordinal(I,J,K,alpha=None,beta=None):

    # test input params here

    Is = range(I)
    Js = range(J)
    Ks = range(K)
    N = I*J
    Ns = range(N)
        
    if alpha == None:
        alpha = alloc_mat(K,K)
        for k1 in Ks:
            for k2 in Ks:
                alpha[k1][k2] = max(1,(K + (0.5 if k1 == k2 else 0) - abs(k1 - k2))**4)
        
    if beta == None:
        beta = alloc_vec(K,2.0)

    # simulated params
    beta = alloc_vec(K,2.0)

    prevalence = pymc.rdirichlet(beta).tolist()
    prevalence.append(1.0-sum(prevalence)) # complete
    category = []
    for i in Is:
        category.append(pymc.rcategorical(prevalence).tolist())

    accuracy = alloc_tens(J,K,K)
    for j in Js:
        for k in Ks:
            accuracy[j][k] = pymc.rdirichlet(alpha[k]).tolist()
            accuracy[j][k].append(1.0-sum(accuracy[j][k]))

    # simulated data
    item = []
    anno = []
    label = []
    for i in Is:
        for j in Js:
            item.append(i)
            anno.append(j)
            label.append(pymc.rcategorical(accuracy[j][category[i]]).tolist())
    N = len(item)

    return (prevalence,category,accuracy,item,anno,label)



        

def mle_em(item,    # int[N]
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
    init_accuracy = 0.4
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

