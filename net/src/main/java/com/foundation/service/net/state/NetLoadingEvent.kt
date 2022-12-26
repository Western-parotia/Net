package com.foundation.service.net.state

/**
 * create by zhusw on 5/25/21 18:00
 */
sealed class NetLoadingEvent {
    object StartEvent : NetLoadingEvent()
    object StopEvent : NetLoadingEvent()
    data class ErrorEvent(val code: Int, val msg: String = "") : NetLoadingEvent()

}




