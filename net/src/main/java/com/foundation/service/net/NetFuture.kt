package com.foundation.service.net

import com.foundation.service.net.state.NetStateListener
import kotlinx.coroutines.Job

class NetFuture(
    private val job: Job, private val proxy: NetStateProxy
) {
    fun onStart(block: () -> Unit): NetFuture {
        proxy.start = block
        return this
    }

    fun onSuccess(block: () -> Unit): NetFuture {
        proxy.success = block
        return this
    }

    fun onFailure(failure: (tagName: String?, e: Throwable) -> Unit): NetFuture {
        proxy.failure = failure
        return this
    }

    fun start(nsl: NetStateListener? = null): Job {
        proxy.nsl = nsl
        job.start()
        return job
    }

}

class NetStateProxy :
    NetStateListener {
    var start: (() -> Unit)? = null
    var success: (() -> Unit)? = null
    var failure: ((tagName: String?, e: Throwable) -> Unit)? = null
    var nsl: NetStateListener? = null

    override fun onStart() {
        nsl?.onStart()
        start?.invoke()
    }

    override fun onSuccess() {
        success?.invoke()
        nsl?.onSuccess()
    }

    override fun onFailure(tagName: String, e: Throwable) {
        failure?.invoke(tagName, e)
        nsl?.onFailure(tagName, e)
    }
}