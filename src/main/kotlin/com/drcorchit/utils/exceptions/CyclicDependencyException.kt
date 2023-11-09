package com.drcorchit.utils.exceptions

class CyclicDependencyException(o1: Any, o2: Any) :
    RuntimeException("Cyclic dependency introduced when $o1 depends on $o2")