package com.alterdekim.telegram.xml;

import org.simpleframework.xml.strategy.Type;
import org.simpleframework.xml.strategy.Visitor;
import org.simpleframework.xml.stream.InputNode;
import org.simpleframework.xml.stream.NodeMap;
import org.simpleframework.xml.stream.OutputNode;

public class CommentVisitor implements Visitor {
    public void read(Type type, NodeMap<InputNode> node) throws Exception {}
    public void write(Type type, NodeMap<OutputNode> node) throws Exception {
        if(!node.getNode().isRoot()) {
            Comment comment = type.getAnnotation(Comment.class);
            if(comment != null) {
                node.getNode().setComment(comment.value());
            }
        }
    }
}