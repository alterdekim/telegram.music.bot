package com.alterdekim.telegram.storage;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

public class TreeNode<E> {

    private E key;
    private List<TreeNode<E>> children;
    private TreeNode<E> parent;

    public TreeNode( E key ) {
        this.key = key;
        this.children = new LinkedList<TreeNode<E>>();
    }

    public void setParent( TreeNode<E> n ) {
        this.parent = n;
    }

    public E getKey() {
        return this.key;
    }

    public int size() {
        return this.children.size();
    }

    public int recursiveSize() {
        int s = this.children.size();
        for( TreeNode<E> n : this.children ) {
            s += n.recursiveSize();
        }
        return s;
    }

    private ArrayList<E> getNonEndPoints( TreeNode<E> node ) {
        ArrayList<E> ends = new ArrayList<E>();
        for( TreeNode<E> n : node.children ) {
            if( n.size() > 0 ) {
                ends.add(n.getKey());
                ends.addAll(getNonEndPoints(n));
            }
        }
        return ends;
    }

    public ArrayList<E> getNonEndPoints( boolean ignoreFirst ) {
        ArrayList<E> ends = new ArrayList<E>();
        for( TreeNode<E> n : this.children ) {
            if( n.size() > 0 ) {
                if( !ignoreFirst ) {
                    ends.add(n.getKey());
                }
                ends.addAll(getNonEndPoints(n));
            }
        }
        return ends;
    }

    private ArrayList<E> getEndPoints( TreeNode<E> node ) {
        ArrayList<E> ends = new ArrayList<E>();
        for( TreeNode<E> n : node.children ) {
            if( n.size() == 0 ) {
                ends.add(n.getKey());
            } else {
                ends.addAll(getEndPoints(n));
            }
        }
        return ends;
    }

    public ArrayList<E> getEndPoints() {
        ArrayList<E> ends = new ArrayList<E>();
        for( TreeNode<E> n : this.children ) {
            if( n.size() == 0 ) {
                ends.add(n.getKey());
            } else {
                ends.addAll(getEndPoints(n));
            }
        }
        return ends;
    }


    public boolean isEmpty() {
        return this.children.size() == 0;
    }

    public boolean containsKey(Object o) {
        for( TreeNode<E> node : children ) {
            if( node.getKey().equals(o) ) {
                return true;
            }
        }
        return false;
    }

    public TreeNode<E> addChild(TreeNode<E> childNode) {
        childNode.setParent((TreeNode<E>) this);
        this.children.add(childNode);
        return childNode;
    }

    public TreeNode<E> getChild(int index) {
        return this.children.get(index);
    }

    public void remove( int index ) {
        this.children.remove(index);
    }

    public void clear() {
        this.children.clear();
    }

    private String recursivePrint( String s, TreeNode<E> n, String prefix ) {
        for( int i = 0; i < n.size(); i++ ) {
            s += prefix + n.getChild(i).getKey().toString() + "\r\n";
            s = recursivePrint(s, n.getChild(i), prefix + n.getChild(i).getKey().toString() + " -> ");
        }
        return s;
    }

    public String printAll() {
        return TreeNode.class.toString() + " key = " + this.getKey().toString() + "\r\n" + recursivePrint("", this, this.getKey() + " -> ");
    }
}