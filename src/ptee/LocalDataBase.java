package ptee;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class LocalDataBase {

	private String		fileName;
	private Connection	connection;
	private String		status;

	public LocalDataBase(String fileName) {
		this.fileName = fileName;
		try {
			Class.forName("org.sqlite.JDBC");

			this.connection = DriverManager.getConnection("jdbc:sqlite:" + this.fileName);
			Statement st = this.connection.createStatement();

			st.execute("create table if not exists 'Cves' ('id' INTEGER PRIMARY KEY AUTOINCREMENT, 'name' text, 'status' text, 'link' text, 'moduleName' text, 'moduleDescription' text);");

			this.status = "ok";
			if (st != null) {
				st.close();
			}
		} catch (SQLException | ClassNotFoundException e) {
			e.printStackTrace();
			this.status = e.toString();
		}
	}

	public void add(Cve cve) {
		String query = "insert into 'Cves' ('name', 'status', 'link', 'moduleName', 'moduleDescription') values (?,?,?,?,?)";
		try {
			PreparedStatement preparedStatement = this.connection.prepareStatement(query);
			preparedStatement.setString(1, cve.name);
			preparedStatement.setString(2, cve.status);
			preparedStatement.setString(3, cve.link);
			preparedStatement.setString(4, cve.moduleName);
			preparedStatement.setString(5, cve.moduleDescription);
			preparedStatement.executeUpdate();
		} catch (SQLException e1) {
			e1.printStackTrace();
		}
	}

	public boolean find(String cveName) {
		boolean result = false;

		String query = "SELECT * FROM Cves WHERE name=?";
		try {
			PreparedStatement preparedStatement = this.connection.prepareStatement(query);
			preparedStatement.setString(1, cveName);
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				if (rs.getInt(1) > 0) {
					result = true;
				}
			} else {
				result = false;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			this.status = e.toString();
		}
		return result;
	}

	public boolean get(Cve cve) {
		if (!find(cve.name)) {
			return false;
		}
		String query = "SELECT * FROM Cves WHERE name=?";
		try {
			PreparedStatement preparedStatement = this.connection.prepareStatement(query);
			preparedStatement.setString(1, cve.name);
			ResultSet rs = preparedStatement.executeQuery();
			if (rs.next()) {
				cve.status = rs.getString("status");
				cve.link = rs.getString("link");
				cve.moduleName = rs.getString("moduleName");
				cve.moduleDescription = rs.getString("moduleDescription");
			} else {
				return false;
			}
			rs.close();
		} catch (SQLException e) {
			e.printStackTrace();
			this.status = e.toString();
		}
		return true;
	}

	public String getStatus() {
		return this.status;
	}

	public Integer getSize() {
		String query = "SELECT COUNT(*) FROM 'Cves'";
		Integer numberOfRows = Integer.valueOf(0);
		try {
			Statement st = this.connection.createStatement();
			ResultSet rs = st.executeQuery(query);
			if (rs.next()) {
				numberOfRows = Integer.valueOf(rs.getInt(1));
			} else {
				System.out.println("error: could not get the record counts");
			}
			rs.close();
			st.close();
		} catch (SQLException e) {
			e.printStackTrace();
		}
		return numberOfRows;
	}

	public String getNameDataBase() {
		return this.fileName;
	}

	public void close() {
		try {
			this.connection.close();
		} catch (SQLException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}
}
