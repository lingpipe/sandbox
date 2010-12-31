from .util import *

def agreement(item,anno,label):
    (I,J,K,N,Is,Js,Ks,Ns) = lengths(item,anno,label)
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
    for j1 in Js:
        for j2 in Js:
            print "{0:4d},{1:4d},{2:8d}/{3:8d}={4:6.4f}".\
                    format(j1,j2,
                           agr[j1][j2],
                           tot[j1][j2],
                           float(agr[j1][j2])/float(tot[j1][j2]))

                

    

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

    
    
    
    
