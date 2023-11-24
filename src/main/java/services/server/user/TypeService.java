package services.server.user;

import org.hibernate.Session;
import utils.HibernateUtil;

public class TypeService {

    public String getTypeName(int typeId) {
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
            return session.createQuery("select t.id from Type t where t.name = :typeOfFile", Integer.class)
                    .setParameter("typeOfFile", typeOfFile)
                    .getSingleResult();
        } catch (Exception e){
            e.printStackTrace();
            return -1;
        }
    }
}
