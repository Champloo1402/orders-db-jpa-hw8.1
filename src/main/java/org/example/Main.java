package org.example;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import org.example.model.Client;
import org.example.model.Order;
import org.example.model.Product;

import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.util.concurrent.Callable;

public class Main {
    static EntityManagerFactory emf;
    static EntityManager em;

    public static void main(String[] args) {
        Scanner sc = new Scanner(System.in);

        try {
            emf = Persistence.createEntityManagerFactory("ordersPU");
            em = emf.createEntityManager();

            boolean running = true;
            try {
                while (running) {
                    System.out.println("1: add client");
                    System.out.println("2: add product");
                    System.out.println("3: create order");
                    System.out.println("4: exit");

                    String s = sc.nextLine();

                    switch (s) {
                        case "1":
                            addClient(sc);
                            break;
                        case "2":
                            addProduct(sc);
                            break;
                        case "3":
                            createOrder(sc);
                            break;
                        case "4":
                            running = false;
                            break;
                    }

                }
            } finally {
                if (em != null) em.close();
                if (emf != null) emf.close();
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        }


        System.out.println("JPA shutdown complete");
    }

    private static void addClient(Scanner sc) {
        System.out.println("Enter a name for a client: ");
        String name = sc.nextLine();

        System.out.println("Enter an email for a client: ");
        String email = sc.nextLine();

        performTransaction(() -> {
            Client client = new Client(name, email);
            em.persist(client);
            return client;
        });
    }

    private static void addProduct(Scanner sc) {
        System.out.println("Enter a name for a product: ");
        String name = sc.nextLine();

        System.out.println("Enter price: ");
        double price = sc.nextDouble();

        sc.nextLine();

        performTransaction(() -> {
            Product product = new Product(name, price);
            em.persist(product);
            return product;
        });

    }

    public static void createOrder(Scanner sc) {
        System.out.println("Enter client's ID: ");
        Long id = sc.nextLong();
        Client client = em.find(Client.class, id);
        if(client == null) throw new RuntimeException("Client not found");

        sc.nextLine();

        System.out.println("Enter product IDs (comma-separated): ");
        String ids = sc.nextLine();
        String[] parts = ids.split(",");
        List<Product> products = new ArrayList<>();

        for(String part : parts){
            Long productId = Long.parseLong(part.trim());
            Product product = em.find(Product.class, productId);
            products.add(product);
        }

        performTransaction(() -> {
            Order order = new Order(client, products);
            em.persist(order);
            return order;
        });


    }

    private static <T> void performTransaction(Callable<T> action) {
        EntityTransaction transaction = em.getTransaction();
        transaction.begin();
        try {
            T result = action.call();
            transaction.commit();

        } catch (Exception ex) {
            if (transaction.isActive())
                transaction.rollback();

            throw new RuntimeException(ex);
        }
    }


}