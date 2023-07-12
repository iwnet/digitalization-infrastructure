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

import java.sql.*;
import java.io.Serializable;
import java.lang.UnsupportedOperationException;
import java.util.LinkedList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlRootElement;

@XmlRootElement(name = "DbInfo")
@XmlAccessorType(XmlAccessType.PUBLIC_MEMBER)
public class DbInfo implements Serializable {

    private transient int id;
    private String slug;
    private String name;
    private String description;
    private String dbname;
    private transient String dtDbname;
    private transient String elDbname;
    private transient String kpiDbname;

    private final static String GET_DBINFO_QUERY =
            "SELECT id, slug, name, description, dbname " +
                    "FROM db_info;";

    private final static String GET_DBINFO_BY_ID_QUERY =
        "SELECT id, slug, name, description, dbname " +
        "FROM db_info " +
        "WHERE id = ?;";

    private final static String GET_DBINFO_BY_SLUG_QUERY =
        "SELECT id, slug, name, description, dbname " +
        "FROM db_info " +
        "WHERE slug = ?;";

    private final static String INSERT_DBINFO_QUERY =
        "INSERT INTO db_info (slug, name, description, dbname) " +
        "VALUES (?, ?, ?, ?) " +
        "RETURNING id;";

    private final static String DELETE_DBINFO_QUERY =
            "DELETE FROM db_info WHERE id = ?;";

    private boolean exists = false;

    public DbInfo() { }

    public DbInfo(String slug, String name, String description, String dbname) {
        this.slug = slug;
        this.name = name;
        this.description = description;
        this.dbname = dbname;
        this.dtDbname = dbname + "_dt";
        this.elDbname = dbname + "_el";
        this.kpiDbname = dbname + "_kpi";
    }

    public int getId() {
        return id;
    }

    public String getSlug() {
        return this.slug;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public String getDbname() {
        return this.dbname;
    }

    public void setSlug(String slug) {
        this.slug = slug;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setDbname(String dbname) {
        this.dbname = dbname;
    }

    public String getDtDbname() { return dtDbname; }

    public void setDtDbname(String dtDbname) { this.dtDbname = dtDbname; }

    public String getElDbname() { return elDbname; }

    public void setElDbname(String elDbname) { this.elDbname = elDbname; }

    public String getKpiDbname() { return kpiDbname; }

    public void setKpiDbname(String kpiDbname) { this.kpiDbname = kpiDbname; }

    @Override
    public String toString() {
        return "DbInfo {" +
                "slug='" + slug + "', " +
                "name='" + name + "', " +
                "description='" + description + "', " +
                "dbname='" + dbname + "' " +
                "}";
    }

    public void save() throws SQLException, UnsupportedOperationException, SystemConnectorException {
        if (!this.exists) {
            // The object does not exist, it should be inserted.
            PostgresqlConnector connector = (PostgresqlConnector ) SystemConnector.getInstance().getBDAconnector();
            Connection connection = connector.getConnection();

            try {
                PreparedStatement statement = connection.prepareStatement(INSERT_DBINFO_QUERY);

                statement.setString(1, this.slug);
                statement.setString(2, this.name);
                statement.setString(3, this.description);
                statement.setString(4, this.dbname);

                ResultSet resultSet = statement.executeQuery();

                connection.commit();

                if (resultSet.next()) {
                    this.id = resultSet.getInt("id");
                    this.dtDbname = dbname + "_dt";
                    this.elDbname = dbname + "_el";
                    this.kpiDbname = dbname + "_kpi";
                    this.exists = true;
                }
            } catch (SQLException e) {
                e.printStackTrace();
                connection.rollback();
                throw new SQLException("Failed to insert DbInfo object.");
            }
        } else {
            // The object exists, it should be updated.
            throw new UnsupportedOperationException("Operation not implemented.");
        }
    }

    public static void destroy(int id) throws SQLException, UnsupportedOperationException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector ) SystemConnector.getInstance().getBDAconnector();
        Connection connection = connector.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(DELETE_DBINFO_QUERY);
            statement.setInt(1, id);

            statement.executeUpdate();
            connection.commit();
            return;
        } catch (SQLException e) {
            e.printStackTrace();
            connection.rollback();
        }

        throw new SQLException("DbInfo object not found.");
    }

    public static DbInfo getDbInfoById(int id) throws SQLException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector ) SystemConnector.getInstance().getBDAconnector();
        Connection connection = connector.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(GET_DBINFO_BY_ID_QUERY);
            statement.setInt(1, id);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                DbInfo info = new DbInfo(
                    resultSet.getString("slug"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("dbname")
                );

                info.id = resultSet.getInt("id");
                info.exists = true;

                return info;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new SQLException("DbInfo object not found.");
    }

    public static DbInfo getDbInfoBySlug(String slug) throws SQLException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector ) SystemConnector.getInstance().getBDAconnector();
        Connection connection = connector.getConnection();

        try {
            PreparedStatement statement = connection.prepareStatement(GET_DBINFO_BY_SLUG_QUERY);
            statement.setString(1, slug);

            ResultSet resultSet = statement.executeQuery();

            if (resultSet.next()) {
                DbInfo info = new DbInfo(
                    resultSet.getString("slug"),
                    resultSet.getString("name"),
                    resultSet.getString("description"),
                    resultSet.getString("dbname")
                );

                info.id = resultSet.getInt("id");
                info.exists = true;

                return info;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new SQLException("DbInfo object not found.");
    }

    public static List<DbInfo> getDbInfo() throws SQLException, SystemConnectorException {
        PostgresqlConnector connector = (PostgresqlConnector ) SystemConnector.getInstance().getBDAconnector();
        Connection connection = connector.getConnection();

        List<DbInfo> infos = new LinkedList<>();
        try {
            PreparedStatement statement = connection.prepareStatement(GET_DBINFO_QUERY);
            ResultSet resultSet = statement.executeQuery();

            while (resultSet.next()) {
                DbInfo info = new DbInfo(
                        resultSet.getString("slug"),
                        resultSet.getString("name"),
                        resultSet.getString("description"),
                        resultSet.getString("dbname")
                );

                info.id = resultSet.getInt("id");
                info.exists = true;
                infos.add(info);
            }

            return infos;
        } catch (SQLException e) {
            e.printStackTrace();
        }

        throw new SQLException("Failed to retrieve Db info.");
    }

}
