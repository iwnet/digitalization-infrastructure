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

package gr.ntua.ece.cslab.iwnet.bda.common.storage.connectors;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.sql.Connection;
import java.sql.SQLException;


public class PostgresqlPooledDataSource {

    private static PostgresqlPooledDataSource dataSource = null;

    private static HikariConfig bdaDataSourceConfig = null;
    private static HikariConfig labDataSourceConfig = null;

    private HikariDataSource bdaPooledDataSource = null;
    private HikariDataSource labPooledDataSource = null;

    private PostgresqlPooledDataSource() {
        this.bdaPooledDataSource = new HikariDataSource(bdaDataSourceConfig);
        this.labPooledDataSource = new HikariDataSource(labDataSourceConfig);
    }

    public static void init(String bdaJdbcURL, String labJdbcURL, String username, String password) {
        bdaDataSourceConfig = new HikariConfig();

        bdaDataSourceConfig.setJdbcUrl(bdaJdbcURL);
        bdaDataSourceConfig.setUsername(username);
        bdaDataSourceConfig.setPassword(password);

        labDataSourceConfig = new HikariConfig();

        labDataSourceConfig.setJdbcUrl(labJdbcURL);
        labDataSourceConfig.setUsername(username);
        labDataSourceConfig.setPassword(password);
    }

    public static PostgresqlPooledDataSource getInstance() {
        if (dataSource == null) {
            dataSource = new PostgresqlPooledDataSource();
        }

        return dataSource;
    }

    public Connection getBdaConnection() {
        Connection connection = null;

        try {
            connection = this.bdaPooledDataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }

    public Connection getLabConnection() {
        Connection connection = null;

        try {
            connection = this.labPooledDataSource.getConnection();
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return connection;
    }
}
