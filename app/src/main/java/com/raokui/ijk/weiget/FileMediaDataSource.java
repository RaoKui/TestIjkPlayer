package com.raokui.ijk.weiget;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

import tv.danmaku.ijk.media.player.misc.IMediaDataSource;

/**
 * 本地文件媒体数据源对象
 * Created by 饶魁 on 2017/9/21.
 */

public class FileMediaDataSource implements IMediaDataSource {


    private RandomAccessFile mFile;

    private long file_size;

    public FileMediaDataSource(File file) throws IOException {
        mFile = new RandomAccessFile(file, "r");
        file_size = mFile.length();
    }

    @Override
    public int readAt(long position, byte[] buffer, int offset, int size) throws IOException {
        if (mFile.getFilePointer() != position) {
            mFile.seek(position);
        }

        if (size == 0) {
            return 0;
        }

        return mFile.read(buffer, 0, size);
    }

    @Override
    public long getSize() throws IOException {
        return file_size;
    }

    @Override
    public void close() throws IOException {
        file_size = 0;
        mFile.close();
        mFile = null;
    }
}
