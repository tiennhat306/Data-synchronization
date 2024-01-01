package DTO;

import java.io.Serializable;
import java.util.LinkedList;

public class MoveCopyFolderDTO implements Serializable {
    private int id;
    private String name;
    private LinkedList<PathItem> path;

    public MoveCopyFolderDTO() {
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public LinkedList<PathItem> getPath() {
        return path;
    }

    public void setPath(LinkedList<PathItem> path) {
        this.path = path;
    }

    public void addPathItem(PathItem pathItem) {
        if (path == null) {
            path = new LinkedList<>();
        }
        path.add(0, pathItem);
    }
}
