# -*- coding: utf-8 -*-
"""
Created on Sun Oct  9 11:27:54 2016

@author: ericgrimson
"""

# O(n)
# Average n/2
# Best 1
def linear_search(L, e):
    found = False
    for i in range(len(L)):
        if e == L[i]:
            found = True
    return found

testList = [1, 3, 4, 5, 9, 18, 27]

# O(n)
def search(L, e):
    for i in range(len(L)):
        if L[i] == e:
            return True
        if L[i] > e:
            return False
    return False


# O(n^2)
def isSubset(L1, L2):
    for e1 in L1:
        matched = False
        for e2 in L2:
            if e1 == e2:
                matched = True
                break
        if not matched:
            return False
    return True


testSet = [1, 2, 3, 4, 5]
testSet1 = [1, 5, 3]
testSet2 = [1, 6]

# is L1 subset of L2?
print(isSubset(testSet, testSet1)) # False
print(isSubset(testSet1, testSet)) # True
print(isSubset(testSet2, testSet)) # False

def intersect(L1, L2):
    tmp = []
    for e1 in L1:
        for e2 in L2:
            if e1 == e2:
                tmp.append(e1)
    res = []
    for e in tmp:
        if not(e in res):
            res.append(e)
    return res

ls1 = [1,2,3,4,4]
ls2 = [3,4,4,5,6]
# all unique intersecting items
print(intersect(ls1, ls2)) # [3,4]