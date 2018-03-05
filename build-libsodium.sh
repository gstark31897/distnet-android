export ANDROID_NDK_HOME=$HOME/Android/Sdk/ndk-bundle

# set everything up
cd libsodium
./autogen.sh
./configure

# build everything
./dist-build/android-armv7-a.sh
./dist-build/android-armv8-a.sh
./dist-build/android-mips32.sh
./dist-build/android-mips64.sh
./dist-build/android-x86.sh
./dist-build/android-x86_64.sh
