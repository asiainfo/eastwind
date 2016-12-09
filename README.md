# eastwind-io
基于Netty、Kryo、Json的Java网络交互框架，初期支持RPC、SOA，后期将支持消息、流。

## 1. RPC
支持二进制、Json、HTTP 三种协议，支持同步、异步 两种方式。
demo源码见eastwind.io.text。

### 1.1 server：
		
	EastWindServer eastwind = new EastWindServer(TEST_SERVER);
	eastwind.registerProvider(new FruitProviderImpl());
	eastwind.start();
	System.in.read();
		
使用默认端口，启动组名为TEST_SERVER的服务端。<br/>
控制台地址：http://127.0.0.1:12468/provider.html?console<br/>
服务端只需开放一个端口即可支持多种协议。<br/>

### 1.2 client：
1.2.1 二进制方式<br/>
二进制方式用Proxy方式创建invoker，效率高，需要提供严格的Interface、Class。
若服务端逻辑向后兼容，客户端不必随服务端同时升级。
二进制同步，创建一个Fruit：

	FruitProvider fruitProvider = client.createProxyInvoker(TestServer.TEST_SERVER, FruitProvider.class);
	fruitProvider.create("apple");
	
二进制异步，查询Fruit列表:

	FruitProvider fruitProvider = client.createProxyInvoker(TestServer.TEST_SERVER, FruitProvider.class);
	InvocationFuture<List<Fruit>> future = InvocationUtil.makeNextAsync().get(fruitProvider.queryAll());
	future.addListener(new QueryAllInvocationListener());
		
1.2.2 Smart方式<br/>
Smart方式，目前使用Json序列化，效率一般，后期将使用更加高效的序列化方式。
Smart方式不需要Interface，需要不严格的Result Class。
同样的，若服务端逻辑向后兼容，客户端不必随服务端同时升级。
Smart同步，创建一个Fruit:

	SmartInvoker<Integer> fruitCreator = client.createSmartInvoker(TestServer.TEST_SERVER, "fruit/create", int.class);
	fruitCreator.invoke("pear")
	
Smart异步，查询Fruit列表:

	SmartInvoker<List<Fruit>> fruitQueryer = client.createSmartInvoker(TestServer.TEST_SERVER, "fruit/queryAll", new TypeReference<List<Fruit>>() {});
	InvocationFuture<List<Fruit>> future = fruitQueryer.invokeAsynchronously();
	future.addListener(new QueryAllPrintInvocationListener());
		
1.2.3 HTTP方式<br/>
HTTP方式一般用于调试、自测，不建议线上环境开放。
uri的query部分为json形式的入参。
	
	http:/127.0.0.1:12468/fruit/create?orange
	http:/127.0.0.1:12468/fruit/queryAll
	
## 2. 路由
路由解决如下问题：
	
	1、AB2位测试工程师，同环境下，测试同服务组的ab2台机器，防止干扰，控制A的请求落在a、B的请求落在b。
	2、线上环境升级，经验证后，服务端才开放服务。
	3、灰度发布，逐步扩大导流到升级机器。
	4、线上环境，部分机器仅对部分VIP客户端组开放。
	5、...
