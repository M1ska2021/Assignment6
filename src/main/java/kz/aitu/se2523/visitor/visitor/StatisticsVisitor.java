package kz.aitu.se2523.visitor.visitor;

import kz.aitu.se2523.visitor.model.DirectoryNode;
import kz.aitu.se2523.visitor.model.Element;
import kz.aitu.se2523.visitor.model.FileNode;

public class StatisticsVisitor implements Visitor {
    private int fileCount = 0;
    private int directoryCount = 0;
    private int maxDepth = 0;
    private int currentDepth = 0;
    private int activeDepth = 0;

    public int getFileCount() {
        return fileCount;
    }

    public int getDirectoryCount() {
        return directoryCount;
    }

    public int getMaxDepth() {
        return maxDepth;
    }

    public void reset() {
        fileCount = 0;
        directoryCount = 0;
        maxDepth = 0;
        currentDepth = 0;
    }

    @Override
    public void visit(FileNode file) {
        if (activeDepth == 0) {
            reset();
        }
        fileCount++;
        if (currentDepth > maxDepth) {
            maxDepth = currentDepth;
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
        if (activeDepth == 0) {
            reset();
        }
        activeDepth++;
        try {
            directoryCount++;
            if (currentDepth > maxDepth) {
                maxDepth = currentDepth;
            }
            for (Element child : directory.getChildren()) {
                currentDepth++;
                child.accept(this);
                currentDepth--;
            }
        } finally {
            activeDepth--;
        }
    }
}
