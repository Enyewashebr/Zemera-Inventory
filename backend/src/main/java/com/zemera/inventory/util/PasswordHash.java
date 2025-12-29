package com.zemera.inventory.util;

import org.mindrot.jbcrypt.BCrypt;

public class PasswordHash {
    public static void main(String[] args) {
        String password = "testpassword"; // replace with any password you want to hash
        String hashed = BCrypt.hashpw(password, BCrypt.gensalt());
        System.out.println(hashed);
    }
}
