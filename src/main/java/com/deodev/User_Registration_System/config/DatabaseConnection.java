package com.deodev.User_Registration_System.config;


import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import jakarta.annotation.PostConstruct;
import javax.sql.DataSource;
import java.sql.Connection;

@Component
@RequiredArgsConstructor
public class DatabaseConnection {

    private final DataSource dataSource;

    @PostConstruct
    public void checkDatabaseConnection() throws Exception {
        try(Connection connection = dataSource.getConnection()) {
            System.out.println("Connected to DB: "+ connection.getMetaData().getURL());
        } catch (Exception ex) {
            System.out.println("Failed to connect to DB"+ ex.getMessage());
            throw ex;
        }
    }

}
