package com.baseApplication.base;


import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.Reader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;


public class XIOUtils {


    public static byte[] toByteArray(InputStream input) throws IOException{
        try ( ByteArrayOutputStream output = new ByteArrayOutputStream()){
            copy((InputStream)input, (OutputStream)output);
            return output.toByteArray();
        }
    }

    public static int copy(InputStream input, OutputStream output) throws IOException {
        long count = copyLarge(input, output);
        return count > 2147483647L ? -1 : (int)count;
    }

    public static long copyLarge(InputStream input, OutputStream output) throws IOException {
        return copy(input, output, 4096);
    }

    public static long copy(InputStream input, OutputStream output, int bufferSize) throws IOException {
        return copyLarge(input, output, new byte[bufferSize]);
    }

    public static long copyLarge(InputStream input, OutputStream output, byte[] buffer) throws IOException {
        long count;
        int n;
        for(count = 0L; -1 != (n = input.read(buffer)); count += (long)n) {
            output.write(buffer, 0, n);
        }

        return count;
    }

    public static String readFileLines(String file)throws IOException  {
        try(FileInputStream fis =  new FileInputStream(file)) {
            return readAllLines(fis, "utf-8");
        }
    }

    public static List<String> readLines(InputStream input)throws IOException  {
        return readLines(input,"utf-8");
    }

    public static String readAllLines(InputStream input)throws IOException  {
        return readAllLines(input,"utf-8");
    }

    public static String readAllLines(InputStream input, String encoding)throws IOException  {
        try( InputStreamReader reader = new InputStreamReader(input, encoding)) {
            return readAllLines((Reader)reader);
        }
    }

    public static List<String> readLines(InputStream input, String encoding) throws IOException {
        try(InputStreamReader reader = new InputStreamReader(input, encoding)) {
            return readLines((Reader)reader);
        }
    }

    public static String readAllLines(Reader input)throws IOException {
        try( BufferedReader reader = toBufferedReader(input)) {

            StringBuilder stringBuilder = new StringBuilder();
            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                stringBuilder.append(line).append("\n");
            }
            return stringBuilder.toString().trim();
        }
    }

    public static List<String> readLines(Reader input)throws IOException {
        try(BufferedReader reader = toBufferedReader(input)){
            List<String> list = new ArrayList();
            for(String line = reader.readLine(); line != null; line = reader.readLine()) {
                list.add(line);
            }
            return list;
        }
    }

    public static BufferedReader toBufferedReader(Reader reader) {
        return reader instanceof BufferedReader ? (BufferedReader)reader : new BufferedReader(reader);
    }

    public static void write(String s,String fileName)throws IOException{
        try(ByteArrayInputStream input = new ByteArrayInputStream(s.getBytes(StandardCharsets.UTF_8));
            FileOutputStream output    = new FileOutputStream(fileName,false)) {
            XIOUtils.copy(input, output);
        }
    }
}
