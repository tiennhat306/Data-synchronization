package services.login;

import DTO.LoginSession;
public class LoginService {
    private static ThreadLocal<LoginSession> threadLocalSession = ThreadLocal.withInitial(LoginSession::new);
    public static LoginSession getCurrentSession() {
        return threadLocalSession.get();
    }
    public static void clearCurrentSession() {
        threadLocalSession.remove();
    }
}