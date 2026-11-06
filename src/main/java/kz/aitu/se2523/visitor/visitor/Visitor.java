package kz.aitu.se2523.visitor.visitor;

import kz.aitu.se2523.visitor.model.DirectoryNode;
import kz.aitu.se2523.visitor.model.FileNode;

public interface Visitor {
    void visit(FileNode file);
    void visit(DirectoryNode directory);
}
