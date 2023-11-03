package applications;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.Socket;
import java.sql.Timestamp;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.hibernate.Session;
import org.hibernate.SessionFactory;

import DTO.Item;
import connection.HibernateProvider;
import javafx.application.Platform;
import javafx.scene.control.Alert;
import javafx.scene.control.Alert.AlertType;
import javafx.util.Pair;
import javafx.scene.control.DialogPane;
import models.File;
import models.Folder;
import services.user.FolderService;
import services.user.UserService;

public class ServerThread implements Runnable {
	private Socket socketOfServer;
	private int clientNumber;
	private BufferedReader is;
	private BufferedWriter os;
	private boolean isClosed;
	static SessionFactory sessionFactory;

	public BufferedReader getIs() {
		return is;
	}

	public BufferedWriter getOs() {
		return os;
	}

	public int getClientNumber() {
		return clientNumber;
	}

	public ServerThread() {
		// TODO Auto-generated constructor stub
	}

	public ServerThread(Socket socketOfServer, int clientNumber) {
		this.socketOfServer = socketOfServer;
		this.clientNumber = clientNumber;
		System.out.println("Server thread number " + clientNumber + " Started");
		isClosed = false;
		sessionFactory = HibernateProvider.getSessionFactory();
	}

	public static boolean checkExistedFile(String name) {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		String sqlQuery = "Select count(*) from files f where f.name = :name";
		int result = Integer.parseInt(session.createNativeQuery(sqlQuery, String.class).setParameter("name", name)
				.getSingleResult().toString());
		session.getTransaction().commit();
		session.close();
		return result == 0 ? true : false;
	}

	@SuppressWarnings("deprecation")
	private static int findFileType(String text) {
		if (sessionFactory == null) {
			throw new IllegalStateException("SessionFactory is not properly initialized");
		}

		Session session = sessionFactory.openSession();
		session.beginTransaction();

		String sqlQuery = "Select t.id FROM types t where t.name = :text";
		System.out.println(sqlQuery);
		int result = Integer
				.parseInt(session.createNativeQuery(sqlQuery).setParameter("text", text).uniqueResult().toString());
		session.getTransaction().commit();
		session.close();
		return result;
	}

	private static List<String> getListFileName() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		List<String> result = new ArrayList<String>();

		String sqlQuery = "SELECT files.name FROM files";

		result = session.createNativeQuery(sqlQuery, String.class).getResultList();

		session.getTransaction().commit();
		session.close();
		return result;
	}

	private static List<String> updateListFileName() {
		Session session = sessionFactory.openSession();
		session.beginTransaction();

		List<String> result = new ArrayList<String>();

		String sqlQuery = "SELECT files.name FROM files";

		result = session.createNativeQuery(sqlQuery, String.class).getResultList();

		session.getTransaction().commit();
		session.close();
		return result;
	}

	private static void handleUploadFile(String requestedName, int requestedType, int folderId, int ownerId, int size,
			Timestamp createdAt, Timestamp updatedAt) {

		Session session = sessionFactory.openSession();
		session.beginTransaction();
		File file = new File();

		// Set the properties of the File entity
		file.setName(requestedName);
		file.setTypeId(requestedType);
		file.setFolderId(folderId);
		file.setOwnerId(ownerId);
		file.setSize(size);
		file.setCreatedAt(createdAt);
		file.setUpdatedAt(updatedAt);

		// Persist the File entity
		session.persist(file);
		session.getTransaction().commit();
		session.close();

	}

	private static void handleUploadFolder(String folderName, int owner_id, int parent_id) {
         Session session = sessionFactory.openSession();
         session.beginTransaction();
         Folder folder = new Folder();
         
         folder.setFolderName(folderName);
         folder.setOwnerId(owner_id);
         folder.setParentId(parent_id);
         
         session.persist(folder);
         session.getTransaction().commit();
         session.close();
	}

	private static String convertFileListToString(List<File> fileList) {
		StringBuilder builder = new StringBuilder();
		for (File file : fileList) {
			builder.append(file.getName()).append(';'); // Use a suitable delimiter
		}
		return builder.toString();
	}

	@Override
	public void run() {
		try {
			Instant currentInstant = Instant.now();

			// Convert Instant to Timestamp
			Timestamp currentTimestamp = Timestamp.from(currentInstant);
			BufferedReader reader = new BufferedReader(new InputStreamReader(socketOfServer.getInputStream()));
			PrintWriter writer = new PrintWriter(socketOfServer.getOutputStream(), true);
			System.out.println("Khời động luông mới thành công, ID là: " + clientNumber);

			List<String> fileList = getListFileName();
			String result = fileList.toString();

			// Loại bỏ dấu [ và ] nếu có
			if (result.startsWith("[") && result.endsWith("]")) {
				result = result.substring(1, result.length() - 1);
			}

			writer.println(result);
			System.out.println(result);

			String generalName = reader.readLine();
			System.out.println(generalName);
			
			if(generalName != null) {
				if(generalName.contains(".")) {
					int indexOfDot = generalName.indexOf(".");
					String beforeDot = generalName.substring(0, indexOfDot); // Characters before the first period
					String afterDot = generalName.substring(indexOfDot + 1); // Characters after the first period
					// Send the two parts to the server
					System.out.println(beforeDot);
					System.out.println(afterDot);
					handleUploadFile(beforeDot, findFileType(afterDot), 1, 1, 30, currentTimestamp, currentTimestamp);
					System.out.println("Thêm thành công");
				} else {
					handleUploadFolder(generalName, 1, 1);
					System.out.println("Thêm folder thành công");
				}
			} else {
				System.out.println("Khong co file hay folder nao duoc them vao");
			}
			
			
//			updateListFileName();
//			List<String> fileListNew = getListFileName();
//			String resultNew = fileList.toString();
//
//			// Loại bỏ dấu [ và ] nếu có
//			if (resultNew.startsWith("[") && resultNew.endsWith("]")) {
//				resultNew = resultNew.substring(1, resultNew.length() - 1);
//			}
//
//			writer.println(resultNew);
//			System.out.println(resultNew);
			
			

		} catch (IOException e) {
			isClosed = true;
			System.out.println(this.clientNumber + " đã thoát");
		}
	}
}
