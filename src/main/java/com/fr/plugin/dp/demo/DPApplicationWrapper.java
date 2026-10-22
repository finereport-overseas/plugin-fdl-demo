package com.fr.plugin.dp.demo;

import com.fr.dp.start.DPApplication;

public class DPApplicationWrapper {

    public static void main(String[] args) {
        System.setProperty("org.springframework.boot.logging.LoggingSystem",
                "org.springframework.boot.logging.log4j2.Log4J2LoggingSystem");
        DPApplication.main(args);
    }
}
