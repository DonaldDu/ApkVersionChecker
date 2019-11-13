cmake_minimum_required(VERSION 3.4.1)

# add_library()可添加多个，有多少个cpp文件可添加多少个
add_library(
        # 对应cpp的文件名
        ApkPatchLibrary

        # Sets the library as a shared library.
        SHARED

        # 对应cpp的文件名地址
        PatchUtils.c)


find_library( # Sets the name of the path variable.
        log-lib

        # Specifies the name of the NDK library that
        # you want CMake to locate.
        log)

#
target_link_libraries( # Specifies the target library.
        ApkPatchLibrary

        # Links the target library to the log library
        # included in the NDK.
        ${log-lib})