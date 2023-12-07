package utils;

import javafx.util.Pair;
import models.File;
import models.Folder;
import services.server.user.FileService;
import services.server.user.FolderService;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class MapUtil {
    private static Map<Integer, Pair<Integer, Integer>> mapFolder;
    private static Map<Integer, Pair<Integer, Integer>> mapFile;

    public static void fillMapFolder() {
        mapFolder = new ConcurrentHashMap<>();
        FolderService folderService = new FolderService();
        List<Folder> fdList = folderService.getAllFolder();
        for (Folder folder : fdList) {
            if (folder.getId() == 1) continue;
            mapFolder.put(folder.getId(), new Pair<>(folder.getParentId(), folder.getOwnerId()));
        }
    }

    public static void fillMapFile() {
        mapFile = new ConcurrentHashMap<>();
        FileService fileService = new FileService();
        List<File> fiList = fileService.getAllFile();
        for (File file : fiList) {
            mapFile.put(file.getId(), new Pair<>(file.getFolderId(), file.getOwnerId()));
        }
    }

    public static Map<Integer, Pair<Integer, Integer>> getMapFolder() {
        if (mapFolder == null) {
            fillMapFolder();
        }
        return mapFolder;
    }

    public static Map<Integer, Pair<Integer, Integer>> getMapFile() {
        if (mapFile == null) {
            fillMapFile();
        }
        return mapFile;
    }
}