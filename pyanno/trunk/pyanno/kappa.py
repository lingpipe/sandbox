from .util import *

def agr(confusion_mat):
    """Return the agreement rate for the specified confusion matrix.
    
    Keyword arguments:
    confusion_mat -- square confusion matrix of catgorical responses
    """
    tot = mat_sum(confusion_mat)
    agr = 0
    k = len(confusion_mat)
    while k > 0:
        k -= 1
        agr += confusion_mat[k][k]
    return float(agr)/float(tot)

def s(confusion_mat):
    """Return the s statistic for the specified confusion matrix.

    Keyword arguments:
    confusion_mat -- square confusion matrix of catgorical responses
    """
    agr_ = agr(confusion_mat)
    e_agr = 1.0/float(len(confusion_mat))
    return chance_adj_agr(agr_,e_agr)

def pi(confusion_mat):
    """Return Scott's pi statistic for the specified confusion matrix.

    Keyword arguments:
    confusion_mat -- square confusion matrix of catgorical responses
    """
    agr_ = agr(confusion_mat)
    K = len(confusion_mat)
    Ks = range(K)
    theta = alloc_vec(K)
    for k1 in Ks:
        for k2 in Ks:
            theta[k1] += confusion_mat[k1][k2]
            theta[k2] += confusion_mat[k1][k2]
    prob_norm(theta)
    e_agr = 0.0
    for k in Ks:
        e_agr += theta[k]**2
    return chance_adj_agr(agr_,e_agr)

def kappa(confusion_mat):
    """Return Cohen's kappa statistic for the specified confusion matrix.

    Keyword arguments:
    confusion_mat -- square confusion matrix of catgorical responses
    """
    agr_ = agr(confusion_mat)
    K = len(confusion_mat)
    Ks = range(K)
    theta1 = alloc_vec(K)
    theta2 = alloc_vec(K)
    for k1 in Ks:
        for k2 in Ks:
            theta1[k1] += confusion_mat[k1][k2]
            theta2[k2] += confusion_mat[k1][k2]
    prob_norm(theta1)
    prob_norm(theta2)
    e_agr = 0.0
    for k in Ks:
        e_agr += theta1[k] * theta2[k]
    return chance_adj_agr(agr_,e_agr)


def chance_adj_agr(agr,expected_agr):
    """Return the chance-adjusted agreement given the specified agreement
    and expected agreement.  

    Defined by (agr - expected_agr)/(1.0 - expected_agr)

    Keyword arguments:
    agr -- agreement
    expected_agr -- expected agreement
    confusion_mat -- square confusion matrix of catgorical responses
    """
    return (agr - expected_agr)/(1.0 - expected_agr)
          
def K(item,anno,label):
    """Return the K agreement statistic for multiple annotators
    represented by the specified items, annotators, and labels.
    
    Keyword arguments:
    item -- array of item IDs, with item[n] being item in n-th annotation
    anno -- parallel array of annotator IDs, with anno[n] being the 
            annotator for the n-th annotation
    label -- parallel array of labels, with label[n] being the
             label assigned by annotator anno[n] to item item[n]
    """
    if len(item) != len(anno):
        raise ValueError("len(item) != len(anno)")
    if len(label) != len(anno):
        raise ValueError("len(label) != len(anno)")
    I = max(item)+1
    J = max(anno)+1
    K = max(label)+1
    N = len(item)
    theta = global_prevalence(item,label)
    agr_exp = 0.0
    for theta_n in theta:
        agr_exp += theta_n * theta_n
    anno_labels = alloc_vec(I)
    i = I
    while i > 0:
        i -= 1
        anno_labels[i] = []
    n = len(item)
    while n > 0:
        n -= 1
        anno_labels[item[n]].append(label[n])
    tot = 0
    agr = 0
    i = I
    while i > 0:
        i -= 1
        M = len(anno_labels[i])
        if M < 2:
            continue
        anno_labels[i].sort()
        tot += (M * (M-1)) / 2
        start = 0
        m = 1
        while m < M:
            if anno_labels[i][m] != anno_labels[i][start]:
                run = m - start
                agr += (run * (run-1)) / 2
                start = m
            m += 1
        run = m - start
        agr += (run * (run-1)) / 2
    return chance_adj_agr(float(agr)/float(tot),agr_exp)
            
            
def global_prevalence(item,label):
    """Return the global maximum likelihood estimate of prevalence for
    the specified array of labels.  

    The prevalence of a category is estimated to be proportional to
    the sum of adjusted counts for labels, where the adjusted count
    for label[n] is 1 divided by the number of labels for the item[n].

    The length of the returned array will be the number of labels, the
    values will be non-negative, and the values will sum to 1.0

    Keyword arguments:
    item -- array of item identifiers
    label -- parallel array of label assignments
    """
    if len(label) != len(item):
        raise ValueError("len(label) != len(item)")
    item_to_labs = {}
    for i in item:
        item_to_labs[i] = []
    N = len(item)
    n = 0
    while n < N:
        item_to_labs[item[n]].append(label[n])
        n += 1

    K = max(label)+1
    theta = alloc_vec(K)
    for labs in item_to_labs.values():
        increment = 1.0/float(len(labs))
        for k in labs:
            theta[k] += increment
    prob_norm(theta)
    return theta



def confusion_matrices(item,anno,label):
    (I,J,K,N,anno_to_item_map) = encode(item,anno,label)
    for j1 in range(J):
        for j2 in range(j1+1,J):
            conf_mat = alloc_mat(K,K)
            item_set1 = set(anno_to_item_map[j1].keys())
            item_set2 = set(anno_to_item_map[j2].keys())
            item_set_both = item1.intersect(item2)
            for i in item_set_both:
                label1 = anno_to_item_map[j1][i]
                label2 = anno_to_item_map[j2][i]
                conf_mat[label1][label2] += 1
            yield (j1,j2,conf_mat)


# assumes unique annotations with map representation
def encode(item,anno,label):
    """Internal use only"""
    if len(anno) != len(anno):
        raise ValueError("len(item) != len(anno)")
    if len(label) != len(anno):
        raise ValueError("len(label) != len(anno)")
    I = max(item)+1
    J = max(anno)+1
    K = max(label)+1
    N = len(item)
    anno_to_item_map = {}
    for j in range(J):
        anno_to_item_map[j] = {}
    for n in range(N):
        anno_to_item_map[anno[n]][item[n]] = label[n]
    return (I,J,K,N,anno_to_item_map)
    

def print_agr_metrics(item,anno,label):
    """Internal use only"""
    print "{0:>3},{1:>3}, {2:>5}, {3:>5}, {4:>5}, {5:>5}".\
        format("j1","j2","agr","s","pi","kappa")
    for (j1,j2,conf_mat) in confusion_matrices(item,anno,label):
        print "{0:3d},{1:3d}, {2:5.3f}, {3:5.3f}, {4:5.3f}, {5:5.3f}".\
            format(j1,j2,
                   agr(conf_mat),
                   s(conf_mat),
                   pi(conf_mat),
                   kappa(conf_mat))
    

