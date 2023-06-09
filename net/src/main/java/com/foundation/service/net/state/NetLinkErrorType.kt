package com.foundation.service.net.state

/**
 * 区分网络链路错误类型
 *
 * create by zhusw on 5/26/21 12:02
 */
enum class NetLinkErrorType(val value: Int) {
    //默认异常状态，通常是发生在收到网络数据后，处理数据时异常：比如 json 解析出错
    CODE_LOCAL(-900_0),

    //网络不可用
    CODE_NETWORK_OFF(-900_1),

    //网络链接错误
    CODE_CONNECT_ERROR(-900_2),

    //网络响应错误
    CODE_RESPONSE_ERROR(-900_3)

}

