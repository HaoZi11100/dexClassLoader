package com.cocos.test3;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

public class main {

    public static void main(String[] args) {
        enctor();
    }

    public static void enctor(){

        String inPath = "F:\\cd\\xx\\MatchingtheGreatAdventure\\unityLibrary\\src\\main\\assets\\b.apk";
        String outPath = "F:\\cd\\xx\\MatchingtheGreatAdventure\\unityLibrary\\src\\main\\assets\\apcs0241335.bin";
        try (FileInputStream encodeApkPathInAssets = new FileInputStream(inPath)) {
            try (FileOutputStream baseApk = new FileOutputStream(outPath, false)) {
                String password = "3A0C92622CF056B908E22385675EE46A";
                byte[] buffer = new byte[1024];
                int read;

                while ((read = encodeApkPathInAssets.read(buffer)) != -1) {
                    baseApk.write(encrypt(buffer, password.getBytes(StandardCharsets.UTF_8)), 0, read);
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        } catch (IOException e) {
            // Handle or log the exception appropriately
        }

    }

    public static byte[] encrypt(byte[] data, byte[] key) {
        if (data == null || data.length == 0 || key == null || key.length == 0) {
            return data;
        }

        byte[] result = new byte[data.length];
        for (int i = 0; i < data.length; i++) {
            result[i] = (byte) (data[i] ^ key[i % key.length] ^ (i & 0xFF));
        }

        return result;
    }

}
