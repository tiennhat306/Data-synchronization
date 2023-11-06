package services.client.user;

import models.File;
import services.client.SocketClientHelper;

import java.util.List;

public class ItemService {
    public ItemService() {
    }

    public List<File> getAllItem(int folderId){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("GET_ALL_ITEM");
            socketClientHelper.sendRequest(String.valueOf(folderId));

            Object obj = socketClientHelper.receiveResponse();
            List<File> itemList = (List<File>) obj;

            socketClientHelper.close();
            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
