package services.user;

import models.Item;
import org.hibernate.Session;

import java.util.List;

public class ItemService {
    private final Session session;
    public ItemService() {
        this.session = null;
    }
    public ItemService(Session session) {
        this.session = session;
    }

    public List<Item> getAllItem() {
        try {
            List<Item> query = session.createQuery("from Item", Item.class).list();
            return query;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }


}
