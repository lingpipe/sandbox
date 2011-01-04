# Library Module
from math import log
import numpy
import pymc
from .util import *

def mle(item,
        anno,
        label,
        init_acc=0.5,
        epsilon=0.001,
        max_epochs=1000):

    if epsilon < 0.0:
        raise ValueError("epislon < 0.0")
    if max_epochs < 0:
        raise ValueError("max_epochs < 0")

    log_likelihood_curve = []
    epoch = 0
    diff = float('inf')
    for (ll,prev_mle,cat_mle,accuracy_mle) in mle_em(item,anno,label,init_acc):
        print "  epoch={0:6d}  log lik={1:+10.4f}   diff={2:10.4f}".\
                format(epoch,ll,diff)
        log_likelihood_curve.append(ll)
        if epoch > max_epochs:
            break
        if len(log_likelihood_curve) > 10:
            diff = (ll - log_likelihood_curve[epoch-10])/10.0
            if abs(diff) < epsilon:
                break
        epoch += 1
    return (diff,ll,prev_mle,cat_mle,accuracy_mle)


def mle_em(item,    # int[N]
           anno,    # int[N]
           label,   # int[N]
           init_accuracy=0.5):

    I = max(item)+1
    J = max(anno)+1
    K = max(label)+1
    N = len(item)

    Is = range(I)
    Js = range(J)
    Ks = range(K)
    Ns = range(N)

    if len(anno) != N:
        raise ValueError("len(item) != len(anno)")
    if len(label) != N:
        raise ValueError("len(item) != len(label)")
    if init_accuracy < 0.0 or init_accuracy > 1.0:
        raise ValueError("init_accuracy not in [0,1]")
    for n in Ns:
        if item[n] < 0:
            raise ValueError("item[n] < 0")
        if anno[n] < 0:
            raise ValueError("anno[n] < 0")
        if label[n] < 0:
            raise ValueError("label[n] < 0")

    warn_missing_vals("item",item)
    warn_missing_vals("anno",anno)
    warn_missing_vals("label",label)

    # initialize params
    prevalence = alloc_vec(K,1.0/K)
    category = alloc_mat(I,K,1.0/K)
    accuracy = alloc_tens(J,K,K,(1.0 - init_accuracy)/(K-1.0))
    for j in Js:
        for k in Ks:
            accuracy[j][k][k] = init_accuracy

    
    while True:

        # E: p(cat[i]|...) 
        for i in Is:
            vec_copy(prevalence,category[i])
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
            prob_norm(category[i])


        # return here with E[cat|prev,acc] and LL(prev,acc;y)
        yield (log_likelihood,prevalence,category,accuracy)

        # M: prevalence* + accuracy*
        fill_vec(prevalence,0.0)
        for i in Is:
            for k in Ks:
                prevalence[k] += category[i][k]
        prob_norm(prevalence)

        fill_tens(accuracy,0.0)
        for n in Ns:
            for k in Ks:
                accuracy[anno[n]][k][label[n]] += category[item[n]][k]
        for j in Js:
            for k in Ks:
                prob_norm(accuracy[j][k])


def map(item,
        anno,
        label,
        alpha=None,
        beta=None,
        init_acc=0.5,
        epsilon=0.001,
        max_epochs=1000):

    if epsilon < 0.0:
        raise ValueError("epislon < 0.0")
    if max_epochs < 0:
        raise ValueError("max_epochs < 0")

    llp_curve = []
    epoch = 0
    diff = float('inf')
    for (lp,ll,prev_mle,cat_mle,accuracy_mle) in mle_em(item,anno,label,alpha,beta,init_acc):
        print "  epoch={0:6d}  log lik={1:+10.4f}  log prior={1:+10.4f}  llp={1:+10.4f}   diff={2:10.4f}".\
                format(epoch,ll,lp,ll+lp,diff)
        log_likelihood_curve.append(ll+lp)
        if epoch > max_epochs:
            break
        if len(log_likelihood_curve) > 10:
            diff = (ll - log_likelihood_curve[epoch-10])/10.0
            if abs(diff) < epsilon:
                break
        epoch += 1
    return (diff,ll,lp,prev_mle,cat_mle,accuracy_mle)


