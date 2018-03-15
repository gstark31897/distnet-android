export ANDROID_NDK_HOME=$HOME/Android/Sdk/ndk-bundle

# set everything up
cd libsodium
./autogen.sh
./configure

# build everything
./dist-build/android-armv7-a.sh
./dist-build/android-armv8-a.sh
./dist-build/android-x86.sh
./dist-build/android-x86_64.sh

# make directories
cd ../
mkdir app/src/main/jniLibs/x86_64
mkdir app/src/main/jniLibs/x86   
mkdir app/src/main/jniLibs/armeabi-v7a
mkdir app/src/main/jniLibs/arm64-v8a  

# copy the endstates
cp libsodium/libsodium-android-westmere/lib/libsodium.so app/src/main/jniLibs/x86_64/
cp libsodium/libsodium-android-i686/lib/libsodium.so     app/src/main/jniLibs/x86/
cp libsodium/libsodium-android-armv8-a/lib/libsodium.so  app/src/main/jniLibs/arm64-v8a/
cp libsodium/libsodium-android-armv7-a/lib/libsodium.so  app/src/main/jniLibs/armeabi-v7a/
