package kz.aitu.se2523.visitor.model;

import kz.aitu.se2523.visitor.visitor.Visitor;

public interface Element {
    String getName();
    DirectoryNode getParent();
    void setParent(DirectoryNode parent);
    void accept(Visitor visitor);
}
