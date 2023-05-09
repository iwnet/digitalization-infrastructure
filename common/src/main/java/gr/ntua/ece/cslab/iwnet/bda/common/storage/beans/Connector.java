/*
 * Copyright 2022 ICCS
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package gr.ntua.ece.cslab.iwnet.bda.common.storage.beans;

import com.google.gson.Gson;
import com.google.gson.JsonParser;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnector;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnectorException;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.PostgresqlConnector;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.Serializable;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedList;
import java.util.List;

@XmlRootElement(name = "Connector")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class Connector implements Serializable {
    private transient int id;
    private String name;
    private String address;
    private Integer port;
    private ConnectorMetadata metadata;
    private boolean external;

    private final static String INSERT_CONNECTOR_QUERY =
            "INSERT INTO connectors (name, address, port, metadata, is_external) " +
            "VALUES (?, ?, ?, ?::json, ?) " +
            "RETURNING id;";

    private final static String GET_CONNECTOR_BY_ID_QUERY =
            "SELECT id, name, address, port, metadata, is_external " +
            "FROM connectors " +
            "WHERE id = ?;";

    private final static String GET_CONNECTOR_METADATA_BY_ID_QUERY =
            "SELECT metadata " +
            "FROM connectors " +
            "WHERE id = ?;";

    private final static String GET_CONNECTOR_QUERY =
            "SELECT id, name, address, port, metadata, is_external " +
            "FROM connectors;";

    private final static String DELETE_CONNECTOR_QUERY =
            "DELETE FROM connectors WHERE id = ?;";

    private boolean exists = false;

    public Connector() { }

    public Connector(String name, String address, Integer port, ConnectorMetadata metadata, boolean external) {
        this.name = name;
        this.address = address;
        this.port = port;
        this.metadata = metadata;
        this.external = external;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public ConnectorMetadata getMetadata() {
        return metadata;
    }

    public void setMetadata(ConnectorMetadata metadata) {
        this.metadata = metadata;
    }

    public boolean isExternal() {
        return external;
    }

    public void setExternal(boolean external) {
        this.external = external;
    }

    @Override
    public String toString() {
        return "Connector{" +
                "name='" + name + '\'' +
                ", address='" + address + '\'' +
                ", port=" + port +
                ", metadata='" + metadata + '\'' +
                ", isExternal=" + external +
                '}';
    }

    public void save() throws SQLException, UnsupportedOperationException, SystemConnectorException {
        if (!this.exists) {
            // The object does not exist, it should be inserted.
            PostgresqlConnector connector = (PostgresqlConnector ) SystemConnector.getInstance().getBDAconnector();
            Connection connection = connector.getConnection();

            try {
                PreparedStatement statement = connection.prepareStatement(INSERT_CONNECTOR_QUERY);

                statement.setString(1, this.name);
                statement.setString(2, this.address);
                statement.setInt(3, this.port);
                statement.setString(4, new Gson().toJson(this.metadata));
                statement.setBoolean(5, this.external);

                ResultSet resultSet = statement.executeQuery();

                connection.commit();

                if (resultSet.next()) {
                    this.id = resultSet.getInt("id");
                    this.exists = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                throw new SQLException("Failed to insert Connector object.");
            }
        } else {
            // The object exists, it should be updated.
            throw new UnsupportedOperationException("Operation not implemented.");
        }
    }

    public static Connector getConnectorInfoById(int id) throws SQLException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector ) SystemConnector.getInstance().getBDAconnector();
        Connection connection = connector.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(GET_CONNECTOR_BY_ID_QUERY);
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                Connector conn = new Connector(
                        resultSet.getString("name"),
                        resultSet.getString("address"),
                        resultSet.getInt("port"),
                        new Gson().fromJson(new JsonParser().parse(resultSet.getString("metadata")).getAsJsonObject(), ConnectorMetadata.class),
                        resultSet.getBoolean("is_external")
                );

                conn.id = resultSet.getInt("id");
                conn.exists = true;

                return conn;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new SQLException("Connector object not found.");
    }


    public static List<Connector> getConnectors() throws SQLException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector ) SystemConnector.getInstance().getBDAconnector();
        Connection connection = connector.getConnection();

        List<Connector> connectors = new LinkedList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(GET_CONNECTOR_QUERY);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                Connector conn = new Connector(
                        resultSet.getString("name"),
                        resultSet.getString("address"),
                        resultSet.getInt("port"),
                        new Gson().fromJson(new JsonParser().parse(resultSet.getString("metadata")).getAsJsonObject(), ConnectorMetadata.class),
                        resultSet.getBoolean("is_external")
                );

                conn.id = resultSet.getInt("id");
                conn.exists = true;
                connectors.add(conn);
            }

            return connectors;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new SQLException("Failed to retrieve Connectors info.");
    }

    public static void destroy(int id) throws SQLException, UnsupportedOperationException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector ) SystemConnector.getInstance().getBDAconnector();
        Connection connection = connector.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(DELETE_CONNECTOR_QUERY);
            statement.setInt(1, id);

            statement.executeUpdate();
            connection.commit();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            connection.rollback();
        }

        throw new SQLException("Connector object not found.");
    }

}
