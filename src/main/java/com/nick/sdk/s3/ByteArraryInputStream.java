package com.nick.sdk.s3;
import java.io.*;

public class ByteArraryInputStream {
    public static void main(String[] args) throws IOException {
        byte[] data = { 72, 101, 108, 108, 111, 33 };
        try (InputStream input = new ByteArrayInputStream(data)) {
            int n;
            while ((n = input.read()) != -1) {

                System.out.println((char)1);
            }
        }
    }
}
