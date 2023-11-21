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

}
