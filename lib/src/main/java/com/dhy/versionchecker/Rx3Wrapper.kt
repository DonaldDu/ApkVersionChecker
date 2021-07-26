package com.dhy.versionchecker

import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.disposables.Disposable
import io.reactivex.rxjava3.schedulers.Schedulers

class Rx3Wrapper<V : IVersion>(api: () -> Observable<V>) : RxWrapper<Observable<V>>(api) {
    private var disposable: Disposable? = null
    override fun call(onGetVersion: (IVersion) -> Unit) {
        if (disposable?.isDisposed == false) return
        disposable = api().subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({
                dispose()
                if (it.isNew) onGetVersion(it)
            }, {
                dispose()
            }, {
                dispose()
            })
    }

    override fun dispose() {
        disposable?.dispose()
    }
}