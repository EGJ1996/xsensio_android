package com.xsensio.nfcsensorcomm.files;

import android.content.Context;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.nio.ByteBuffer;
import java.nio.DoubleBuffer;
import java.nio.IntBuffer;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FileManager {

    //BILGUUN
    List<Byte> filedata=new ArrayList<>();
    int fileIntA;
    int fileIntB;
    double fileDouble;

    private void savefile(Context context, String name, List<Byte> data, int a, int b, double c){
        FileOutputStream outputStream;
        // Converting Whole data into array of bytes
        // First 16 bytes are for saving a,b and c numbers
        int[] rest = {a,b};
        ByteBuffer byteBuffer = ByteBuffer.allocate(8);
        IntBuffer intBuffer = byteBuffer.asIntBuffer();
        intBuffer.put(rest);
        byte[] ints=byteBuffer.array();

        ByteBuffer byteBuffer1 = ByteBuffer.allocate(8);
        DoubleBuffer doubleBuffer = byteBuffer1.asDoubleBuffer();
        doubleBuffer.put(c);
        byte[] doubles=byteBuffer.array();

        byte[] lolbytes=new byte[data.size()+16];

        for(int i=0;i<8;i++){ lolbytes[i]=ints[i]; }
        for(int i=0;i<8;i++){ lolbytes[i+8]=doubles[i]; }
        for(int i=0;i<data.size();i++){ lolbytes[i+16]=data.get(i); }

        try {
            outputStream = context.openFileOutput(name, Context.MODE_PRIVATE);
            outputStream.write(lolbytes);
            outputStream.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    private void readfile(Context context,String name){
        File file = new File(context.getFilesDir(), name);
        FileInputStream inputStream;
        String[] sexybacks=context.fileList();
        int size = (int) file.length();
        byte bytes[] = new byte[size];
        byte tmpBuff[] = new byte[size];
        try {
            inputStream=new FileInputStream(file);
            int read = inputStream.read(bytes, 0, size);
            if (read < size) {
                int remain = size - read;
                while (remain > 0) {
                    read = inputStream.read(tmpBuff, 0, remain);
                    System.arraycopy(tmpBuff, 0, bytes, size - remain, read);
                    remain -= read;
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
        }

        fileIntA=(ByteBuffer.wrap(Arrays.copyOfRange(bytes,0,4))).getInt();
        fileIntB=(ByteBuffer.wrap(Arrays.copyOfRange(bytes,4,8))).getInt();
        fileDouble=(ByteBuffer.wrap(Arrays.copyOfRange(bytes,8,16))).getDouble();
        for(int i=16;i<bytes.length;i++){
            filedata.add(bytes[i]);
        }
    }
    //BILGUUN
}
