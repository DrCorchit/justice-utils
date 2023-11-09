package com.drcorchit.utils.exceptions

class MissingDependencyException constructor(message: String) :
    RuntimeException("Missing dependency: $message") {
    constructor(clazz: Class<*>) : this(clazz.name)
}