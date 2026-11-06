package kz.aitu.se2523.visitor.model;

import kz.aitu.se2523.visitor.visitor.Visitor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class DirectoryNode implements Element {
    private final String name;
    private final List<Element> children = new ArrayList<>();
    private DirectoryNode parent;

    public DirectoryNode(String name) {
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Directory name cannot be null or empty");
        }
        this.name = name;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public DirectoryNode getParent() {
        return parent;
    }

    @Override
    public void setParent(DirectoryNode parent) {
        this.parent = parent;
    }

    public List<Element> getChildren() {
        return Collections.unmodifiableList(children);
    }

    public void add(Element child) {
        if (child == null) {
            throw new IllegalArgumentException("Child cannot be null");
        }
        for (Element existing : children) {
            if (existing.getName().equals(child.getName())) {
                throw new IllegalArgumentException("Duplicate sibling name: " + child.getName());
            }
        }
        if (child.getParent() != null && child.getParent() != this) {
            throw new IllegalStateException("Element already belongs to a parent directory");
        }
        DirectoryNode current = this;
        while (current != null) {
            if (current == child) {
                throw new IllegalStateException("Cycle detected: cannot add ancestor as child");
            }
            current = current.getParent();
        }
        children.add(child);
        child.setParent(this);
    }

    public boolean remove(Element child) {
        if (children.remove(child)) {
            child.setParent(null);
            return true;
        }
        return false;
    }

    @Override
    public void accept(Visitor visitor) {
        visitor.visit(this);
    }
}
