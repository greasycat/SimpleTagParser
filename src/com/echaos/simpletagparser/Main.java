package com.echaos.simpletagparser;

public class Main {

    public static void main(String[] args) {
        long start = System.nanoTime();

        HtmlParser htmlParser = new HtmlParser();
        HtmlParser.Node<HtmlParser.TagBlock> all_div = new HtmlParser.TagBlockTreeGenerator().generate("div", htmlParser.sampleHTML);

        long end = System.nanoTime();
        System.out.println((end-start)/1000000);

    }
}
