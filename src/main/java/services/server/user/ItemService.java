package services.server.user;

import DTO.ItemDTO;
import DTO.ItemDeletedDTO;
import DTO.MoveCopyFolderDTO;
import DTO.PathItem;
import enums.FolderTypeId;
import enums.PermissionType;
import enums.TypeEnum;
import javafx.util.Pair;
import models.File;
import models.Folder;
import org.hibernate.Session;
import utils.HibernateUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class ItemService {
    public ItemService() {
    }

    public List<ItemDTO> getAllItem(int userId, int folderId, String searchText){
        PermissionService permissionService = new PermissionService();
        int permission = permissionService.checkUserPermission(userId, folderId, true);
        if (permission <= 1) {
            return null;
        }

        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            String folderPermissionConditions = "(per.permissionType IN (2, 3) AND (per.userId is null OR per.userId = :userId)) OR fd.ownerId = :userId";
            String folderQuery = "select distinct fd from Folder fd Join Permission per on fd.id = per.folderId" +
                    " where fd.parentId = :folderId AND fd.folderName LIKE :searchText AND fd.isDeleted = false" +
                    " AND (" + folderPermissionConditions + ")";
            List<Folder> folderList = session.createQuery(folderQuery, Folder.class)
                    .setParameter("folderId", folderId)
                    .setParameter("searchText", "%" + searchText + "%")
                    .setParameter("userId", userId)
                    .list();

            String filePermissionConditions = "(per.permissionType IN (2, 3) AND (per.userId is null OR per.userId = :userId)) OR fl.ownerId = :userId";
            String fileQuery = "select distinct fl from File fl Join Permission per on fl.folderId = per.folderId" +
                    " where fl.folderId = :folderId AND fl.name LIKE :searchText AND fl.isDeleted = false" +
                    " AND (" + filePermissionConditions + ")";
            List<File> fileList = session.createQuery(fileQuery, File.class)
                    .setParameter("folderId", folderId)
                    .setParameter("searchText", "%" + searchText + "%")
                    .setParameter("userId", userId)
                    .list();

            return mapToListItemDTO(userId, folderList, fileList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    
    public List<MoveCopyFolderDTO> getAllFolderPopups(int userId, int itemId, boolean isFolder, int folderId){
        PermissionService permissionService = new PermissionService();
        int permission = permissionService.checkUserPermission(userId, folderId, true);
        if (permission < PermissionType.READ.getValue()) {
            return null;
        }

        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<MoveCopyFolderDTO> itemList = new ArrayList<>();

            String folderPermissionConditions = "(per.permissionType IN (2, 3) AND (per.userId is null OR per.userId = :userId)) OR fd.ownerId = :userId";
            String folderQuery = "select distinct fd from Folder fd Join Permission per on fd.id = per.folderId" +
                    " where fd.parentId = :folderId AND fd.isDeleted = false" + (isFolder ? " AND fd.id <> " + itemId : "") +
                    " AND (" + folderPermissionConditions + ")";
            List<Folder> folderList = session.createQuery(folderQuery, Folder.class)
                    .setParameter("folderId", folderId)
                    .setParameter("userId", userId)
                    .list();
            if(folderList != null) {
                for (Folder folder : folderList) {
                    MoveCopyFolderDTO folderToMoveCopyDTO = new MoveCopyFolderDTO();
                    folderToMoveCopyDTO.setId(folder.getId());
                    folderToMoveCopyDTO.setName(folder.getFolderName());

                    FolderService folderService = new FolderService();
                    while(folder.getId() != FolderTypeId.ROOT.getValue()) {
                        folderToMoveCopyDTO.addPathItem(new PathItem(folder.getId(), folder.getFolderName()));
                        folder = folderService.getFolderById(folder.getParentId());
                    }

                    itemList.add(folderToMoveCopyDTO);
                }
            }

            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private String getSizeName(long size) {
        String sizeName;
        sizeName = getString(size);
        return sizeName;
    }

    public static String getString(long size) {
        String sizeName;
        if (size < 1024) {
            sizeName = size + " bytes";
        } else if (size < 1024 * 1024) {
            sizeName = size / 1024 + " KB";
        } else if (size < 1024 * 1024 * 1024) {
            sizeName = size / (1024 * 1024) + " MB";
        } else {
            sizeName = size / (1024 * 1024 * 1024) + " GB";
        }
        return sizeName;
    }

    private List<ItemDTO> mapToListItemDTO(int userId, List<Folder> folderList, List<File> fileList) {
        List<ItemDTO> itemList = new ArrayList<>();
        FolderService folderService = new FolderService();

        if(folderList != null) {
            for (Folder folder : folderList) {
                ItemDTO folderToItemDTO = new ItemDTO();

                folderToItemDTO.setId(folder.getId());
                folderToItemDTO.setName(folder.getFolderName());
                folderToItemDTO.setTypeId(TypeEnum.FOLDER.getValue());
                folderToItemDTO.setTypeName("");
                folderToItemDTO.setParentId(folder.getParentId());
                folderToItemDTO.setOwnerId(folder.getOwnerId());
                folderToItemDTO.setOwnerName(folder.getUsersByOwnerId().getName());

                Pair<Timestamp, Integer> updatedFolderInfo = folderService.getLastModifiedInfo(folder.getId());
                folderToItemDTO.setUpdatedDate(updatedFolderInfo.getKey() != null? updatedFolderInfo.getKey() : null);

                String updatedPersonName = updatedFolderInfo.getValue() == null
                        ? null
                        : new UserService().getNameOfUserById(updatedFolderInfo.getValue());
                folderToItemDTO.setUpdatedPersonName(updatedFolderInfo.getValue() == null ? "" : updatedPersonName);
                int countItem = folderService.getNumberItemOfFolder(userId, folder.getId());
                folderToItemDTO.setSize(countItem);
                folderToItemDTO.setSizeName(countItem + " mục");

                while(folder.getId() != FolderTypeId.ROOT.getValue()) {
                    folderToItemDTO.addPathItem(new PathItem(folder.getId(), folder.getFolderName()));
                    folder = folderService.getFolderById(folder.getParentId());
                }

                itemList.add(folderToItemDTO);
            }
        }

        if(fileList != null) {
            for (File file : fileList) {
                ItemDTO fileToItemDTO = new ItemDTO();

                fileToItemDTO.setId(file.getId());
                fileToItemDTO.setName(file.getName());
                fileToItemDTO.setTypeId(file.getTypeId());
                fileToItemDTO.setTypeName(file.getTypesByTypeId().getName());
                fileToItemDTO.setParentId(file.getFolderId());
                fileToItemDTO.setOwnerId(file.getOwnerId());
                fileToItemDTO.setOwnerName(file.getUsersByOwnerId().getName());
                fileToItemDTO.setUpdatedDate(file.getUpdatedAt());
                fileToItemDTO.setUpdatedPersonName(file.getUsersByUpdatedBy().getName());
                fileToItemDTO.setSize(file.getSize());
                fileToItemDTO.setSizeName(getSizeName(file.getSize()));

                Folder folder = folderService.getFolderById(file.getFolderId());
                while(folder.getId() != FolderTypeId.ROOT.getValue()) {
                    fileToItemDTO.addPathItem(new PathItem(folder.getId(), folder.getFolderName()));
                    folder = folderService.getFolderById(folder.getParentId());
                }

                itemList.add(fileToItemDTO);
            }
        }

        return itemList;
    }

    public List<ItemDTO> getAllItemPrivateOwnerId(int userId, String searchText){
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Folder> folderList;
            if(searchText.isEmpty()) {
                String folderQuery = "select distinct fd from Folder fd" +
                        " where fd.ownerId = :userId AND fd.isDeleted = false" +
                        " and fd.parentId NOT IN (SELECT fd2.id FROM Folder fd2" +
                        " where fd2.ownerId = :userId and fd2.isDeleted = false and fd.parentId <> :rootId)";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("userId", userId)
                        .setParameter("rootId", FolderTypeId.ROOT.getValue())
                        .list();
            } else {
                String folderQuery = "select distinct fd from Folder fd" +
                        " where fd.ownerId = :userId AND fd.folderName LIKE :searchText AND fd.isDeleted = false";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

            List<File> fileList;
            if(searchText.isEmpty()){
                String fileQuery = "select distinct fl from File fl" +
                        " where fl.ownerId = :userId AND fl.isDeleted = false" +
                        " AND fl.folderId NOT IN (SELECT fd.id FROM Folder fd" +
                        " WHERE fd.ownerId = :userId and fd.isDeleted = false and fd.parentId <> :rootId)";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("userId", userId)
                        .setParameter("rootId", FolderTypeId.ROOT.getValue())
                        .list();
            } else {
                String fileQuery = "select distinct fl from File fl" +
                        " where fl.ownerId = :userId AND fl.name LIKE :searchText AND fl.isDeleted = false";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

            return mapToListItemDTO(userId, folderList, fileList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<ItemDTO> getAllOtherShareItem(int userId, String searchText){
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Folder> folderList;
            if(searchText.isEmpty()) {
                String folderQuery = "select distinct fd from Folder fd Join Permission per on fd.id = per.folderId" +
                        " where fd.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.userId = :userId" +
                        " AND fd.parentId NOT IN (SELECT fd2.id FROM Folder fd2 Join Permission per2 on fd2.id = per2.folderId" +
                        " WHERE per2.userId = :userId AND per2.permissionType IN (2, 3) and fd2.isDeleted = false)";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("userId", userId)
                        .list();
            } else {
                String folderQuery = "select distinct fd from Folder fd Join Permission per on fd.id = per.folderId" +
                        " where fd.folderName LIKE :searchText AND fd.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.userId = :userId";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

            List<File> fileList;
            if(searchText.isEmpty()){
                String fileQuery = "select distinct f from File f Join Permission per on f.id = per.fileId" +
                        " where f.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.userId = :userId" +
                        " AND f.folderId NOT IN (SELECT fd.id FROM Folder fd Join Permission per on fd.id = per.folderId" +
                        " WHERE per.userId = :userId AND per.permissionType IN (2, 3) and fd.isDeleted = false)";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("userId", userId)
                        .list();
            } else {
                String fileQuery = "select distinct f from File f Join Permission per on f.id = per.fileId" +
                        " where f.name LIKE :searchText AND f.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.userId = :userId";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

            return mapToListItemDTO(userId, folderList, fileList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
    public List<ItemDTO> getAllSharedItem(int userId, String searchText){
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Folder> folderList;
            if(searchText.isEmpty()) {
                String folderQuery = "select distinct fd from Folder fd Join Permission per on fd.id = per.folderId" +
                        " where fd.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.sharedBy = :userId" +
                        " AND fd.parentId NOT IN (SELECT fd2.id FROM Folder fd2 Join Permission per2 on fd2.id = per2.folderId" +
                        " WHERE per2.sharedBy = :userId AND per2.permissionType IN (2, 3) and fd2.isDeleted = false)";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("userId", userId)
                        .list();
            } else {
                String folderQuery = "select distinct fd from Folder fd Join Permission per on fd.id = per.folderId" +
                        " where fd.folderName LIKE :searchText AND fd.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.sharedBy = :userId";
                folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

            List<File> fileList;
            if(searchText.isEmpty()){
                String fileQuery = "select distinct f from File f Join Permission per on f.id = per.fileId" +
                        " where f.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.sharedBy = :userId" +
                        " AND f.folderId NOT IN (SELECT fd.id FROM Folder fd Join Permission per on fd.id = per.folderId" +
                        " WHERE per.sharedBy = :userId AND per.permissionType IN (2, 3) and fd.isDeleted = false)";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("userId", userId)
                        .list();
            } else {
                String fileQuery = "select distinct f from File f Join Permission per on f.id = per.fileId" +
                        " where f.name LIKE :searchText AND f.isDeleted = false" +
                        " AND per.permissionType IN (2, 3) AND per.userId = :userId";
                fileList = session.createQuery(fileQuery, File.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .list();
            }

            return mapToListItemDTO(userId, folderList, fileList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ItemDeletedDTO> getAllDeletedItem(int userId, String searchText) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Folder> folderList;

            String folderQuery ="select distinct fd from Folder fd" +
                        " where fd.folderName LIKE :searchText AND fd.parentId = :trashId and fd.isDeleted = true AND fd.ownerId = :userId";
            folderList = session.createQuery(folderQuery, Folder.class)
                        .setParameter("searchText", "%" + searchText + "%")
                        .setParameter("userId", userId)
                        .setParameter("trashId", FolderTypeId.TRASH.getValue())
                        .list();

            List<File> fileList;
            String fileQuery = "select distinct fl from File fl" +
                    " where fl.name LIKE :searchText AND fl.folderId = :trashId and fl.isDeleted = true AND fl.ownerId = :userId";
            fileList = session.createQuery(fileQuery, File.class)
                    .setParameter("searchText", "%" + searchText + "%")
                    .setParameter("userId", userId)
                    .setParameter("trashId", FolderTypeId.TRASH.getValue())
                    .list();

            return mapToListItemDeletedDTO(userId, folderList, fileList);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    private List<ItemDeletedDTO> mapToListItemDeletedDTO(int userId, List<Folder> folderList, List<File> fileList) {
        List<ItemDeletedDTO> itemList = new ArrayList<>();
        FolderService folderService = new FolderService();

        if(folderList != null) {
            for (Folder folder : folderList) {
                ItemDeletedDTO folderToItemDeletedDTO = new ItemDeletedDTO();

                folderToItemDeletedDTO.setId(folder.getId());
                folderToItemDeletedDTO.setName(folder.getFolderName());
                folderToItemDeletedDTO.setTypeId(TypeEnum.FOLDER.getValue());
                folderToItemDeletedDTO.setTypeName("");
                folderToItemDeletedDTO.setDeletedDate(folder.getDateDeleted());
                folderToItemDeletedDTO.setDeletedPersonName(folder.getUsersByDeletedBy().getName());
                folderToItemDeletedDTO.setBeforeDeletedPath(folder.getFinalpath());

                int countItem = new FolderService().getNumberItemOfFolder(userId, folder.getId());
                folderToItemDeletedDTO.setSize(countItem);
                folderToItemDeletedDTO.setSizeName(countItem + " mục");

                while(folder.getId() != FolderTypeId.ROOT.getValue()) {
                    folderToItemDeletedDTO.addPathItem(new PathItem(folder.getId(), folder.getFolderName()));
                    folder = folderService.getFolderById(folder.getParentId());
                }

                itemList.add(folderToItemDeletedDTO);
            }
        }

        if(fileList != null) {
            for (File file : fileList) {
                ItemDeletedDTO fileToItemDeletedDTO = new ItemDeletedDTO();

                fileToItemDeletedDTO.setId(file.getId());
                fileToItemDeletedDTO.setName(file.getName());
                fileToItemDeletedDTO.setTypeId(file.getTypeId());
                fileToItemDeletedDTO.setTypeName(file.getTypesByTypeId().getName());
                fileToItemDeletedDTO.setDeletedDate(file.getDateDeleted());
                fileToItemDeletedDTO.setDeletedPersonName(file.getUsersByDeletedBy().getName());
                fileToItemDeletedDTO.setBeforeDeletedPath(file.getFinalpath());
                fileToItemDeletedDTO.setSize(file.getSize());
                fileToItemDeletedDTO.setSizeName(getSizeName(file.getSize()));

                Folder folder = folderService.getFolderById(file.getFolderId());
                while(folder.getId() != FolderTypeId.ROOT.getValue()) {
                    fileToItemDeletedDTO.addPathItem(new PathItem(folder.getId(), folder.getFolderName()));
                    folder = folderService.getFolderById(folder.getParentId());
                }

                itemList.add(fileToItemDeletedDTO);
            }
        }

        return itemList;
    }
}
