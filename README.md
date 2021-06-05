# NET Library

基于kotlin+协程封装的网络请求库。结合NetViewModel获取请求状态监听，状态判断。分层处理业务数据。




核心功能：

* 1.service快速创建（带缓存）
* 2.NetViewModel，网络状态拦截，数据状态拦截，异常捕获 
* 3.动态切换域名

功能结构图：

![结构图](images/function_structure.jpg) 

使用：

```kotlin
 implementation("com.foundation.service:net:最新版本")
```


基于协程的封装，侵入性极低，只要会用协程，即可直接使用。


NetViewModel 详细使用请参考 `HomeVM`类

域名切换源码为开源库`RetrofitUrlManager`，[点击直达源码仓库](https://github.com/JessYanCoding/RetrofitUrlManager/blob/master/README-zh.md)

## 快速使用：

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
        netLaunch({
            val data = withBusiness {
                homeRepo.homeApi.getBanner()
            }
            _bannerData.value = data
        }, WanAndroidNetStateHandler(true, _loadEventLiveData), "加载 banner")

```


* 独立使用

需要调用者管理好appointScope的销毁

```kotlin
 NetRC.uiLaunch({
                NetRC.withIO {
                    delay(10_000)
                }
                "btnLongTime run finished".log("btnLongTime--")
            }, object : NetStateListener {
                override fun onStart() {
                    "onStart: ".log("btnLongTime--")
                }

                override fun onSuccess() {
                    "onSuccess: ".log("btnLongTime--")
                }

                override fun onFailure(e: Throwable) {
                    "onFailure: $e".log("btnLongTime--")
                }
            }, appointScope = MainScope())

```

## 核心类：

### 一、NetRC
网络请求

```kotlin
    /**
     * 回调在主线程
     * block 作为匿名协程拓展，具备包含子协程的能力
     * 但应该完全避免其中包含独立协程（任何情况下，都不应该使用独立协程嵌套，这样会丧失"父子"协程的控制）
     * [NetStateListener] 作为状态监听器，通常你应该自己实现一个子类，
     * 统一处理状态满足匹配UI展示需要
     *
     * 异常捕获：不管是 withContext(IO) 还是 async(IO) 中发生的异常，最终都会
     * 在根协程的线程环境获取到异常信息。
     * @param block
     * @param state
     * @param tag
     * @param appointScope 如果未指定协程 则会创建一个新的[CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)] 协程
     * @return 如果未指定可自动取消的appointScope，则需要自主控制取消
     */
    fun uiLaunch(
        block: suspend CoroutineScope.() -> Unit,
        state: NetStateListener? = null,
        tag: String = "",
        appointScope: CoroutineScope
    )
    
  /**
     * 回调在子线程
     * @param block
     * @param state
     * @param tag
     * @param appointScope
     * @return
     */
    fun ioLaunch(
        block: suspend CoroutineScope.() -> Unit,
        state: NetStateListener? = null,
        tag: String = "",
        appointScope: CoroutineScope
    )
```
### 二、NetLinkErrorType

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




