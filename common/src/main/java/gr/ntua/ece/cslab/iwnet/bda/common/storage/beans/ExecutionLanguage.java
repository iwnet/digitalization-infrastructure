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
import java.util.logging.Level;
import java.util.logging.Logger;

@XmlRootElement(name = "ExecutionLanguage")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class ExecutionLanguage implements Serializable {
    private final static Logger LOGGER = Logger.getLogger(ExecutionLanguage.class.getCanonicalName());

    private int id;
    private String name;

    private static final String INSERT_LANGUAGE =
        "INSERT INTO execution_languages (name) VALUES (?) RETURNING id;";

    // Query to fetch all languages from db
    private static final String GET_LANGUAGES = "SELECT * FROM execution_languages;";

    // Query to fetch specific language from its id
    private static final String GET_LANGUAGE_BY_ID = "SELECT * FROM execution_languages WHERE id = ?;";


    // Empty constructor
    public ExecutionLanguage() {}

    public ExecutionLanguage(String name) {
        this.name = name;
    }

    // Getters and Setters
    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String toString() {
        return "ExecutionLanguages{" +
                "id=" + id +
                ", name='" + name + '\'' +
                '}';
    }

    public void save() throws SystemConnectorException, SQLException {
        PostgresqlConnector connector = (PostgresqlConnector )
                SystemConnector.getInstance().getBDAconnector();

        Connection connection = connector.getConnection();

        PreparedStatement statement = connection.prepareStatement(INSERT_LANGUAGE);

        statement.setString(1, this.name);

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

        LOGGER.log(Level.INFO, "SUCCESS: Insert Into execution languages. ID: "+this.id);
    }

    public static List<ExecutionLanguage> getLanguages() throws SQLException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector ) SystemConnector.getInstance().getBDAconnector();
        Connection connection = connector.getConnection();

        List<ExecutionLanguage> executionLanguages = new LinkedList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(GET_LANGUAGES);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                ExecutionLanguage language = new ExecutionLanguage(
                        resultSet.getString("name")
                );

                language.id = resultSet.getInt("id");
                executionLanguages.add(language);
            }

            return executionLanguages;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new SQLException("Failed to retrieve ExecutionLanguage info.");
    }

    public static ExecutionLanguage getLanguageById(int id) throws SQLException, SystemConnectorException {

        PostgresqlConnector connector = (PostgresqlConnector ) SystemConnector.getInstance().getBDAconnector();
        Connection connection = connector.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(GET_LANGUAGE_BY_ID);
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                ExecutionLanguage language = new ExecutionLanguage(
                        resultSet.getString("name")
                );

                language.id = resultSet.getInt("id");

                return language;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new SQLException("ExecutionLanguage object not found.");

    }
}
