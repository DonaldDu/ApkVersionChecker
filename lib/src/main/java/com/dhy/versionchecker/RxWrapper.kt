package com.dhy.versionchecker


abstract class RxWrapper<T>(val api: () -> T) {
    abstract fun dispose()
    abstract fun call(onGetVersion: (IVersion) -> Unit)
}