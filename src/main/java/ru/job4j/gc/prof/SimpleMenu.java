package ru.job4j.gc.prof;

import java.util.Random;
import java.util.Scanner;

public class SimpleMenu {

    private static final int BUBBLE_SORT = 1;
    private static final int INSERT_SORT = 2;
    private static final int MERGE_SORT = 3;
    private static final int EXIT = 4;

    private static void menu() {
        System.out.println("1.Пузырьковая сортировка.");
        System.out.println("2.Сортировка вставкой.");
        System.out.println("3.Сортировка слиянием");
        System.out.println("4.Выход");
    }

    public static void main(String[] args) {
        Random random = new Random();
        boolean run = true;
        RandomArray randomArray = new RandomArray(random);
        Scanner scanner = new Scanner(System.in);
        int arrayElements = 250000;
        randomArray.insert(arrayElements);
        System.out.println("Выберите пункт меню.");
        while (run) {
            menu();
            int choice = scanner.nextInt();
            if (choice == BUBBLE_SORT) {
                BubbleSort bubbleSort = new BubbleSort();
                System.out.println(bubbleSort.sort(randomArray));
            }
            if (choice == INSERT_SORT) {
                InsertSort insertSort = new InsertSort();
                System.out.println(insertSort.sort(randomArray));
            }
            if (choice == MERGE_SORT) {
                MergeSort mergeSort = new MergeSort();
                System.out.println(mergeSort.sort(randomArray));
            }
            if (choice == EXIT) {
                run = false;
                System.out.println("Завершение работы");
            }
        }
    }
}
