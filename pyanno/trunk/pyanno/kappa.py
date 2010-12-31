from .util import *

def agr(confusion_mat):
    tot = mat_sum(confusion_mat)
    agr = 0
    for k in range(len(confusion_mat)):
        agr += confusion_mat[k][k]
    return agr

def s(confusion_mat):
    agr = agr(confusion_mat)
    e_agr = (1.0/float(len(confusion_mat)))**2
    return chance_adj_agr(agr,e_agr)

def pi(confusion_mat):
    agr = agr(confusion_mat)
    K = len(confusion_mat)
    Ks = range(K)
    theta = alloc_vec(K)
    for row in confusion_mat:
        for k in Ks:
            theta[k] += row[k]
    prob_norm(theta,Ks)
    e_agr = 0.0
    for k in Ks:
        e_agr += theta[k]**2
    return chance_adj_agr(agr,e_agr)

def kappa(confusion_mat):
    agr = agr(confusion_mat)
    K = len(confusion_mat)
    Ks = range(K)
    theta1 = alloc_vec(K)
    theta2 = alloc_vec(K)
    for k1 in Ks:
        for k2 in Ks:
            theta1[k1] += confusion_mat[k1][k2]
            theta2[k2] += confusion_mat[k1][k2]
    prob_norm(theta1,Ks)
    prob_norm(theta2,Ks)
    e_agr = 0.0
    for k in Ks:
        e_agr += theta1[k] * theta2[k]
    return chance_adj_agr(agr,e_agr)



(agr,total,agr_prop,s,pi,kappa) = agreement(item,anno,label)

def agreement(item,anno,label):
    (I,J,K,N,Is,Js,Ks,Ns,agr,tot) = lengths(item,anno,label)
    for j1 in Js:
        for j2 in Js:
            print "{0:4d},{1:4d},{2:8d}/{3:8d}={4:6.4f}".\
                    format(j1,j2,
                           agr[j1][j2],
                           tot[j1][j2],
                           float(agr[j1][j2])/float(tot[j1][j2]))

OVERALL PREVALENCE OVERALL OR PAIRWISE?

def prevalence(label,Ks)
    theta = alloc_vec(max(label)+1)
    for k in label:
        theta[k] += 1
    return prob_norm(theta,Ks)
    

B/A
    20 20|40 
    10 50|60
    -----
    30 70

def s(item,anno,label):
    (I,J,K,N,Is,Js,Ks,Ns,agr,tot) = lengths(item,anno,label)
    expected_agr = (1.0/float(K))**2
    for j1 in Js:
        for j2 in Js:
            agr_hat = float(agr[j1][j2])/float(tot[j1][j2]),
            print "{0:4d},{1:4d},{2:8d}/{3:8d}={4:6.4f}".\
                    format(j1,j2,
                           agr[j1][j2],
                           tot[j1][j2],
                           agr_hat,
                           chance_adj_agr(agr_hat,expected_agr))


def pi(item,anno,label):
    (I,J,K,N,Is,Js,Ks,Ns,agr,tot) = lengths(item,anno,label)
    prev = prevalence(label,Ks)
    expected_agr = 0.0
    for theta in prev:
        expected_agr += theta**2
    for j1 in Js:
        for j2 in Js:
            agr_hat = float(agr[j1][j2])/float(tot[j1][j2]),
            print "{0:4d},{1:4d},{2:8d}/{3:8d}={4:6.4f}".\
                    format(j1,j2,
                           agr[j1][j2],
                           tot[j1][j2],
                           agr_hat
                           chance_adj_agr(agr_hat,expected_agr))
    

def kappa(

def chance_adj_agr(agr,expected_agr):
    return (agr - expected_agr)/(1.0 - expected_agr)
                               

    

def lengths(item,anno,label):
    N = len(item)
    if len(anno) != N:
        raise ValueError("len(item) != len(anno)")
    if len(label) != N:
        raise ValueError("len(label) != len(anno)")
    I = max(item)+1
    J = max(anno)+1
    K = max(label)+1
    Is = range(I)
    Js = range(J)
    Ks = range(K)
    Ns = range(N)
    item_to_anno_lab = {}
    for i in Is:
        item_to_anno_lab[i] = []
    for n in Ns:
        item_to_anno_lab[item[n]].append((anno[n],label[n]))
    agr = alloc_mat(J,J)
    tot = alloc_mat(J,J)
    for i in Is:
        for (j1,k1) in item_to_anno_lab[i]:
            for (j2,k2) in item_to_anno_lab[i]:
                tot[j1][j2] += 1
                if (k1 == k2):
                    agr[j1][j2] += 1
    return (I,J,K,N,Is,Js,Ks,Ns,agr,tot)
    
    