# alpha and beta previous counts, not alpha/beta Dirichlet here
def map_em(item,
           anno,
           label,
           alpha=None,
           beta=None,
           init_accuracy=0.5):

    I = max(item)+1
    J = max(anno)+1
    K = max(label)+1
    N = len(item)

    Is = range(I)
    Js = range(J)
    Ks = range(K)
    Ns = range(N)

    if alpha == None:
        alpha = alloc_mat(K,K,0.0)
    if beta == None:
        beta = alloc_vec(K,0.0)

    if len(anno) != N:
        raise ValueError("len(item) != len(anno)")
    if len(label) != N:
        raise ValueError("len(item) != len(label)")
    if init_accuracy < 0.0 or init_accuracy > 1.0:
        raise ValueError("init_accuracy not in [0,1]")
    for n in Ns:
        if item[n] < 0:
            raise ValueError("item[n] < 0")
        if anno[n] < 0:
            raise ValueError("anno[n] < 0")
        if label[n] < 0:
            raise ValueError("label[n] < 0")
    if len(alpha) != K:
        raise ValueError("len(alpha) != K")
    for k in Ks:
        if len(alpha[k]) != K:
            raise ValueError("len(alpha[k]) != K")
    if len(beta) != K:
        raise ValueError("len(beta) != K")

    warn_missing_vals("item",item)
    warn_missing_vals("anno",anno)
    warn_missing_vals("label",label)

    # initialize params
    prevalence = alloc_vec(K)
    vec_copy(beta,prevalence)
    prob_norm(prevalence)

    category = alloc_mat(I,K)
    for i in Is:
        vec_copy(beta,category[i])
        prob_norm(category[i])

    accuracy = alloc_tens(J,K,K,(1.0 - init_accuracy)/(K-1.0))
    for j in Js:
        for k in Ks:
            accuracy[j][k][k] = init_accuracy
    
    beta_a = numpy.array(beta)
    alpha_a = []
    for k in Ks:
        alpha_a.append(numpy.array(alpha[k]))

    while True:

        # E: p(cat[i]|...) 
        for i in Is:
            vec_copy(prevalence,category[i])
        for n in Ns:
            for k in Ks:
                category[item[n]][k] *= accuracy[anno[n]][k][label[n]]

        # need log p(prev|beta) + SUM_k log p(acc[k]|alpha[k])
        # log likelihood here to reuse intermediate category calc
        log_likelihood = 0.0
        for i in Is:
            likelihood_i = 0.0
            for k in Ks:
                likelihood_i += category[i][k]
            log_likelihood_i = log(likelihood_i)
            log_likelihood += log_likelihood_i

        log_prior = 0.0
        prevalence_a = numpy.array(prevalence[0:(K-1)])
        log_prior += pymc.dirichlet_like(prevalence_a,beta_a)
        for j in Js:
            for k in Ks:
                acc_j_k_a = numpy.array(accuracy[j][k][0:(K-1)])
                log_prior += pymc.dirichlet_like(acc_j_k_a,alpha_a[k])

        for i in Is:
            prob_norm(category[i])

        # return here with E[cat|prev,acc] and LL(prev,acc;y)
        yield (log_prior,log_likelihood,prevalence,category,accuracy)

        # M: prevalence* + accuracy*
        vec_copy(beta,prevalence)
        for i in Is:
            for k in Ks:
                prevalence[k] += category[i][k]
        prob_norm(prevalence)

        for j in Js:
            for k in Ks:
                vec_copy(alpha[k],accuracy[j][k])
        for n in Ns:
            for k in Ks:
                accuracy[anno[n]][k][label[n]] += category[item[n]][k]
        for j in Js:
            for k in Ks:
                prob_norm(accuracy[j][k])



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
                alpha[k1][k2] = max(1,(K + (0.5 if k1 == k2 else 0) \
                                       - abs(k1 - k2))**4)
        
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



