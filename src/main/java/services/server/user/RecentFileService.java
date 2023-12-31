package services.server.user;

import DTO.PathItem;
import DTO.RecentFileDTO;
import enums.FolderTypeId;
import jakarta.persistence.NoResultException;
import models.Folder;
import models.RecentFile;
import org.hibernate.Session;
import org.hibernate.Transaction;
import utils.HibernateUtil;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class RecentFileService {
    public boolean addRecentFile(int userId, int fileId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            Transaction transaction = session.beginTransaction();

            try{
                RecentFile existRecentFile = session.createQuery("select rf from RecentFile rf where rf.userId = :userId and rf.fileId = :fileId", RecentFile.class)
                        .setParameter("userId", userId)
                        .setParameter("fileId", fileId)
                        .uniqueResult();
                if(existRecentFile != null){
                    existRecentFile.setOpenedAt(new Timestamp(System.currentTimeMillis()));
                    session.merge(existRecentFile);
                    transaction.commit();
                    return true;
                } else {
                    throw new NoResultException();
                }
            } catch (NoResultException e) {
                RecentFile recentFile = new RecentFile();
                recentFile.setUserId(userId);
                recentFile.setFileId(fileId);
                recentFile.setOpenedAt(new Timestamp(System.currentTimeMillis()));
                session.persist(recentFile);
                transaction.commit();
                return true;
            } catch (Exception e){
                e.printStackTrace();
                transaction.rollback();
                return false;
            }
        } catch (Exception e){
            e.printStackTrace();
            return false;
        }
    }

    public List<RecentFileDTO> getAllRecentOpenedItem(int userId, String searchText) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            List<RecentFileDTO> recentFileDTOList = new ArrayList<>();
            FolderService folderService = new FolderService();

            List<RecentFile> recentFiles = session.createQuery("select rf from RecentFile rf where rf.userId = :userId and rf.filesByFileId.name LIKE :searchText and rf.filesByFileId.isDeleted = false order by rf.openedAt desc", RecentFile.class)
                    .setParameter("userId", userId)
                    .setParameter("searchText", "%" + searchText + "%")
                    .setMaxResults(20)
                    .getResultList();
            if(recentFiles != null){
                for(RecentFile rf : recentFiles){
                    RecentFileDTO recentFileDTO = new RecentFileDTO();
                    recentFileDTO.setId(rf.getFileId());
                    recentFileDTO.setTypeId(rf.getFilesByFileId().getTypeId());
                    recentFileDTO.setName(rf.getFilesByFileId().getName());
                    recentFileDTO.setTypeName(rf.getFilesByFileId().getTypesByTypeId().getName());
                    recentFileDTO.setFolderId(rf.getFilesByFileId().getFolderId());
                    recentFileDTO.setOpenedDate(rf.getOpenedAt());
                    recentFileDTO.setOwnerName(rf.getFilesByFileId().getUsersByOwnerId().getName());
                    recentFileDTO.setPath(FolderService.getPath(rf.getFilesByFileId().getFolderId()));

                    Folder folder = folderService.getFolderById(rf.getFilesByFileId().getFolderId());
                    while(folder.getId() != FolderTypeId.ROOT.getValue()) {
                        recentFileDTO.addPathItem(new PathItem(folder.getId(), folder.getFolderName()));
                        folder = folderService.getFolderById(folder.getParentId());
                    }

                    recentFileDTOList.add(recentFileDTO);
                }
            }
            return recentFileDTOList;
        } catch (Exception e){
            e.printStackTrace();
            return null;
        }
    }
}
