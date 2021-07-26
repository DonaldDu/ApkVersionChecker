package com.dhy.versionchecker

import io.reactivex.Observable
import io.reactivex.android.schedulers.AndroidSchedulers
import io.reactivex.disposables.Disposable
import io.reactivex.schedulers.Schedulers


class Rx2Wrapper<V : IVersion>(api: () -> Observable<V>) : RxWrapper<Observable<V>>(api) {
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