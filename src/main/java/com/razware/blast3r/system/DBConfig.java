package com.razware.blast3r.system;

import com.esotericsoftware.minlog.Log;
import com.google.gson.annotations.Expose;
import com.razware.blast3r.Main;
import org.kohsuke.args4j.Option;

public class DBConfig {
    private String jdbcUrl = "jdbc:";
    @Expose
    @Option(name = "--mysql-db", aliases = {"-mdb"}, usage = "The MySql database name", depends = "--mysql")
    private String mySqlDbName = "blast3r";
    @Expose
    @Option(name = "--mysql-user", aliases = {"-mu"}, usage = "The MySql user", depends = "--mysql")
    private String mySqlUser;
    @Expose
    @Option(name = "--mysql-password", aliases = {"-mp"}, usage = "The MySql password", depends = {"--mysql"})
    private String mySqlPassword;
    @Expose
    @Option(name = "--mysql", aliases = {"-my"}, usage = "If the database is mysql", forbids = {"--sqlite-db"}, depends = {"--mysql-user", "--mysql-password"})
    private boolean mysql = false;
    @Expose
    @Option(name = "--sqlite-db", aliases = {"-sdb"}, usage = "The location of the sqlite database file", forbids = "--mysql")
    private String sqliteDb = Main.NAME + ".sqlite";
    @Expose
    @Option(name = "--mysql-port", aliases = {"-mport"}, usage = "The remote database port", depends = "--mysql")
    private int dbPort = 3306;
    @Expose
    @Option(name = "--mysql-host", aliases = {"-mhost"}, usage = "The remote database host", depends = "--mysql")
    private String dbHost = "localhost";

    public String getDbHost() {
        return dbHost;
    }

    public void setDbHost(String dbHost) {
        this.dbHost = dbHost;
    }

    public int getDbPort() {
        return dbPort;
    }

    public void setDbPort(int dbPort) {
        this.dbPort = dbPort;
    }

    public String getSqliteDb() {
        return sqliteDb;
    }

    public void setSqliteDb(String sqliteDb) {
        this.sqliteDb = sqliteDb;
    }

    public boolean isMysql() {
        return mysql;
    }

    public void setMysql(boolean mysql) {
        this.mysql = mysql;
    }

    public String getMySqlDbName() {
        return mySqlDbName;
    }

    public void setMySqlDbName(String mySqlDbName) {
        this.mySqlDbName = mySqlDbName;
    }

    public String getMySqlUser() {
        return mySqlUser;
    }

    public void setMySqlUser(String mySqlUser) {
        this.mySqlUser = mySqlUser;
    }

    public String getMySqlPassword() {
        return mySqlPassword;
    }

    public void setMySqlPassword(String mySqlPassword) {
        this.mySqlPassword = mySqlPassword;
    }

    public String getJdbcUrl() {
        return jdbcUrl;
    }

    public void init() {
        if (isMysql()) {
            this.jdbcUrl = jdbcUrl.concat("mysql//" + this.getDbHost() + ":" + this.getDbPort() + "/" + this.getMySqlDbName() + "?user=" + this.getMySqlUser() + "&password=" + this.getMySqlPassword());
        } else {
            this.jdbcUrl = jdbcUrl.concat("sqlite:" + this.getSqliteDb());
        }
        Log.debug(this.jdbcUrl);
    }
}
