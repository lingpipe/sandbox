# Library Module

def em_dawid_skene(alpha,   # float[K][K]
                   beta,    # float[K]
                   item,    # int[N]
                   anno,    # int[N]
                   label):  # int[N]
    I = max(item)+1
    J = max(anno)+1
    K = max(label)+1
    N = len(item)

    Is = range(0,I)
    Js = range(0,J)
    Ks = range(0,K)
    Ns = range(0,N)

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

    prevalence = []    # double[K]
    category = []      # double[I][K]
    accuracy = []      # double[J][K][K]
    for k in Ks:
        prevalence.append(1.0/K)
    for i in Is:
        category.append([])
        for k in Ks:
            category[i].append(1.0/K)
    for j in Js:
        accuracy.append([])
        for k1 in Ks:
            accuracy[j].append([])
            for k2 in Ks:
                accuracy[j][k1].append(0.7 if k1 == k2 else 1.0/(K-1.0))

    epoch = 0
    while True:
        epoch += 1
        yield (prevalence,category,accuracy)  # go first to return init
        # E Step: p(cat[i]|...)
        for i in Is:
            list_copy(prevalence,category[i],Ks)
        for n in Ns:
            for k in Ks:
                category[item[n]][k] *= accuracy[anno[n]][k][label[n]]
        for i in Is:
            prob_norm(category[i],Ks)

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
        

            
        
    
