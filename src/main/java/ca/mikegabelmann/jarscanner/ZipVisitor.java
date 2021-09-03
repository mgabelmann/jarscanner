package ca.mikegabelmann.jarscanner;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.IOException;
import java.nio.file.FileVisitResult;
import java.nio.file.Path;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.attribute.BasicFileAttributes;
import java.util.concurrent.ExecutorService;

/**
 *
 * @author mgabelmann
 */
public class ZipVisitor extends SimpleFileVisitor<Path> {
    private static final Logger LOGGER = LogManager.getLogger(ZipVisitor.class);

    private ExecutorService service;


    /**
     * Constructor.
     * @param service service
     */
    public ZipVisitor(ExecutorService service) {
        this.service = service;
    }

    @Override
    public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) throws IOException {
        LOGGER.debug("directory={}", dir);

        return super.preVisitDirectory(dir, attrs);
    }

    @Override
    public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) throws IOException {
        if (file.toString().endsWith(".jar")) {
            LOGGER.debug("{} is being processed", file.getFileName());

            service.execute(new JarProcessor(file));

            return FileVisitResult.CONTINUE;

        } else {
            LOGGER.info("{}, is not a jar - skipped", file);
        }

        return super.visitFile(file, attrs);
    }

}
