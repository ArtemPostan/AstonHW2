package org.example;

import org.example.repository.UserRepository;
import org.example.services.UserService;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        UserService userService = new UserService(new UserRepository(), new Scanner(System.in));
        userService.start();
    }
}
