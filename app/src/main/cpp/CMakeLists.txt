# For more information about using CMake with Android Studio, read the
# documentation: https://d.android.com/studio/projects/add-native-code.html

# Sets the minimum version of CMake required to build the native library.

cmake_minimum_required(VERSION 3.22.1)

# Declares and names the project.

project("lame")
#音频渲染
set(OpenSL ${CMAKE_SOURCE_DIR}/opensl)
#视频渲染
set(OpenGL ${CMAKE_SOURCE_DIR}/gles)


include_directories(
        ${CMAKE_SOURCE_DIR}/include)
include_directories(
        ${CMAKE_SOURCE_DIR}/opensl)
include_directories(
        ${CMAKE_SOURCE_DIR}/gles)

add_library( # Sets the name of the library.
        lame
        # Sets the library as a shared library.
        SHARED
        # Provides a relative path to your source file(s).
        native-lib.cpp
        mp3_encoder.cpp
        OpenSLAudioPlay.cpp
        gles_play.cpp
        PlayCallback.cpp
        video_play.cpp
        )
add_library(mp3lame STATIC IMPORTED)
set_target_properties(mp3lame PROPERTIES IMPORTED_LOCATION
        ${CMAKE_SOURCE_DIR}/../jniLibs/${ANDROID_ABI}/libmp3lame.a)

find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)
# Specifies libraries CMake should link to your target library. You
# can link multiple libraries, such as libraries you define in this
# build script, prebuilt third-party libraries, or system libraries.

target_link_libraries( # Specifies the target library.
        lame
        mp3lame
        OpenSLES
        # OpenGL 与 NativeWindow 连接本地窗口的中间者
        EGL
        #视频渲染
        GLESv2
        android
        ${log-lib}
        )

