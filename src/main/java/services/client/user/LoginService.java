package services.client.auth;

import DTO.UserSession;
import services.client.SocketClientHelper;

public class LoginService {
    private static ThreadLocal<UserSession> threadLocalSession = ThreadLocal.withInitial(UserSession::new);
    public static UserSession getCurrentSession() {
        return threadLocalSession.get();
    }
    public static void clearCurrentSession() {
        threadLocalSession.remove();
    }

    public UserSession login(String username, String password) {
        try{
            SocketClientHelper socketClientHelper = new SocketClientHelper();
            socketClientHelper.sendRequest("LOGIN");
            socketClientHelper.sendRequest(username);
            socketClientHelper.sendRequest(password);

            UserSession user = (UserSession) socketClientHelper.receiveResponse();
            socketClientHelper.close();
            return user;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
