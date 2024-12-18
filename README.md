# activityGuard：Android Activity混淆

**activityGuard** 是一种针对四大组件进行混淆的解决方案，能够在打包时对apk和aab中的Activity、Service、Application和自定义的view进行名称混淆以提升应用的安全性。

### 目的
*   **防止逆向：** Android四大组件的类名直接暴露在 AndroidManifest.xml 和代码中,容易被反编译后根据名称了解应用逻辑。
*   **增强安全性：** 混淆名称增加了攻击者定位关键组件的难度，降低被针对性攻击的风险。
*   **马甲包：** 降低aab包查重率，避免上架Google Play因查重率过高，导致下架或封号问题

### 原理分析
Android四大组件在打包过程中不能够R8被混淆，因为组件在AndroidManifest.xml以明文形式存在Android系统通过反射创建相关类来启动。所以我们需要在R8执行前修改AndroidManifest.xml和layout布局中的类名，并把新的名称keep的R8的混淆规则中（R8混淆执行时是以keep住的类为节点，如果没引用的类会被移除掉）。最后在通过asm字节码修改类名，就能够实现对四大组件和自定义view实现混淆名称了。
activityGuard 通过自定义Gradle任务在打包过程中修改替换AndroidManifest.xml和layout中的类名和class的类名来实现对Android四大组件的混淆

### 使用方法

```
buildscript {
    repositories {
        maven { url 'https://jitpack.io' }
    }
    dependencies {
        classpath "com.github.denglongfei:activityGuard:1.0.0"
    }
}
```
配置
```
plugins {
    id("activityGuard")
}
//以下均为非必须
actGuard {
    //是否开启
    isEnable = true
    //不需要混淆的类
    whiteClassList = hashSetOf(
        "com.activityGuard.confuseapp.MainActivity1",
        "*.MainActivity2",
    )
    //自己实现混淆类名时
//    //目录混淆
//    obfuscatorDirFunction={ dirName->
//        dirName
//    }
//    //类名混淆
//    obfuscatorClassFunction= { className, dirName->
//        className
//    }
}
```

#### AAB混淆

aab打包混淆流程图如下

![aab.png](https://p0-xtjj-private.juejin.cn/tos-cn-i-73owjymdk6/c1a1fe61d5f14041b7c0cb0fec53f101~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgZGVuZ2xvbmdmZWk=:q75.awebp?policy=eyJ2bSI6MywidWlkIjoiNDI2NTc2MDg0ODM1MjMxMSJ9&rk3s=f64ab15b&x-orig-authkey=f32326d3454f2ac7e96d3d06cdbb035152127018&x-orig-expires=1735147137&x-orig-sign=c0%2BxOSFu3r4aJsljYLv84HmJSco%3D)

#### Apk混淆

apk打包混淆流程图如下

![apk.png](https://p0-xtjj-private.juejin.cn/tos-cn-i-73owjymdk6/7367818fc8244c52960b8c43f43a6966~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgZGVuZ2xvbmdmZWk=:q75.awebp?policy=eyJ2bSI6MywidWlkIjoiNDI2NTc2MDg0ODM1MjMxMSJ9&rk3s=f64ab15b&x-orig-authkey=f32326d3454f2ac7e96d3d06cdbb035152127018&x-orig-expires=1735147137&x-orig-sign=E8agZhXJaGECUKt%2BaXFpXWuF7AQ%3D)
### 最终效果

**apk**

![image.png](https://p0-xtjj-private.juejin.cn/tos-cn-i-73owjymdk6/5890dbd94f9e4f7db9c6e34b9326c527~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgZGVuZ2xvbmdmZWk=:q75.awebp?policy=eyJ2bSI6MywidWlkIjoiNDI2NTc2MDg0ODM1MjMxMSJ9&rk3s=f64ab15b&x-orig-authkey=f32326d3454f2ac7e96d3d06cdbb035152127018&x-orig-expires=1735147137&x-orig-sign=YTvNDqkptX7xg3QSsMgExMa9ycQ%3D)
![image.png](https://p0-xtjj-private.juejin.cn/tos-cn-i-73owjymdk6/a8f6748d622a497f97fcb9e8cbc4f464~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgZGVuZ2xvbmdmZWk=:q75.awebp?policy=eyJ2bSI6MywidWlkIjoiNDI2NTc2MDg0ODM1MjMxMSJ9&rk3s=f64ab15b&x-orig-authkey=f32326d3454f2ac7e96d3d06cdbb035152127018&x-orig-expires=1735147137&x-orig-sign=yNd2PdCGNl4Vww3Z6%2FikrOZXIGo%3D)

**aab**

![image.png](https://p0-xtjj-private.juejin.cn/tos-cn-i-73owjymdk6/6b76046efd5142b99c73d4ec9733dc3b~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgZGVuZ2xvbmdmZWk=:q75.awebp?policy=eyJ2bSI6MywidWlkIjoiNDI2NTc2MDg0ODM1MjMxMSJ9&rk3s=f64ab15b&x-orig-authkey=f32326d3454f2ac7e96d3d06cdbb035152127018&x-orig-expires=1735147137&x-orig-sign=KP6PkRLRLQ33lKVnyyxyJupw7lg%3D)

![image.png](https://p0-xtjj-private.juejin.cn/tos-cn-i-73owjymdk6/e1c071c8405e4343945b788f9701a2da~tplv-73owjymdk6-jj-mark-v1:0:0:0:0:5o6Y6YeR5oqA5pyv56S-5Yy6IEAgZGVuZ2xvbmdmZWk=:q75.awebp?policy=eyJ2bSI6MywidWlkIjoiNDI2NTc2MDg0ODM1MjMxMSJ9&rk3s=f64ab15b&x-orig-authkey=f32326d3454f2ac7e96d3d06cdbb035152127018&x-orig-expires=1735147137&x-orig-sign=VB4zebHz20AmavDH4VJ%2FtKuAkHQ%3D)

