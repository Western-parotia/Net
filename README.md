# NET Library

基于kotlin+协程封装的网络请求库。结合NetViewModel获取请求状态监听，状态判断。分层处理业务数据。


核心功能：

* 1.service快速创建（带缓存）
* 2.NetViewModel，网络状态拦截，数据状态拦截，异常捕获 
* 3.域名切换 包含静态多域名 与动态多域名
* 4.提供流式API

功能结构图：

![结构图](images/function_structure.jpg) 

引用：

```kotlin
 implementation("com.foundation.service:net:1.0.6")
```

基于协程的封装，分层提供异常通知，需要具备协程基础使用的知识。

会创建协程，会切分线程即可。

* 1.0.6 更新：协程启动方式从立即启动改为惰性启动，需要主动掉用start()

# 网络请求使用示例：

* 1、初始化

```kotlin
        val okHttpClient =  OkHttpClient.Builder().addDynamicDomainSkill()
            .build()
        val retrofit = Retrofit.Builder()
            .client( okHttpClient)
            .baseUrl("https://www.xx.com")
            .build()
        NetManager.init(retrofit,application,BuildConfig.DEBUG)
```

* 2、创建支持协程的网络请求接口

```kotlin
    @GET("banner/json")
    suspend fun getBanner(): Response<BaseApiResponse<List<BannerEntity>>>
```


* 3、在NetViewModle子类中加载数据


```kotlin

netLaunch("加载banner") {
    val data = withBusiness {
        homeApi.getBanner()
    }
    _bannerData.value = data
}.onStart {

}.onSuccess {

}.onFailure { tagName, e ->

}.start()
//也支持一次性接管全部状态
    .start(your listener)

```

# 核心类：

### 1 NetRC
网络请求

```kotlin
    /**
 * 采用惰性启动协程执行任务，掉用NetFuture.start()启动执行
 *
 */
private fun launch(
    block: suspend CoroutineScope.() -> Unit,
    tag: String,
    appointScope: CoroutineScope,
): NetFuture {
    val stateProxy = NetStateProxy()
    val exHandler = CoroutineExceptionHandler { ctx, throwable ->
        val name: String? = ctx[CoroutineName]?.name
        "ctxName:$name ,thread:${Thread.currentThread().name},$throwable ".log(TAG)
        val transformThrowable = transformHttpException(throwable)
        stateProxy.onFailure(tag, transformThrowable)
    }

    val ctx = exHandler + CoroutineName(tag)
    val job = appointScope.launch(ctx, start = CoroutineStart.LAZY) {
        stateProxy.onStart()
        if (!networkIsAvailable(NetManager.app)) {
            throw NetException.createNetWorkType("网络链接不可用")
        }
        block.invoke(this)
        stateProxy.onSuccess()
    }
    return NetFuture(job, stateProxy)
}

```

### 2 NetLinkErrorType

```kotlin
    //默认异常状态，通常是发生在收到网络数据后，处理数据时异常：比如 json 解析出错
    CODE_NORMAL(-900_0),

//网络不可用
CODE_NETWORK_OFF(-900_1),

//网络链接错误
CODE_CONNECT_ERROR(-900_2),

//网络响应错误
CODE_RESPONSE_ERROR(-900_3)
```

域名切换源码为开源库`RetrofitUrlManager`
，[点击直达源码仓库](https://github.com/JessYanCoding/RetrofitUrlManager/blob/master/README-zh.md)

# 运行时切换域名使用示例：

* 1 为Okhttp添加指定拦截器

```kotlin
OkHttpClient.Builder().addDynamicDomainSkill()
```

或者

```kotlin
NetManager.addDynamicDomainSkill(okhttpBuilder)
```

* 1 切换指定了`Domain-Name header`的接口的域名

前置：必须要设置`Domain-Name header`

```kotlin
    @Headers("Domain-Name: searchDomain")//searchDomain 是替换域名时用于匹配接口的KEY
@GET("/")
suspend fun specialSearch(): Response<ResponseBody>
```

切换

```kotlin
 NetManager.putDomain("searchDomain", "https://www.bing.com")
```

* 1 全局切换`BaseUrl`

```kotlin
 NetManager.setGlobalDomain("https://www.baidu.com")
```

切换时的优先级最低与`Domain-Name header` 只会切换所有没指定`Domain-Name header`的接口

被替换掉的`BaseUrl`就是下面代码中设置的`baseUrl `参数

```kotlin

val retrofit = Retrofit.Builder()
    .client(okHttpClient)
    .baseUrl("https://www.google.com")
    .build()

```





