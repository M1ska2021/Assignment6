package kz.aitu.se2523.visitor;

import kz.aitu.se2523.visitor.model.DirectoryNode;
import kz.aitu.se2523.visitor.model.FileNode;
import kz.aitu.se2523.visitor.visitor.SearchVisitor;
import kz.aitu.se2523.visitor.visitor.StatisticsVisitor;
import kz.aitu.se2523.visitor.visitor.TotalSizeVisitor;
import kz.aitu.se2523.visitor.visitor.Visitor;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class FileSystemVisitorTest {

    @Test
    public void t1_bothElementTypesReachCorrectOverloadedVisitMethods() {
        AtomicBoolean fileVisited = new AtomicBoolean(false);
        AtomicBoolean directoryVisited = new AtomicBoolean(false);

        Visitor testVisitor = new Visitor() {
            @Override
            public void visit(FileNode file) {
                fileVisited.set(true);
            }

            @Override
            public void visit(DirectoryNode directory) {
                directoryVisited.set(true);
            }
        };

        FileNode file = new FileNode("sample.txt", 100);
        DirectoryNode dir = new DirectoryNode("folder");

        file.accept(testVisitor);
        dir.accept(testVisitor);

        assertTrue(fileVisited.get());
        assertTrue(directoryVisited.get());
    }

    @Test
    public void t2_sizeAccumulationAcrossMultipleNestingLevelsIsCorrect() {
        DirectoryNode root = new DirectoryNode("root");
        root.add(new FileNode("root.txt", 500));

        DirectoryNode sub1 = new DirectoryNode("sub1");
        sub1.add(new FileNode("sub1_a.txt", 1500));
        sub1.add(new FileNode("sub1_b.txt", 2000));

        DirectoryNode sub2 = new DirectoryNode("sub2");
        sub2.add(new FileNode("deep.bin", 3000));
        sub1.add(sub2);
        root.add(sub1);

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);

        assertEquals(7000L, sizeVisitor.getTotalSize());
    }

    @Test
    public void t3_searchCorrectlyCombinesBothConfigurableFilterConditions() {
        DirectoryNode root = new DirectoryNode("project");
        root.add(new FileNode("test_unit.java", 1200));
        root.add(new FileNode("test_integration.java", 800));
        root.add(new FileNode("data.json", 1500));
        root.add(new FileNode("notes.txt", 300));

        SearchVisitor searchVisitor = new SearchVisitor(".java", 1000);
        root.accept(searchVisitor);

        List<String> results = searchVisitor.getMatches();
        assertEquals(1, results.size());
        assertEquals("test_unit.java", results.get(0));
    }

    @Test
    public void t4_searchProducesCorrectDeterministicRootRelativePaths() {
        DirectoryNode root = new DirectoryNode("root");
        root.add(new FileNode("b_file.log", 100));

        DirectoryNode aDir = new DirectoryNode("a_dir");
        aDir.add(new FileNode("z_file.log", 100));

        DirectoryNode bSub = new DirectoryNode("b_sub");
        bSub.add(new FileNode("c_file.log", 100));

        aDir.add(bSub);
        root.add(aDir);

        SearchVisitor searchVisitor = new SearchVisitor(".log", 50);
        root.accept(searchVisitor);

        List<String> matches = searchVisitor.getMatches();
        assertEquals(3, matches.size());
        assertEquals("a_dir/b_sub/c_file.log", matches.get(0));
        assertEquals("a_dir/z_file.log", matches.get(1));
        assertEquals("b_file.log", matches.get(2));
    }

    @Test
    public void t5_statisticsAreCorrectForNestingCountingAndMaximumDepth() {
        DirectoryNode root = new DirectoryNode("root");
        DirectoryNode dir1 = new DirectoryNode("dir1");
        DirectoryNode dir2 = new DirectoryNode("dir2");

        dir2.add(new FileNode("fileA.txt", 10));
        dir1.add(dir2);
        dir1.add(new FileNode("fileB.txt", 20));
        root.add(dir1);
        root.add(new FileNode("fileC.txt", 30));

        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        root.accept(statsVisitor);

        assertEquals(3, statsVisitor.getFileCount());
        assertEquals(3, statsVisitor.getDirectoryCount());
        assertEquals(3, statsVisitor.getMaxDepth());
    }

    @Test
    public void t6_emptyStructuresAndNoMatchSearchCasesAreHandled() {
        DirectoryNode emptyRoot = new DirectoryNode("empty");

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        emptyRoot.accept(sizeVisitor);
        assertEquals(0L, sizeVisitor.getTotalSize());

        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        emptyRoot.accept(statsVisitor);
        assertEquals(0, statsVisitor.getFileCount());
        assertEquals(1, statsVisitor.getDirectoryCount());
        assertEquals(0, statsVisitor.getMaxDepth());

        SearchVisitor searchVisitor = new SearchVisitor("missing.txt", 100);
        emptyRoot.accept(searchVisitor);
        assertTrue(searchVisitor.getMatches().isEmpty());
    }

    @Test
    public void t7_repeatExecutionsDoNotLeakOrMultiplyAccumulatedState() {
        DirectoryNode root = new DirectoryNode("folder");
        root.add(new FileNode("a.txt", 250));
        root.add(new FileNode("b.txt", 350));

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);
        assertEquals(600L, sizeVisitor.getTotalSize());

        root.accept(sizeVisitor);
        assertEquals(600L, sizeVisitor.getTotalSize());

        sizeVisitor.reset();
        root.accept(sizeVisitor);
        assertEquals(600L, sizeVisitor.getTotalSize());

        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        root.accept(statsVisitor);
        assertEquals(2, statsVisitor.getFileCount());
        assertEquals(1, statsVisitor.getDirectoryCount());

        root.accept(statsVisitor);
        assertEquals(2, statsVisitor.getFileCount());
        assertEquals(1, statsVisitor.getDirectoryCount());

        SearchVisitor searchVisitor = new SearchVisitor(".txt", 100);
        root.accept(searchVisitor);
        assertEquals(2, searchVisitor.getMatches().size());

        root.accept(searchVisitor);
        assertEquals(2, searchVisitor.getMatches().size());
    }

    @Test
    public void t8_invalidTreeMutationsAreRejectedWhileValidNestingRemainsSupported() {
        assertThrows(IllegalArgumentException.class, () -> new FileNode("invalid.bin", -1));
        assertThrows(IllegalArgumentException.class, () -> new FileNode(null, 10));
        assertThrows(IllegalArgumentException.class, () -> new DirectoryNode(""));

        DirectoryNode root = new DirectoryNode("root");
        FileNode file = new FileNode("unique.txt", 100);
        root.add(file);

        assertThrows(IllegalArgumentException.class, () -> root.add(new FileNode("unique.txt", 200)));

        DirectoryNode other = new DirectoryNode("other");
        assertThrows(IllegalStateException.class, () -> other.add(file));

        DirectoryNode parent = new DirectoryNode("parent");
        DirectoryNode child = new DirectoryNode("child");
        DirectoryNode grandchild = new DirectoryNode("grandchild");

        parent.add(child);
        child.add(grandchild);

        assertThrows(IllegalStateException.class, () -> grandchild.add(parent));
        assertThrows(IllegalStateException.class, () -> parent.add(parent));

        DirectoryNode sibling = new DirectoryNode("sibling");
        parent.add(sibling);
        assertEquals(2, parent.getChildren().size());
    }
}
