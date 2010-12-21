# Library Module

def hello(a="world"):
    print "hello", a
    

def em_dawid_skene(alpha,   # float[K][K]
                   beta,    # float[K]
                   item,    # int[N]
                   anno,    # int[N]
                   label):  # int[N]
    I = max(item)
    J = max(anno)
    K = max(label)
    N = len(item)

    Is = range(0,I)
    Js = range(0,J)
    Ks = range(0,K)

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

    prevalence = []          # double[K]
    category = [ [] ]        # double[I][K]
    accuracy = [ [ [] ] ]    # double[J][K][K]
    for k in Ks:
        prevalence[k] = 1/K
    for i in Is:
        for k in Ks:
            category[i][k] = 1/K
    for j in Js:
        for k1 in Ks:
            for k2 in Ks:
                accuracy[j][k1][k2] = 0.7 if k1 == k2 else 1/(K-1)
          
    while true:
        # E Step: p(cat[i]|...)
        for i in Is:
            list_copy(prevalence,category[i],Ks)
            for n in Ns:
                for k in Ks:
                    category[item[n]][k] *= accuracy[anno[n]][k][label[n]]
                    for i in Is:
                        linearNorm(category[i],Ks)


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
        
        yield (prevalence,category,accuracy)

            
def list_copy(froms,tos,indexes):
    for i in indexes:
        tos[i] = froms[i]

def prob_norm(theta,indexes):
    Z = sum(theta)
    for i in indexes:
        theta[i] /= Z
        
            
        
    
