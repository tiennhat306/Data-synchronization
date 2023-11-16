package utils;

import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;

public class HibernateUtil {
//    public static SessionFactory getSessionFactory() {
//        SessionFactory sessionFactory = null;
//        try {
//            Configuration configuration = new Configuration();
//            sessionFactory = configuration.configure().buildSessionFactory();
//        } catch (Throwable ex) {
//            ex.printStackTrace();
//        }
//        return sessionFactory;
//    }
    private static SessionFactory sessionFactory;

    static{
        try {
            Configuration configuration = new Configuration();
            configuration.configure();
//            StandardServiceRegistryBuilder registryBuilder = new StandardServiceRegistryBuilder()
//                    .applySettings(configuration.getProperties());
            // registryBuilder.build()
            sessionFactory = configuration.buildSessionFactory();
        } catch (Throwable ex) {
            System.err.println("Không thể khởi tạo SessionFactory: " + ex);
            ex.printStackTrace();
        }
    }
    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }
    public static void close() {
        // Close caches and connection pools
        getSessionFactory().close();
    }
}
