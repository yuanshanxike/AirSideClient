#!/bin/bash
CPU=arm
PREFIX=$(pwd)/android/$CPU
ADDI_CFLAGS=""
ADDI_LDFLAGS=""

function build_arm
{
./configure \
    --prefix=$PREFIX \
    --enable-shared \
    --enable-static \
    --disable-gpac \
    --disable-cli \
    --host=arm-linux-androideabi \
    --cross-prefix=$TOOLCHAIN/bin/arm-linux-androideabi- \
    --sysroot=$SYSROOT \
    --extra-cflags="-Os -fpic $ADDI_CFLAGS" \
    --extra-ldflags="$ADDI_LDFLAGS" \
    $ADDITIONAL_CONFIGURE_FLAG
make clean
make
make install
}

build_arm
