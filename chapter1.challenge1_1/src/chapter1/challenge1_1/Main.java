package chapter1.challenge1_1;

import java.util.Scanner;

/**
 * Chapter1_Challenge_1_1
 * The Cryptic Message Decoder (Variables & Operators)
 *
 * Scenario:
 * You are a secret agent who has intercepted a cryptic message.
 * The message is an integer, but the real information is hidden within it
 * through a series of mathematical operations.
 *
 * Requirements:
 *  - No if-statements or loops (only variables and operators)
 *  - Use division (/), modulus (%), and assignment operations
 *  - Extract digits mathematically
 *
 * Example:
 *  Input: 13579
 *  Output: The decrypted code is: 910
 */
public class Main {

    public static void main(String[] args) {
        Scanner input = new Scanner(System.in);

        System.out.print("Enter a positive integer: ");
        int number = input.nextInt();

        // Make sure number is positive
        number = Math.abs(number);

        // Step 1: Count digits
        int digitsCount = (number == 0) ? 1 : (int) Math.log10(number) + 1;

        // Step 2: Extract digits mathematically
        int lastDigit = number % 10;
        int firstDigit = number / (int) Math.pow(10, digitsCount - 1);

        // Middle part = number without first and last digit
        int middlePart = (digitsCount > 2)
                ? (number % (int) Math.pow(10, digitsCount - 1)) / 10
                : 0;

        // Extract second and second-last digits
        int secondDigit = (digitsCount > 2)
                ? middlePart / (int) Math.pow(10, digitsCount - 3)
                : 0;
        int secondLastDigit = (digitsCount > 2)
                ? middlePart % 10
                : 0;

        // Step 3: Perform operations
        int product = firstDigit * lastDigit;
        int sum = secondDigit + secondLastDigit;

        // Step 4: Combine results
        String finalCode = String.valueOf(product) + sum;

        // Step 5: Display the result
        System.out.println("The decrypted code is: " + finalCode);
    }
}
