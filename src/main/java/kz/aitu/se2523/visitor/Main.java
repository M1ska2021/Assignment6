package kz.aitu.se2523.visitor;

import kz.aitu.se2523.visitor.model.DirectoryNode;
import kz.aitu.se2523.visitor.model.FileNode;
import kz.aitu.se2523.visitor.visitor.SearchVisitor;
import kz.aitu.se2523.visitor.visitor.StatisticsVisitor;
import kz.aitu.se2523.visitor.visitor.TotalSizeVisitor;

public class Main {
    public static void main(String[] args) {
        DirectoryNode root = new DirectoryNode("portfolio");
        FileNode readme = new FileNode("README.md", 1450);
        FileNode pom = new FileNode("pom.xml", 2200);
        root.add(readme);
        root.add(pom);

        DirectoryNode src = new DirectoryNode("src");
        DirectoryNode mainDir = new DirectoryNode("main");
        FileNode app = new FileNode("App.java", 3100);
        FileNode config = new FileNode("Config.java", 850);
        mainDir.add(app);
        mainDir.add(config);
        src.add(mainDir);

        DirectoryNode resources = new DirectoryNode("resources");
        FileNode props = new FileNode("application.properties", 420);
        FileNode banner = new FileNode("banner.txt", 150);
        resources.add(props);
        resources.add(banner);
        src.add(resources);
        root.add(src);

        DirectoryNode docs = new DirectoryNode("docs");
        FileNode spec = new FileNode("specification.pdf", 15400);
        FileNode arch = new FileNode("architecture.png", 48000);
        docs.add(spec);
        docs.add(arch);
        root.add(docs);

        DirectoryNode archive = new DirectoryNode("archive");
        DirectoryNode emptyBackup = new DirectoryNode("empty_backup");
        archive.add(emptyBackup);
        root.add(archive);

        System.out.println("=== Visitor Pattern Demonstration ===");
        System.out.println("Root directory: " + root.getName());

        TotalSizeVisitor sizeVisitor = new TotalSizeVisitor();
        root.accept(sizeVisitor);
        System.out.println("\n--- TotalSizeVisitor ---");
        System.out.println("Subtree size: " + sizeVisitor.getTotalSize() + " bytes");

        SearchVisitor javaSearch = new SearchVisitor(".java", 1000);
        root.accept(javaSearch);
        System.out.println("\n--- SearchVisitor (.java, minSize >= 1000) ---");
        for (String path : javaSearch.getMatches()) {
            System.out.println("Match: " + path);
        }

        SearchVisitor largeSearch = new SearchVisitor("", 10000);
        root.accept(largeSearch);
        System.out.println("\n--- SearchVisitor (all files, minSize >= 10000) ---");
        for (String path : largeSearch.getMatches()) {
            System.out.println("Match: " + path);
        }

        StatisticsVisitor statsVisitor = new StatisticsVisitor();
        root.accept(statsVisitor);
        System.out.println("\n--- StatisticsVisitor ---");
        System.out.println("Total files: " + statsVisitor.getFileCount());
        System.out.println("Total directories: " + statsVisitor.getDirectoryCount());
        System.out.println("Maximum tree depth: " + statsVisitor.getMaxDepth());

        System.out.println("\n--- Verification of Reusability (No State Leakage) ---");
        sizeVisitor.reset();
        root.accept(sizeVisitor);
        System.out.println("Re-run size visitor: " + sizeVisitor.getTotalSize() + " bytes");

        statsVisitor.reset();
        root.accept(statsVisitor);
        System.out.println("Re-run statistics files: " + statsVisitor.getFileCount() +
                ", dirs: " + statsVisitor.getDirectoryCount() +
                ", depth: " + statsVisitor.getMaxDepth());

        System.out.println("\n--- Invariant Defense ---");
        try {
            root.add(new FileNode("README.md", 500));
        } catch (IllegalArgumentException e) {
            System.out.println("Caught duplicate sibling name: " + e.getMessage());
        }

        try {
            mainDir.add(root);
        } catch (IllegalStateException e) {
            System.out.println("Caught cycle detection: " + e.getMessage());
        }

        try {
            new FileNode("bad.bin", -10);
        } catch (IllegalArgumentException e) {
            System.out.println("Caught negative size: " + e.getMessage());
        }
    }
}
