package applications;

import java.util.Iterator;
import java.util.List;

import models.*;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import utils.HibernateUtil;
import org.hibernate.cfg.Configuration;

public class testConnect {

    private static SessionFactory factory;
    public static void main(String[] args) {
        factory = HibernateUtil.getSessionFactory();
        try (Session session = factory.openSession()) {
            //Transaction tx = null;
            try {
                //tx = session.beginTransaction();
                List<File> types = session.createQuery("from File ", File.class).list();
                //Iterator<Type> iterator = files.iterator();
                System.out.println("List files:");
                for (Iterator iterator = types.iterator(); iterator.hasNext(); ) {
                    File file = (File) iterator.next();
                    String filePath = file.getName() + "." + file.getTypesByTypeId().getName();
                    Folder folder = file.getFoldersByFolderId();
                    while (folder != null) {
                        filePath = folder.getFolderName() + "/" + filePath;
                        folder = folder.getFoldersByParentId();
                    }
                    System.out.println("/" + filePath);
                }
                //tx.commit();
            } catch (HibernateException e) {
//                if (tx != null) {
//                    tx.rollback();
//                }
                e.printStackTrace();
            } finally {
                session.close();
            }
        }
//        Configuration configuration = new Configuration();
//        SessionFactory sessionFactory = configuration.configure().buildSessionFactory();
//        Session session = sessionFactory.openSession();
//
//        Type type = new Type();
//        type.setName("test");
//
//        Transaction tx = session.getTransaction();
//        tx.begin();
//        session.save(type);
//        tx.commit();
    }
}
