package services.client.user;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import DTO.Item;
import services.client.SocketClientHelper;

import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;

public class ItemService {
    public ItemService() {
    }
    public List<Item> getAllItem(int folderId){
        try {
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            // send request to server
            socketClientHelper.sendRequest("GET_ALL_ITEM");
            socketClientHelper.sendRequest(String.valueOf(folderId));

            // receive response from server
            Type itemListType = new TypeToken<ArrayList<Item>>(){}.getType();
            List<Item> itemList = socketClientHelper.receiveObject(itemListType);

            socketClientHelper.close();
            return itemList;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

}
