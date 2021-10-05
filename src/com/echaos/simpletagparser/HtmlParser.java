package com.echaos.simpletagparser;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HtmlParser {

    public static class Tag {
        private Integer start;
        private Integer end;
        private Boolean isClosingTag;
        public HashMap<String, String> attributes;

        Tag() {

        }

        public Integer getStart() {
            return start;
        }

        public void setStart(Integer start) {
            this.start = start;
        }

        public Integer getEnd() {
            return end;
        }

        public void setEnd(Integer end) {
            this.end = end;
        }

        public Boolean getClosingTag() {
            return isClosingTag;
        }

        public void setClosingTag(Boolean closingTag) {
            isClosingTag = closingTag;
        }

        public void addAttribute(String attribute, String value) {
            if (this.attributes != null)
                this.attributes.put(attribute, value);
        }

        public String getAttribute(String attribute) {
            if (this.attributes != null)
                return this.attributes.get(attribute);
            return "";
        }

    }

    public static class TagBlock {
        private Tag openTag;
        private Tag closeTag;
        private boolean isUnpairedBlock;


        TagBlock() {
        }

        public boolean isUnpairedBlock() {
            return isUnpairedBlock;
        }

        public void setUnpairedBlock(boolean unpairedBlock) {
            isUnpairedBlock = unpairedBlock;
        }

        public Tag getOpenTag() {
            return openTag;
        }

        public void setOpenTag(Tag openTag) {
            this.openTag = openTag;
        }

        public Tag getCloseTag() {
            return closeTag;
        }

        public void setCloseTag(Tag closeTag) {
            this.closeTag = closeTag;
        }
    }

    public static class Node<T> {
        public T key = null;
        public Node<T> parent = null;
        public List<Node<T>> children;

        public Node() {
            children = new ArrayList<>();
        }

        public void setKey(T t) {
            key = t;
        }

        public void setParent(Node<T> parent) {
            this.parent = parent;
        }

        public void addChild(Node<T> child) {
            if (children != null) {
                child.setParent(this);
                children.add(child);
            }
        }
    }


    public static class TagBlockTreeGenerator {

        List<Tag> tagList;

        TagBlockTreeGenerator() {
            tagList = new ArrayList<>();
        }

        public Node<TagBlock> generate(String tagName, String content) {
            Node<TagBlock> base = new Node<>();
            tagList = findAllTagPositions(tagName, content);
            recursivelyGenerateBlockTree(base, 0, 0);

            return base;
        }

        public void recursivelyGenerateBlockTree(Node<TagBlock> base, int i, int unpaired) {
            if (i < tagList.size()) {
                // If the tag is an opening tag
                if (!tagList.get(i).isClosingTag) {

                    Node<TagBlock> child = new Node<>();
                    TagBlock tagBlock = new TagBlock();
                    tagBlock.setOpenTag(tagList.get(i));
                    child.setKey(tagBlock);
                    base.addChild(child);

                    recursivelyGenerateBlockTree(child, i + 1, unpaired + 1);
                } else {
                    if (unpaired > 0) {
                        base.key.setCloseTag(tagList.get(i));
                        recursivelyGenerateBlockTree(base.parent, i+1, unpaired -1);
                    } else if (unpaired == 0) {
                        Node<TagBlock> child = new Node<>();
                        child.key.setUnpairedBlock(true);
                        child.key.setCloseTag(tagList.get(i));
                        recursivelyGenerateBlockTree(child, i+1, unpaired -1);
                    }else {
                        Node<TagBlock> child = new Node<>();
                        child.key.setUnpairedBlock(true);
                        child.key.setCloseTag(tagList.get(i));
                        base.parent.addChild(child);
                    }
                }
            }
        }
    }

    public void print(String s) {
        System.out.println(s);
    }

    public String recursiveToString(Node<String> node, String tab) {
        if (node.key == null) {
            return "";
        } else {
            if (node.children == null) {
                return node.key + "\n";
            } else {
                StringBuilder children_text = new StringBuilder();
                for (Node<String> child : node.children) {
                    children_text.append(recursiveToString(child, tab + "  "));
                }
                return tab + node.key + "\n" + children_text.toString();
            }
        }
    }

    public static List<Tag> findAllTagPositions(String tagName, String content) {
        List<Tag> results = new ArrayList<>();
        Pattern forAllPattern = Pattern.compile(String.format("</\\s*?%s.*?>|<\\s*?%s.*?>", tagName, tagName));
        Pattern closingTagPattern = Pattern.compile(String.format("</\\s*?%s.*?>", tagName));

        Matcher matcher = forAllPattern.matcher(content);
        while (matcher.find()) {
            Tag tag = new Tag();
            tag.setStart(matcher.start());
            tag.setEnd(matcher.end());

            if (closingTagPattern.matcher(matcher.group()).find()) {
                tag.setClosingTag(true);
            } else {
                tag.setClosingTag(false);
                tag.attributes = hashMapFromTag(matcher.group());
            }

            results.add(tag);

        }

        return results;
    }


    public static HashMap<String, String> hashMapFromTag(String rawTag) {
        HashMap<String, String> tagHashMap = new HashMap<>();
        Matcher matcher = Pattern.compile("([\\w-]+)=\"([^\"]*)\"").matcher(rawTag);
        while (matcher.find()) {
            tagHashMap.put(matcher.group(1), matcher.group(2));
        }
        return tagHashMap;
    }

    public String sampleExpression = "[[[][][][]][][]][][][][][[][][][][]]";

    public void recursiveExpressionSolver(Node<String> base, int i, int Unpaired) {
        System.out.println(Unpaired + " unpaired left");
        if (i < sampleExpression.length()) {
            if (sampleExpression.charAt(i) == '[') {
                Node<String> child = new Node<>();
                child.key = i + ":";
                base.addChild(child);
                recursiveExpressionSolver(child, i + 1, Unpaired + 1);
            } else {
                if (Unpaired > 0) {
                    base.key += i;
                    recursiveExpressionSolver(base.parent, i + 1, Unpaired - 1);
                } else if (Unpaired == 0) {

                    Node<String> child = new Node<>();
                    child.key = "!:" + i;
                    base.addChild(child);
                    recursiveExpressionSolver(child, i + 1, Unpaired - 1);
                } else {
                    Node<String> child = new Node<>();
                    child.key = "!:" + i;
                    base.parent.addChild(child);
                }
            }
        }
    }

    public String sampleHTML = "";
}