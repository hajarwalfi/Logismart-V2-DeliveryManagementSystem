package com.logismart.security.util;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;

/**
 * Utility to generate BCrypt password hashes for database initialization
 * Run this class to generate hashes for user passwords
 */
public class PasswordHashGenerator {

    public static void main(String[] args) {
        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        System.out.println("=== BCrypt Password Hashes ===\n");

        // Generate hash for manager123
        String managerHash = encoder.encode("manager123");
        System.out.println("Password: manager123");
        System.out.println("Hash: " + managerHash);
        System.out.println();

        // Generate hash for livreur123
        String livreurHash = encoder.encode("livreur123");
        System.out.println("Password: livreur123");
        System.out.println("Hash: " + livreurHash);
        System.out.println();

        // Generate hash for client123
        String clientHash = encoder.encode("client123");
        System.out.println("Password: client123");
        System.out.println("Hash: " + clientHash);
        System.out.println();

        // Generate hash for generic password123 (for all users)
        String genericHash = encoder.encode("password123");
        System.out.println("Password: password123 (generic)");
        System.out.println("Hash: " + genericHash);
    }
}
