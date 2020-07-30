package br.com.log.processor;

import java.io.FileInputStream;
import java.util.Scanner;

public class Main {

    public static void main(String[] args) {
        try {
            System.out.println("Input the location of the log file:");
            Scanner scanner = new Scanner(System.in);
            String inputLocation = scanner.nextLine();
            FileInputStream input = new FileInputStream(inputLocation);
            Processor processor = new Processor(input);
            ReportGenerator reportGenerator = new ReportGenerator(processor);
            reportGenerator.generateReport();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
