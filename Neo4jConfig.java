package com.mycompany.alonlibrarysystem;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Proxy;
import java.util.concurrent.CompletableFuture;
import org.neo4j.driver.AuthTokens;
import org.neo4j.driver.Driver;
import org.neo4j.driver.GraphDatabase;

public class Neo4jConfig {

    private static final String URI = "neo4j://127.0.0.1:7687";
    private static final String USER = "neo4j";
    private static final String PASSWORD = "Bogart123";

    private static Driver realDriver;
    private static Driver safeDriver;

    static {
        // The real driver closes only when the app exits
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            if (realDriver != null) realDriver.close();
        }));
    }

    public static synchronized Driver getDriver() {
        if (safeDriver == null) {
            realDriver = GraphDatabase.driver(URI, AuthTokens.basic(USER, PASSWORD));
            safeDriver = (Driver) Proxy.newProxyInstance(
                Driver.class.getClassLoader(),
                new Class<?>[]{Driver.class},
                (proxy, method, args) -> {
                    // Ignore close() calls from other classes
                    if (method.getName().equals("close")) return null;
                    if (method.getName().equals("closeAsync"))
                        return CompletableFuture.completedFuture(null);
                    try {
                        return method.invoke(realDriver, args);
                    } catch (InvocationTargetException e) {
                        throw e.getCause();
                    }
                });
        }
        return safeDriver;
    }
}