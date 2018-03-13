# [libx264](https://www.videolan.org/developers/x264.html) 和 [librtmp](https://github.com/ossrs/librtmp)的编译脚本

|  | |
| :-----: | :------------------------: |
| os：      | ubuntu 16.04 LTS (64bit) |
| ndk:      | [android-ndk-r10e](https://developer.android.com/ndk/downloads/index.html?hl=zh-cn) |
| arch:     | arm |
| platform: | API 21 (Android 5.0) |

#### .bashrc中配置的环境变量（需根据自己的NDK路径进行配置）:

```shell
# GCC 找到头文件的路径(ndk arm)
INCLUDE=/home/lewis/android-ndk-r10e/platforms/android-21/arch-arm/usr/include
C_INCLUDE_PATH=$INCLUDE
export C_INCLUDE_PATH
```

```shell
# ndk environment variables
export NDK=/home/lewis/android-ndk-r10e
export SYSROOT=$NDK/platforms/android-21/arch-arm/  #arm
export TOOLCHAIN=$NDK/toolchains/arm-linux-androideabi-4.9/prebuilt/linux-x86_64
```

>libx264的工程中自带**configure**文件，所以只需要在工程目录下编写一个编译配置脚本，执行即可

>librtmp工程包含着Makefile文件，不过在这里我是通过 [NDK Building](https://developer.android.com/ndk/guides/build.html) 的
方式进行构建的，参考文章--[简述RTMPDump与编译移植](http://zhengxiaoyong.me/2016/11/20/%E7%AE%80%E8%BF%B0RTMPDump%E4%B8%8E%E7%BC%96%E8%AF%91%E7%A7%BB%E6%A4%8D/)