package ca.mikegabelmann.jarscanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;


/**
 *
 * @author mgabelmann
 */
public class ProcessFiles {
    private static final Logger LOGGER = LogManager.getLogger(ProcessFiles.class);

    private final String path;


    /**
     * Constructor.
     * @param path file path
     */
    public ProcessFiles(final String path) {
        if (path == null) {
            throw new IllegalArgumentException("path is required");
        }

        this.path = path;
    }

    /**
     * Process path.
     * @throws IOException error
     */
    public void process() throws IOException {
        ExecutorService es = Executors.newFixedThreadPool(1);
        ZipVisitor zv = new ZipVisitor(es);

        Files.walkFileTree(Paths.get(path), zv);

        es.shutdown();
    }

    /**
     * Entry point.
     * @param args command line arguments
     * @throws Exception error
     */
    public static void main(final String[] args) throws Exception {
        if (args.length != 1) {
            throw new IllegalArgumentException("A directory is required");
        }

        new ProcessFiles(args[0]).process();
    }

}
