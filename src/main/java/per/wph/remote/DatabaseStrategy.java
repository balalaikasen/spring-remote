package per.wph.remote;

import per.wph.beans.io.Resource;

import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.sql.*;

/**
 * =============================================
 *
 * @author wu
 * @create 2018-07-23 23:39
 * =============================================
 */
public class DatabaseStrategy implements ByteLoaderStrategy {

    private RemoteDBConfiguer dbConfiguer;

    public DatabaseStrategy(RemoteDBConfiguer dbConfiguer) {
        this.dbConfiguer = dbConfiguer;
    }

    private static final String TABLE_NAME_DEAFULT = "remote_beans";

    private static final String CLASS_BYTE_COLUMN_NAME = "class_byte";

    private static final String QUERY_DEFAULT = "SELECT * FROM ? WHERE remote_name = ? ORDER BY version DESC LIMIT 1";

    private static final String QUERY_FOR_VERSION = "SELECT * FROM ? WHERE remota_name = ? AND version = ?";


    @Override
    public byte[] load(RemoteBeanDefinition beanDefinition) {
        byte[] ret = null;
        Connection conn = getConn();
        try {
            if(beanDefinition.getVersion() != null && !beanDefinition.getVersion().equals("")){
                ret = doLoadByte(queryWithVersion(conn, beanDefinition));
                if(ret != null){
                    return ret;
                }
            }
            ret = doLoadByte(query(conn, beanDefinition));
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        return ret;
    }

    private InputStream queryWithVersion(Connection connection, RemoteBeanDefinition beanDefinition) throws Exception{
        InputStream is = null;
        PreparedStatement ps = connection.prepareStatement(QUERY_FOR_VERSION);
        ps.setString(1, TABLE_NAME_DEAFULT);
        ps.setString(2, beanDefinition.getRemoteName());
        ps.setString(3, beanDefinition.getVersion());
        ResultSet resultSet = ps.executeQuery();
        while (resultSet.next()){
            is = resultSet.getBlob(CLASS_BYTE_COLUMN_NAME).getBinaryStream();
        }
        return is;
    }


    private InputStream query(Connection connection, RemoteBeanDefinition beanDefinition) throws Exception{
        InputStream is = null;
        PreparedStatement ps = connection.prepareStatement(QUERY_DEFAULT);
        ps.setString(1, TABLE_NAME_DEAFULT);
        ps.setString(2, beanDefinition.getRemoteName());
        ResultSet resultSet = ps.executeQuery();
        while (resultSet.next()){
            is = resultSet.getBlob(CLASS_BYTE_COLUMN_NAME).getBinaryStream();
        }
        return is;
    }

    private byte[] doLoadByte(InputStream is){
        ByteArrayOutputStream baos = null;
        baos = new ByteArrayOutputStream();
        byte[] bytes = new byte[1024];
        int length = 0;
        try {
            while ((length = is.read(bytes)) != -1) {
                baos.write(bytes,0,length);
            }
        }catch (IOException e){
            throw new RuntimeException(e);
        }
        return baos.toByteArray();
    }


    public Connection getConn(){
        String driver = dbConfiguer.getDriver();
        String url = dbConfiguer.getHost();
        String username = dbConfiguer.getUsername();
        String password = dbConfiguer.getPassword();

        Connection conn = null;
        try {
            Class.forName(driver);
            conn = (Connection) DriverManager.getConnection(url, username, password);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
        return conn;
    }

}
