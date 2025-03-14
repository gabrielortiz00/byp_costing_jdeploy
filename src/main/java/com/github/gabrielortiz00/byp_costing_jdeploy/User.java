package com.github.gabrielortiz00.byp_costing_jdeploy;

import com.github.gabrielortiz00.byp_costing_jdeploy.utils.HashUtil;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.Objects;

public class User {

    private final String username;
    private final String plainTextPassword;

    public User(String username, String plainTextPassword) {
        this.username = username;
        this.plainTextPassword = plainTextPassword;
    }

    //validates credentials calling HashUtil hash method
    public boolean validateCredentials() {
        try {
            String hashedInput = HashUtil.hashSHA256(plainTextPassword);

            URL credentialsUrl = getClass().getResource("/com.github.gabrielortiz00.byp_costing_jdeploy/files/credentials.csv");

            if (credentialsUrl == null) {
                System.out.println("Could not find credentials.csv.");
                return false;
            }

            try (BufferedReader br = new BufferedReader(new InputStreamReader(credentialsUrl.openStream()))) {

                String line;
                while ((line = br.readLine()) != null) {
                    if (line.trim().isEmpty() || line.startsWith("#")) {
                        continue;
                    }
                    String[] parts = line.split(",");
                    if (parts.length == 2) {
                        String fileUsername = parts[0].trim();
                        String fileHashedPassword = parts[1].trim();

                        if (Objects.equals(username, fileUsername)) {
                            return hashedInput.equalsIgnoreCase(fileHashedPassword);
                        }
                    }
                }
            }
            return false;

        } catch (Exception e) {
            e.printStackTrace();
            return false;
        }
    }
}