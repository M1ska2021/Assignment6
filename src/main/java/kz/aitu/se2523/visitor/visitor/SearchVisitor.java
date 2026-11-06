package kz.aitu.se2523.visitor.visitor;

import kz.aitu.se2523.visitor.model.DirectoryNode;
import kz.aitu.se2523.visitor.model.Element;
import kz.aitu.se2523.visitor.model.FileNode;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.List;
import java.util.function.Predicate;

public class SearchVisitor implements Visitor {
    private final Predicate<String> namePredicate;
    private final long minSize;
    private final List<String> matches = new ArrayList<>();
    private final Deque<String> currentPathSegments = new ArrayDeque<>();
    private int activeDepth = 0;

    public SearchVisitor(String namePattern, long minSize) {
        this(name -> namePattern == null || namePattern.isEmpty() || name.contains(namePattern), minSize);
    }

    public SearchVisitor(Predicate<String> namePredicate, long minSize) {
        if (namePredicate == null) {
            throw new IllegalArgumentException("Predicate cannot be null");
        }
        if (minSize < 0) {
            throw new IllegalArgumentException("Minimum size cannot be negative: " + minSize);
        }
        this.namePredicate = namePredicate;
        this.minSize = minSize;
    }

    public List<String> getMatches() {
        List<String> sorted = new ArrayList<>(matches);
        Collections.sort(sorted);
        return Collections.unmodifiableList(sorted);
    }

    public void reset() {
        matches.clear();
        currentPathSegments.clear();
    }

    @Override
    public void visit(FileNode file) {
        if (activeDepth == 0) {
            reset();
        }
        boolean nameMatches = namePredicate.test(file.getName());
        boolean sizeMatches = file.getSize() >= minSize;
        if (nameMatches && sizeMatches) {
            if (currentPathSegments.isEmpty()) {
                matches.add(file.getName());
            } else {
                matches.add(String.join("/", currentPathSegments));
            }
        }
    }

    @Override
    public void visit(DirectoryNode directory) {
        if (activeDepth == 0) {
            reset();
        }
        activeDepth++;
        try {
            for (Element child : directory.getChildren()) {
                currentPathSegments.addLast(child.getName());
                child.accept(this);
                currentPathSegments.removeLast();
            }
        } finally {
            activeDepth--;
        }
    }
}
