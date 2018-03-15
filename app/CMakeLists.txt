cmake_minimum_required(VERSION 3.4.1)

include_directories(src/main/cpp/include/)
include_directories(../libsodium/src/libsodium/include/)

link_directories(src/main/jniLibs/${ANDROID_ABI}/)

add_library(distnet-core SHARED
            src/main/cpp/src/jni.cpp src/main/cpp/src/node.cpp src/main/cpp/src/socket.cpp src/main/cpp/src/keypair.cpp)

find_library(log-lib log)

target_link_libraries(distnet-core ${log-lib} sodium)