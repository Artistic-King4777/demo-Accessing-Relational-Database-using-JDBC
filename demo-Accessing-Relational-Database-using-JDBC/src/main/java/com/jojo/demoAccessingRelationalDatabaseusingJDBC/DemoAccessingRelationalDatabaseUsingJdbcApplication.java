package com.jojo.demoAccessingRelationalDatabaseusingJDBC;

import hello.Customer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

// @SpringBootApplication:
//   -- a convenience annotation that adds:
//       ++ @Configuration -- tags the class as a \source of bean
//            definitions for the application context
//       ++ @EnableAutoConfiguration -- tells SpringBoot to start adding
//            beans based on classpath settings, other beans and various
//            property settings
//       ++ @ComponentScan -- tells Spring to look for other components,
//            configurations, and services in the "hello" package. In this case
//            there aren't any.
@SpringBootApplication
public class DemoAccessingRelationalDatabaseUsingJdbcApplication implements CommandLineRunner {

    // JDBC Template makes it easy to use SQL Relational Databases
    //   Most of it is mired in resources, managing connections, exception handling
    //   and general error checking unrelated to what the code is trying to do
    //   The JdbcTemplate does that all for you so all you gotta do is focus on the task at hand

    private static final Logger log = LoggerFactory.getLogger(DemoAccessingRelationalDatabaseUsingJdbcApplication.class);

    // The main() method uses Spring Boot's SpringApplication.run() method to
    //   launch an application. Did you notice that there wasn't a single line
    //   of XML? This web app is 100% pure Java and you didnt have to deal with
    //   configuring any plumbing or infrastructure
    //   It helps you write to the database without the database
    //   Can log things out in the console
	public static void main(String[] args) {
		SpringApplication.run(DemoAccessingRelationalDatabaseUsingJdbcApplication.class, args);
	}

	// Spring Boot supports H2 which is an in-memory relational database engine
    //   that auto creates a connection. Because we're using *spring-jdbc*
    //   Spring Boot auto creates a "JDBC Template"
    //   The @Autowired - JdbcTemplate field auto loads it to make it
    //   available for use
    @Autowired
    private JdbcTemplate jdbcTemplate;

	// This Application class implements Spring Boot’s CommandLineRunner,
    // which means it will execute the run() method after the application
    // context is loaded up.
    @Override
    public void run(String... strings) throws Exception {

        // loggers
        log.info("Creating tables");

        // executing SQL CODE using the JDBC using the execute method
        jdbcTemplate.execute("DROP TABLE customers IF EXISTS");
        jdbcTemplate.execute("CREATE TABLE customers(" +
                "id SERIAL, first_name VARCHAR(255), last_name VARCHAR(255))");

        // Take a list of strings and using Java 8 streams:
        //   Split up the array of whole names into an array of first/last names
        List<Object[]> splitUpNames = Arrays.asList("John Woo", "Jeff Dean", "Josh Bloch", "Josh Long").stream()
                .map(name -> name.split(" "))
                .collect(Collectors.toList());

        // Use a Java 8 stream to print out each tuple of the list
        splitUpNames.forEach(name -> log.info(String.format("Inserting customer record for %s %s", name[0], name[1])));

        // Logging and adding names to database
        // installing new records in your newly created table using:
        //   JdbcTemplate's batchUpdate() operation to bulk load data
        //   The first argument to the method call is the query string, the
        //   last argument (the array of "Object"s holds the variables that will be
        //   substituted into the query where the "?" characters are
        jdbcTemplate.batchUpdate("INSERT INTO customers(first_name, last_name) VALUES (?,?)", splitUpNames);

        // TODO - notes in green in the site
        // For For single insert statements, JdbcTemplate’s `insert method is good.
        //   But for multiple inserts, it’s better to use batchUpdate
        //   Use ? for arguments to avoid SQL injection attacks by
        //     instructing JDBC to bind variables.



        // Finally you use the "query" method to search your table for records
        //   matching the criteria. You again use the "?" arguments to create
        //   parameters for the query, passing in the actual values when you make
        //   a call.
        //   The last argument is a Java 8 lambda used to convert each result row
        //   into a new "Customer" object
        log.info("Querying for customer records where first_name = 'Josh':");
        jdbcTemplate.query(
                "SELECT id, first_name, last_name FROM customers WHERE first_name = ?", new Object[] { "Josh" },
                (rs, rowNum) -> new Customer(rs.getLong("id"), rs.getString("first_name"), rs.getString("last_name"))
        ).forEach(customer -> log.info(customer.toString()));

        // TODO - notes in green in the site
        // Java 8 lambdas map nicely onto single method interfaces, like
        //   Spring’s RowMapper. If you are using Java 7 or earlier, you can
        //   easily plug in an anonymous interface implementation and have the
        //   same method body as the lambda expression’s body contains, and it
        //   will work with no fuss from Spring.
    }



}

