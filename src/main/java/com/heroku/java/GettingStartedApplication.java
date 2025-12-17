package com.heroku.java;
import java.util.Random;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

import javax.sql.DataSource;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.Map;

@SpringBootApplication
@Controller
public class GettingStartedApplication {
    private final DataSource dataSource;

    @Autowired
    public GettingStartedApplication(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @GetMapping("/")
    public String index() {
        return "index";
    }

    String getRandomString() {
	    int leftLimit = 48; // '0'
int rightLimit = 122; // 'z'
Random random = new Random();

return random.ints(leftLimit, rightLimit + 1)
.filter(i -> (i <= 57 || i >= 65) && (i <= 90 || i >= 97)) // Alphanumeric filter
.limit(10)
.collect(StringBuilder::new, StringBuilder::appendCodePoint, StringBuilder::append)
.toString();

}

    @GetMapping("/database")
    String database(Map<String, Object> model) {
    try (Connection connection = dataSource.getConnection()) {
        final var statement = connection.createStatement();
        // Create table if it doesn't exist and insert a record with timestamp and random string
        statement.executeUpdate("CREATE TABLE IF NOT EXISTS table_timestamp_and_random_string (tick timestamp, random_string varchar(50))");

        // Generate random string and insert into the table
        String rs = getRandomString();
        statement.executeUpdate("INSERT INTO table_timestamp_and_random_string VALUES (now(), '" + rs + "')");

        // Retrieve both tick and random_string from the database
        final var resultSet = statement.executeQuery("SELECT tick, random_string FROM table_timestamp_and_random_string");
        final var output = new ArrayList<String>();

        // Iterate through the result set
        while (resultSet.next()) {
            // Retrieve both the tick and random_string for each row
            String tick = resultSet.getTimestamp("tick").toString();
            String randomString = resultSet.getString("random_string");

            // Add the result to the output list with both the tick and random string
            output.add("Read from DB: " + tick + " " + randomString);
        }

        // Put the output into the model to display it
        model.put("records", output);
        return "database";

    } catch (Throwable t) {
        model.put("message", t.getMessage());
        return "error";
    }
}


    @GetMapping("/databaseOld")
    String databaseOld(Map<String, Object> model) {
        try (Connection connection = dataSource.getConnection()) {
            final var statement = connection.createStatement();
            statement.executeUpdate("CREATE TABLE IF NOT EXISTS ticks (tick timestamp)");
            statement.executeUpdate("INSERT INTO ticks VALUES (now())");

            final var resultSet = statement.executeQuery("SELECT tick FROM ticks");
            final var output = new ArrayList<>();
            while (resultSet.next()) {
                output.add("Read from DB: " + resultSet.getTimestamp("tick"));
            }

            model.put("records", output);
            return "database";

        } catch (Throwable t) {
            model.put("message", t.getMessage());
            return "error";
        }
    }


    public static void main(String[] args) {
        SpringApplication.run(GettingStartedApplication.class, args);
    }
}
