package com.echaos.simpletagparser;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;
import java.util.regex.Matcher;
import java.util.regex.Pattern;


public class HtmlParser {

    public static Node<TagBlock> findFirstTagBlocksByName(Node<TagBlock> base, String tagName)
    {
        AtomicReference<Node<TagBlock>> result = new AtomicReference<>();
        AtomicBoolean occurred = new AtomicBoolean(false);
        recursiveNodeOperation(base, (n)->{
            if (!occurred.get() && n.key != null && n.key.openTag != null && n.key.openTag.tagName.equals(tagName))
            {
                    result.set(n);
                    occurred.set(false);
            }
        });

        return result.get();
    }

    public static List<Tag> findAllChildTagsByName(Node<TagBlock> parent, String tagName) {
        List<Tag> tagList = new ArrayList<>();
        recursiveNodeOperation(parent, (n)->{
            if (n.key != null && n.key.openTag != null && n.key.openTag.tagName.equals(tagName))
            {
                tagList.add(n.key.openTag);
            }
        });
        return tagList;
    }

    public static List<Tag> findAllTagsByName(String content, String tagName) {
        List<Tag> results = new ArrayList<>();
        Pattern forAllPattern = Pattern.compile(String.format("</\\s*?%s.*?>|<\\s*?%s.*?>", tagName, tagName));
        Pattern closingTagPattern = Pattern.compile(String.format("</\\s*?%s.*?>", tagName));

        Matcher matcher = forAllPattern.matcher(content);
        while (matcher.find()) {
            Tag tag = new Tag();
            tag.setTagName(tagName);
            tag.setStart(matcher.start());
            tag.setEnd(matcher.end());

            if (closingTagPattern.matcher(matcher.group()).find()) {
                tag.setTagType(Tag.TagType.CLOSE);
            } else {
                tag.setTagType(Tag.TagType.OPEN);
                tag.attributes = hashMapFromTag(matcher.group());
            }

            results.add(tag);

        }

        return results;
    }

    @SafeVarargs
    public static List<Tag> findAllTagWithAttributeName(String content, String tagName, Pair<String, String>... specificAttributePairs) {
        List<Tag> results = new ArrayList<>();
        if (specificAttributePairs != null) {
            for (Tag tag : findAllTagsByName(content, tagName)) {
                for (Pair<String, String> pair : specificAttributePairs) {
                    if (tag.containAttribute(pair.first)) {
                        if (tag.getAttribute(pair.first).equals(pair.second)) {
                            results.add(tag);
                            break;
                        }
                    }
                }
            }
        }
        return results;
    }

    @SafeVarargs
    public static List<Tag> findAllTagWithAttributeValuePairs(String content, String tagName, Pair<String, String>... attributesValuePair) {
        List<Tag> results = new ArrayList<>();
        if (attributesValuePair != null) {
            for (Tag tag : findAllTagsByName(content, tagName)) {
                boolean containAllAttributeValuePairs = true;
                for (Pair<String, String> pair : attributesValuePair) {
                    if (tag.containAttribute(pair.first)) {
                        containAllAttributeValuePairs &= (tag.getAttribute(pair.first).equals(pair.second));

                    }
                }


                if (containAllAttributeValuePairs)
                    results.add(tag);
            }
        }
        return results;
    }

