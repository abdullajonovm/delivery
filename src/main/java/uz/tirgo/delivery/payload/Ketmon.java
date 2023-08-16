package uz.tirgo.delivery.payload;

import java.util.HashMap;

public class Ketmon {
    public static void main(String[] args) {
        String mainString = "Hello/19";
        String stringToRemove = "";

        String result = mainString.replace("Hello/","");

        System.out.println(result); // "Hello, !"
    }
}
