package services.server.user;

import jakarta.persistence.NoResultException;
import models.Type;
import org.hibernate.Session;
import utils.HibernateUtil;

public class TypeService {

    public static String getTypeName(int typeId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            return session.createQuery("select t.name from Type t where t.id = :typeId", String.class)
                    .setParameter("typeId", typeId)
                    .getSingleResult();
        } catch (Exception e){
            e.printStackTrace();
            return "";
        }


    }

    public int getTypeId(String typeOfFile) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()){
            try{
                return session.createQuery("select t.id from Type t where t.name = :typeOfFile", Integer.class)
                        .setParameter("typeOfFile", typeOfFile)
                        .getSingleResult();
            } catch (NoResultException e){
                session.beginTransaction();
                Type type = new Type();
                type.setName(typeOfFile);
                session.persist(type);
                session.getTransaction().commit();
                return type.getId();
            }
        } catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }
}
