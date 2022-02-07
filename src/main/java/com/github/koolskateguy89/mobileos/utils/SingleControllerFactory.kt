package com.github.koolskateguy89.mobileos.utils

import javafx.util.Callback
import java.lang.IllegalArgumentException
import lombok.SneakyThrows

class SingleControllerFactory(val obj: Any) : Callback<Class<*>, Any> {
    val clazz: Class<*> = obj.javaClass

    override fun call(param: Class<*>): Any = if (param == clazz) {
        obj
    } else {
        param.getConstructor().newInstance()
    }
}
