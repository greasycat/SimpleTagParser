package com.echaos.simpletagparser;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.Scanner;
import com.echaos.simpletagparser.HtmlParser;
import com.echaos.simpletagparser.HtmlParser.*;

public class Main {

    public static String readFromFile(String path) {
        StringBuilder result = new StringBuilder();
        try {
            File myObj = new File(path);
            Scanner myReader = new Scanner(myObj);
            while (myReader.hasNextLine()) {
                String data = myReader.nextLine();
                result.append(data);
                result.append("\n");
            }
        } catch (IOException ioException) {
            ioException.printStackTrace();
        }
        return result.toString();
    }

    public static void main(String[] args) {
        long start = System.nanoTime();
        //----------------------------------
        String html = readFromFile("D://sample.html");
        HtmlParser.TagBlockTreeGenerator tagBlockTreeGenerator = new HtmlParser.TagBlockTreeGenerator();

        List<Tag> tags = HtmlParser.findAllChildTagsByName(tagBlockTreeGenerator.generate(html, "form"), "input");

        //----------------------------------
        long end = System.nanoTime();
        System.out.println((end - start) / 1000000);

    }

}
