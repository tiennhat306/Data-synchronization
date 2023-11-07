//package applications;
//
//import java.io.BufferedReader;
//import java.io.IOException;
//import java.net.ServerSocket;
//import java.net.Socket;
//import java.util.concurrent.ArrayBlockingQueue;
//import java.util.concurrent.ThreadPoolExecutor;
//import java.util.concurrent.TimeUnit;
//
//import org.hibernate.SessionFactory;
//
//import utils.HibernateUtil;
//
//public class Server {
//	public static BufferedReader is;
//
//	private static final String SERVER_HOST = "localhost";
//	private static final String SERVER_PATH = "D:\\User\\Desktop\\Server";
//	private static final int PORT = 6969;
//	// private static final int BUFFER_SIZE = 1024;
//	private static SessionFactory sessionFactory;
//	private int noOfThreads = 0;
//	private ServerSocket serverSocket;
//	private Socket clientSocket;
//
//	public static void main(String[] args) {
//
//		new Server();
//	}
//
//	public Server() {
//		// Hibernate SessionFactory
//		sessionFactory = HibernateUtil.getSessionFactory();
//		try {
//			serverSocket = new ServerSocket(PORT);
//			System.out.println("Server is waiting for connections...");
//		} catch (IOException e) {
//			System.out.println("Could not create server socket");
//			System.exit(-1);
//		}
//
//		ThreadPoolExecutor executor = new ThreadPoolExecutor(10, // corePoolSize
//				100, // maximumPoolSize
//				10, // thread timeout
//				TimeUnit.SECONDS, new ArrayBlockingQueue<>(8) // queueCapacity
//		);
//
//		while (true) {
//			try {
//				clientSocket = serverSocket.accept();
//				System.out.println("Client connected from: " + clientSocket.getInetAddress());
//				int dataPort = Server.PORT + noOfThreads + 1;
//				System.out.println(dataPort);
//				// Create new worker thread for new connection
//				//Worker w = new Worker(clientSocket, dataPort);
//				System.out.println("New connection received. Worker was created.");
//				//w.start();
//				ServerThread serverThread = new ServerThread(clientSocket, noOfThreads++);
//                executor.execute(serverThread);
//			} catch (IOException e) {
//				System.out.println("Error accepting client connection: " + e.getMessage());
//				if (clientSocket != null && !clientSocket.isClosed()) {
//					try {
//						clientSocket.close();
//					} catch (IOException ex) {
//						System.out.println("Error closing client socket: " + ex.getMessage());
//					}
//				}
//			}
//		}
//	}
//
//}
