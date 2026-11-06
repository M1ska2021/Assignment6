package kz.aitu.se2523.visitor.visitor;

import kz.aitu.se2523.visitor.model.DirectoryNode;
import kz.aitu.se2523.visitor.model.Element;
import kz.aitu.se2523.visitor.model.FileNode;

public class TotalSizeVisitor implements Visitor {
    private long totalSize = 0;
    private int activeDepth = 0;

    public long getTotalSize() {
        return totalSize;
    }

    public void reset() {
        totalSize = 0;
    }

    @Override
    public void visit(FileNode file) {
        if (activeDepth == 0) {
            reset();
        }
        totalSize += file.getSize();
    }

    @Override
    public void visit(DirectoryNode directory) {
        if (activeDepth == 0) {
            reset();
        }
        activeDepth++;
        try {
            for (Element child : directory.getChildren()) {
                child.accept(this);
            }
        } finally {
            activeDepth--;
        }
    }
}
