package com.example.presentation.base

import java.lang.ref.WeakReference

open class PresenterMvp<V : ViewMvp> {
    private var weakReference: WeakReference<V>? = null
    fun attachView(view: V) {
        if (!isViewAttached) {
            weakReference = WeakReference(view)
        }
        onAttachView()
    }

    protected open fun onAttachView() {

    }

    private fun detachView() {
        weakReference?.clear()
        weakReference = null
        onDetachView()
    }

    protected open fun onDetachView() {

    }

    val view: V?
        get() = weakReference?.get()

    private val isViewAttached: Boolean
        get() = weakReference != null && weakReference!!.get() != null
}