    public static List<Tag> findAllTags(String content) {
        List<Tag> results = new ArrayList<>();

        Set<String> selfClosingTagNames = new HashSet<String>() {{
            add("br");
            add("img");
            add("link");
            add("meta");
            add("embed");
        }};

        Pattern forAllPattern = Pattern.compile("<\\s*?/?\\s*?(\\w+).*?>");
        Pattern closingTagPattern = Pattern.compile("</\\w*?.*?>");

        Matcher matcher = forAllPattern.matcher(content);
        while (matcher.find()) {
            Tag tag = new Tag();
            tag.setStart(matcher.start());
            tag.setEnd(matcher.end());
            tag.setTagName(matcher.group(1));

            if (selfClosingTagNames.contains(matcher.group(1))) {
                tag.setTagType(Tag.TagType.SELF_CLOSE);
                tag.attributes = hashMapFromTag(matcher.group());
            } else {
                if (closingTagPattern.matcher(matcher.group()).find()) {
                    tag.setTagType(Tag.TagType.CLOSE);
                } else {
                    tag.setTagType(Tag.TagType.OPEN);
                    tag.attributes = hashMapFromTag(matcher.group());
                }
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

    public static void recursiveNodeOperation(Node<TagBlock> node, INodeOperation<TagBlock> operation) {
        operation.run(node);
        if (node.children != null) {
            for (Node<TagBlock> child : node.children) {
                recursiveNodeOperation(child, operation);
            }
        }
    }

    public interface INodeOperation<T> {
        void run(Node<T> node);
    }

    public static class Tag {
        public HashMap<String, String> attributes;
        private Integer start;
        private Integer end;
        private String tagName;
        private Boolean isNonClosingTag;

        public TagType getTagType() {
            return tagType;
        }

        public void setTagType(TagType tagType) {
            this.tagType = tagType;
        }

        public enum TagType {
            OPEN,
            CLOSE,
            SELF_CLOSE,
        }

        ;

        private TagType tagType;

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

        public void addAttribute(String attribute, String value) {
            if (this.attributes != null)
                this.attributes.put(attribute, value);
        }

        public String getAttribute(String attribute) {
            String result = "";
            if (this.attributes != null) {
                result = this.attributes.get(attribute);
                return result == null ? "" : result;
            }
            return "";
        }

        public boolean containAttribute(String attribute) {
            if (this.attributes != null)
                return this.attributes.containsKey(attribute);
            return false;
        }

        public String getTagName() {
            return tagName;
        }

        public void setTagName(String tagName) {
            this.tagName = tagName;
        }

        public Boolean getNonClosingTag() {
            return isNonClosingTag;
        }

        public void setNonClosingTag(Boolean nonClosingTag) {
            isNonClosingTag = nonClosingTag;
        }
    }

    public static class TagBlock {
        private Tag openTag;
        private Tag closeTag;
        private boolean isUnpairedBlock;
        private boolean isSelfClosingBlock;


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

        public boolean isSelfClosingBlock() {
            return isSelfClosingBlock;
        }

        public void setSelfClosingBlock(boolean selfClosingBlock) {
            isSelfClosingBlock = selfClosingBlock;
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

        public Node<TagBlock> generate(String content) {
            Node<TagBlock> base = new Node<>();
            tagList = findAllTags(content);
            recursivelyGenerateBlockTree(base, 0, 0);

            return base;
        }

        public Node<TagBlock> generate(String content, String baseTagName)
        {

            Node<TagBlock> parent = generate(content);
            return findFirstTagBlocksByName(parent, baseTagName);
        }

        public void recursivelyGenerateBlockTree(Node<TagBlock> base, int i, int unpaired) {
            if (i < tagList.size()) {
                // If the tag is an opening tag
                switch (tagList.get(i).tagType) {
                    case OPEN -> {
                        //Open tag
                        Node<TagBlock> child = new Node<>();
                        TagBlock tagBlock = new TagBlock();
                        tagBlock.setOpenTag(tagList.get(i));
                        tagBlock.setSelfClosingBlock(false);
                        child.setKey(tagBlock);
                        base.addChild(child);

                        recursivelyGenerateBlockTree(child, i + 1, unpaired + 1);
                    }
                    case CLOSE -> {
                        //Closing Tag
                        if (unpaired > 0) {
                            base.key.setCloseTag(tagList.get(i));
                            recursivelyGenerateBlockTree(base.parent, i + 1, unpaired - 1);
                        } else if (unpaired == 0) {
                            Node<TagBlock> child = new Node<>();
                            child.key.setUnpairedBlock(true);
                            child.key.setCloseTag(tagList.get(i));
                            recursivelyGenerateBlockTree(child, i + 1, unpaired - 1);
                        } else {
                            Node<TagBlock> child = new Node<>();
                            child.key.setUnpairedBlock(true);
                            child.key.setCloseTag(tagList.get(i));
                            base.parent.addChild(child);
                        }
                    }
                    case SELF_CLOSE -> {
                        Node<TagBlock> child = new Node<>();
                        TagBlock tagBlock = new TagBlock();
                        tagBlock.setSelfClosingBlock(true);
                        tagBlock.setOpenTag(tagList.get(i));
                        tagBlock.setCloseTag(tagBlock.getOpenTag());
                        child.setKey(tagBlock);
                        base.addChild(child);
                        recursivelyGenerateBlockTree(base, i + 1, unpaired);
                    }
                }
            }
        }
    }

}