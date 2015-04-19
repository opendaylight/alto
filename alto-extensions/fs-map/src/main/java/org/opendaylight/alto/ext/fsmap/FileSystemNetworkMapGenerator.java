package org.opendaylight.alto.ext.fsmap;

import java.net.URI;
import java.nio.file.FileSystem;
import java.nio.file.SimpleFileVisitor;
import java.nio.file.FileVisitResult;
import java.nio.file.PathMatcher;
import java.nio.file.WatchService;
import java.nio.file.WatchKey;
import java.nio.file.WatchEvent;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardWatchEventKinds;
import java.nio.charset.StandardCharsets;
import java.nio.file.ClosedWatchServiceException;
import java.nio.file.attribute.BasicFileAttributes;

import java.io.IOException;

import java.util.concurrent.locks.ReentrantLock;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.HashMap;

import org.opendaylight.alto.commons.types.rfc7285.RFC7285JSONMapper;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285NetworkMap;
import org.opendaylight.alto.commons.types.rfc7285.RFC7285VersionTag;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FileSystemNetworkMapGenerator implements Runnable, AutoCloseable {

    private static final Logger logger = LoggerFactory.getLogger(FileSystemNetworkMapGenerator.class);

    private ReentrantLock lock = new ReentrantLock();
    private Path source = null;
    private WatchService watcher = null;
    private HashMap<Path, WatchKey> keys = new HashMap<Path, WatchKey>();
    private HashMap<WatchKey, Path> paths = new HashMap<WatchKey, Path>();
    private MapFileLoader loader = null;
    private AtomicBoolean cancelled = new AtomicBoolean(false);
    private HashMap<Path, RFC7285VersionTag> path_to_id = new HashMap<Path, RFC7285VersionTag>();
    private HashMap<RFC7285VersionTag, RFC7285NetworkMap> id_to_map = new HashMap<RFC7285VersionTag, RFC7285NetworkMap>();
    private RFC7285JSONMapper mapper = new RFC7285JSONMapper();

    public FileSystemNetworkMapGenerator(URI uri) throws Exception {
        source = Paths.get(uri);

        FileSystem fs = source.getFileSystem();
        watcher = fs.newWatchService();
        if (watcher == null) {
            throw new IOException("Unable to create watcher on given uri: " + uri);
        }

        onCreateDir(source);

        loader = new MapFileLoader(fs);
        Files.walkFileTree(source, loader);
    }

    class MapFileLoader extends SimpleFileVisitor<Path> {
        private PathMatcher matcher = null;

        MapFileLoader(FileSystem fs) {
            matcher = fs.getPathMatcher("glob:**/*.{networkmap}");
        }

        @Override
        public FileVisitResult preVisitDirectory(Path dir, BasicFileAttributes attrs) {
            lock.lock();
            onCreateDir(dir);
            lock.unlock();
            return FileVisitResult.CONTINUE;
        }

        @Override
        public FileVisitResult visitFile(Path file, BasicFileAttributes attrs) {
            logger.info("visiting file: " + file.toString());
            if (file.toFile().isFile()) {
                if (matcher.matches(file)) {
                    lock.lock();
                    onCreate(file);
                    lock.unlock();
                }
            }
            return FileVisitResult.CONTINUE;
        }
    }

    public void run() {
        while (!cancelled.get()) {
            WatchKey key;
            try {
                key = watcher.take();
            } catch (ClosedWatchServiceException e) {
                System.out.println(e);
                break;
            } catch (Exception e) {
                System.out.println(e);
                continue;
            }

            for (WatchEvent<?> event: key.pollEvents()) {
                WatchEvent.Kind<?> kind = event.kind();

                if (kind == StandardWatchEventKinds.OVERFLOW)
                    continue;

                lock.lock();
                Path dir = paths.get(key);
                Path file = dir.resolve((Path)event.context());

                if (kind == StandardWatchEventKinds.ENTRY_CREATE) {
                    if (file.toFile().isFile())
                        onCreate(file);
                    else if (file.toFile().isDirectory())
                        onCreateDir(file);
                } else if (kind == StandardWatchEventKinds.ENTRY_DELETE) {
                    if (file.toFile().isFile())
                        onDelete(file);
                    else if (file.toFile().isDirectory())
                        onDeleteDir(file);
                } else if (kind == StandardWatchEventKinds.ENTRY_MODIFY) {
                    if (file.toFile().isFile())
                        onModify(file);
                    else if (file.toFile().isDirectory())
                        onModifyDir(file);
                }
                lock.unlock();
            }

            boolean valid = key.reset();
            if (!valid) {
                Path dir = paths.get(key);
                keys.remove(dir);
                paths.remove(key);

                if (keys.isEmpty())
                    break;
            }
        }
        cleanup();
    }

    public void cleanup() {
        lock.lock();
        for (WatchKey key: keys.values()) {
            try {
                if (key != null) {
                    key.cancel();
                }
            } catch (Exception e) {
            }
        }
        keys.clear();
        paths.clear();
        try {
            if (watcher != null) {
                watcher.close();
                watcher = null;
            }
        } catch (Exception e) {
        }

        for (RFC7285VersionTag vtag: path_to_id.values()) {
            // TODO remove network map
            if (vtag.incomplete())
                continue;
        }
        path_to_id.clear();
        id_to_map.clear();
        lock.unlock();
    }

    public void close() {
        cancelled.set(true);
        cleanup();
    }

    public void onCreate(Path file) {
        //TODO
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.US_ASCII);
            RFC7285NetworkMap map = mapper.asNetworkMap(content);

            RFC7285VersionTag vtag = map.meta.vtag;
            logger.info("vtag: <" + vtag.rid + ", " + vtag.tag + ">");
            if (id_to_map.get(map.meta.vtag) != null) {
                logger.warn("Version tag already registered: ("
                                + vtag.rid + ", " + vtag.tag + ")");
                return;
            }
            path_to_id.put(file, map.meta.vtag);
            id_to_map.put(map.meta.vtag, map);
            logger.info("create successfully: " + file.toString());
            logger.info("current maps: " + id_to_map.size());
        } catch (Exception e) {
            logger.warn("Error while creating " + file.toString());
            logger.warn(e.toString());
        }
    }

    public void onCreateDir(Path dir) {
        try {
            WatchKey key = dir.register(watcher,
                                    StandardWatchEventKinds.ENTRY_CREATE,
                                    StandardWatchEventKinds.ENTRY_DELETE,
                                    StandardWatchEventKinds.ENTRY_MODIFY);
            keys.put(dir, key);
            paths.put(key, dir);
            logger.info("create dir successfully: " + dir.toString());
        } catch (Exception e) {
            logger.warn(e.toString());
        }
    }

    public void onDelete(Path file) {
        //TODO
        try {
            RFC7285VersionTag vtag = path_to_id.get(file);
            if (vtag == null)
                return;

            id_to_map.remove(vtag);
            logger.info("delete: " + file.toString());
        } catch (Exception e) {
            logger.warn("Error while deleting " + file.toString());
            logger.warn(e.toString());
        }
    }

    public void onDeleteDir(Path dir) {
        // TODO
        try {
            WatchKey key = keys.get(dir);
            if (key != null) {
                key.cancel();

                keys.remove(dir);
                paths.remove(key);
            }
            logger.warn("delete dir successfully: " + dir.toString());
        } catch (Exception e) {
            logger.warn(e.toString());
        }
    }

    public void onModify(Path file) {
        //TODO
        try {
            String content = new String(Files.readAllBytes(file), StandardCharsets.US_ASCII);
            RFC7285NetworkMap map = mapper.asNetworkMap(content);
            RFC7285VersionTag vtag = map.meta.vtag;
            RFC7285VersionTag old = path_to_id.get(file);

            if (old != null) {
                if (vtag.rid != old.rid) {
                    throw new Exception("defining another map in one file is not allowed");
                }
                id_to_map.remove(old);
            }
            path_to_id.put(file, vtag);
            id_to_map.put(vtag, map);
            logger.info("modify successfully: " + file.toString());
        } catch (Exception e) {
            logger.warn("Error while modifying " + file.toString());
            logger.warn(e.toString());
        }
    }

    public void onModifyDir(Path dir) {
        //TODO
        onDeleteDir(dir);
        onModifyDir(dir);
    }
}
