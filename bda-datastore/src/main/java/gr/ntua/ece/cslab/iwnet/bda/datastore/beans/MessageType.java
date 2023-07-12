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

package gr.ntua.ece.cslab.iwnet.bda.datastore.beans;

import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnector;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.SystemConnectorException;
import gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors.PostgresqlConnector;

import java.io.Serializable;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Vector;
import java.util.logging.Level;
import java.util.logging.Logger;

import com.google.gson.JsonParser;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "MessageType")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class MessageType implements Serializable {
    private final static Logger LOGGER = Logger.getLogger(MessageType.class.getCanonicalName());
    private final static int DEFAULT_VECTOR_SIZE = 10;

    private transient Integer id;
    private String name;
    private String description;
    private boolean active;
    private String format;

    private boolean exists = false;

    public MessageType() { }

    public MessageType(String name, String description, boolean active, String format) {
        this.name = name;
        this.description = description;
        this.active = active;
        this.format = format;
    }

    public Integer getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public boolean isActive() {
        return active;
    }

    public void setActive(boolean active) {
        this.active = active;
    }

    public String getFormat() { return format; }

    public void setFormat(String format) {
        this.format = format;
    }

    public List<String> getMessageColumns() {
        List<String> columns = new ArrayList<>();
        columns.addAll(new JsonParser().parse(this.format).getAsJsonObject().keySet());
        return columns;
    }

    @Override
    public String toString() {
        return "MessageType{" +
                "name='" + name + '\'' +
                ", description='" + description + '\'' +
                ", active=" + active + '\'' +
                ", format='" + format +
                '}';
    }

    private final static String CREATE_MESSAGE_TYPES_TABLE_QUERY =
            "CREATE TABLE metadata.message_type ( " +
                    "id                    SERIAL PRIMARY KEY, " +
                    "name                  VARCHAR(64) NOT NULL UNIQUE, " +
                    "description           VARCHAR(256), " +
                    "active                BOOLEAN DEFAULT(true), " +
                    "format                VARCHAR" +
                    ");";

    private final static String MESSAGE_TYPES_QUERY =
            "SELECT id, name, description, active, format " +
                    "FROM metadata.message_type;";

    private final static String ACTIVE_MESSAGE_TYPES_QUERY =
            "SELECT id, name, description, active, format " +
                    "FROM metadata.message_type " +
                    "WHERE active = true;";

    private final static String ACTIVE_MESSAGE_NAMES_QUERY =
            "SELECT name " +
                    "FROM metadata.message_type " +
                    "WHERE active = true;";

    private final static String GET_MESSAGE_BY_NAME_QUERY =
            "SELECT id, name, description, active, format " +
                    "FROM metadata.message_type " +
                    "WHERE name = ?;";

    private final static String GET_MESSAGE_BY_ID_QUERY =
            "SELECT * " +
                    "FROM metadata.message_type " +
                    "WHERE id = ?;";

    private final static String INSERT_MESSAGE_QUERY =
            "INSERT INTO metadata.message_type (name,description,active,format) " +
                    "VALUES (?, ?, ?, ?) " +
                    "RETURNING id;";

    private final static String UPDATE_MESSAGE_QUERY =
            "UPDATE metadata.message_type " +
                    "SET name = ?, description = ?, active = ?, format = ? " +
                    "WHERE id = ?";

    private final static String DELETE_MESSAGE_QUERY =
            "DELETE FROM metadata.message_type WHERE id = ?;";

    public static List<MessageType> getMessageTypes(String slug) throws SQLException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector )
                SystemConnector.getInstance().getDTconnector(slug);

        Connection connection = connector.getConnection();

        Vector<MessageType> messageTypes = new Vector<MessageType>(DEFAULT_VECTOR_SIZE);

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(MESSAGE_TYPES_QUERY);

            while (resultSet.next()) {
                MessageType messageType = new MessageType(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getBoolean("active"),
                        resultSet.getString("format")
                );
                messageType.exists = true;
                messageType.id = resultSet.getInt("id");

                messageTypes.addElement(messageType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return messageTypes;
    }

    public static List<MessageType> getActiveMessageTypes(String slug) throws SQLException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector )
                SystemConnector.getInstance().getDTconnector(slug);

        Connection connection = connector.getConnection();

        Vector<MessageType> messageTypes = new Vector<MessageType>(DEFAULT_VECTOR_SIZE);

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(ACTIVE_MESSAGE_TYPES_QUERY);

            while (resultSet.next()) {
                MessageType messageType = new MessageType(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getBoolean("active"),
                        resultSet.getString("format")
                );
                messageType.exists = true;
                messageType.id = resultSet.getInt("id");

                messageTypes.addElement(messageType);
            }
        } catch (SQLException e) {
            e.printStackTrace();
            throw e;
        }

        return messageTypes;
    }

    public static List<String> getActiveMessageTypeNames(String slug) throws SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector) SystemConnector.getInstance().getDTconnector(slug);
        Connection connection = connector.getConnection();

        Vector<String> messageTypeNames = new Vector<String>(10);

        try {
            Statement statement = connection.createStatement();
            ResultSet resultSet = statement.executeQuery(ACTIVE_MESSAGE_NAMES_QUERY);

            while (resultSet.next()) {
                messageTypeNames.addElement(resultSet.getString("name"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return messageTypeNames;
    }

    public static MessageType getMessageByName(String slug, String name) throws SQLException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector) SystemConnector.getInstance().getDTconnector(slug);
        Connection connection = connector.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(GET_MESSAGE_BY_NAME_QUERY);
            statement.setString(1, name);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                MessageType msg = new MessageType(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getBoolean("active"),
                        resultSet.getString("format")
                );
                msg.exists = true;
                msg.id = resultSet.getInt("id");

                return msg;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new SQLException("MessageType object not found.");
    }

    public static MessageType getMessageById(String slug, int id) throws SQLException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector) SystemConnector.getInstance().getDTconnector(slug);
        Connection connection = connector.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(GET_MESSAGE_BY_ID_QUERY);
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                MessageType msg = new MessageType(
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getBoolean("active"),
                        resultSet.getString("format")
                );
                msg.exists = true;
                msg.id = resultSet.getInt("id");

                return msg;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new SQLException("MessageType object not found.");
    }

    public void save(String slug) throws SQLException, SystemConnectorException {
        if (!this.exists) {
            PostgresqlConnector connector = (PostgresqlConnector)
                    SystemConnector.getInstance().getDTconnector(slug);

            Connection connection = connector.getConnection();

            PreparedStatement statement = connection.prepareStatement(INSERT_MESSAGE_QUERY);

            statement.setString(1, this.name);
            statement.setString(2, this.description);
            statement.setBoolean(3, this.active);
            statement.setString(4, this.format);

            try {
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    this.id = resultSet.getInt("id");

                }

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }
        else {
            PostgresqlConnector connector = (PostgresqlConnector)
                    SystemConnector.getInstance().getDTconnector(slug);

            Connection connection = connector.getConnection();

            PreparedStatement statement = connection.prepareStatement(UPDATE_MESSAGE_QUERY);

            statement.setString(1, this.name);
            statement.setString(2, this.description);
            statement.setBoolean(3, this.active);
            statement.setString(4, this.format);

            statement.setInt(5, Integer.valueOf(this.id));

            try {
                statement.executeUpdate();

                connection.commit();
            } catch (SQLException e) {
                connection.rollback();
                throw e;
            }
        }

        LOGGER.log(Level.INFO, "SUCCESS: Insert Into message_type. ID: "+this.id);
    }

    public static void destroy(String slug, int id) throws SQLException, UnsupportedOperationException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector )
                SystemConnector.getInstance().getDTconnector(slug);
        Connection connection = connector.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(DELETE_MESSAGE_QUERY);
            statement.setInt(1, id);

            statement.executeUpdate();
            connection.commit();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            connection.rollback();
        }

        throw new SQLException("MessageType object not found.");
    }

    public static void createTable(String slug) throws SQLException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector )
                SystemConnector.getInstance().getDTconnector(slug);

        Connection connection = connector.getConnection();

        PreparedStatement statement = connection.prepareStatement(CREATE_MESSAGE_TYPES_TABLE_QUERY);

        try {
            statement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            connection.rollback();
            throw e;
        }

        LOGGER.log(Level.INFO, "SUCCESS: Create message_type table in metadata schema.");
    }
}

