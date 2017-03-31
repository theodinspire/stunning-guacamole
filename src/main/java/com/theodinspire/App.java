package com.theodinspire;

import java.io.File;

/**
 * Hello world!
 *
 */
public class App {
    public static void main( String[] args ) {
        String test = "world-building'";
        System.out.println(test);
        
        String next = test.replaceAll("(?<=\\w)'(?!\\w)", " doop ");
        System.out.println(next);
    }
}
