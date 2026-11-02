package com.modernricemill.config;

import com.modernricemill.entity.Product;
import com.modernricemill.entity.Role;
import com.modernricemill.entity.User;
import com.modernricemill.repository.ProductRepository;
import com.modernricemill.repository.UserRepository;
import com.modernricemill.service.CustomerAuthService;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Configuration
public class DataInitializer {

    @Bean
    CommandLineRunner seedUsers(UserRepository users, PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByEmail("admin@ricemill.local")) {
                User admin = new User();
                admin.setName("Mill Admin");
                admin.setEmail("admin@ricemill.local");
                admin.setPassword(encoder.encode("admin123"));
                admin.setRole(Role.ROLE_ADMIN);
                users.save(admin);
            }
            String demoPhone = "9876543210";
            String demoEmail = CustomerAuthService.syntheticEmailForPhone(demoPhone);
            if (!users.existsByEmail(demoEmail)) {
                User u = new User();
                u.setName("Demo Customer");
                u.setEmail(demoEmail);
                u.setPhone(demoPhone);
                u.setPassword(encoder.encode(UUID.randomUUID().toString()));
                u.setRole(Role.ROLE_USER);
                users.save(u);
            }
        };
    }

    @Bean
    CommandLineRunner seedProducts(ProductRepository products) {
        return args -> {
            if (products.count() > 0) {
                List<Product> existing = products.findAll();
                boolean updated = false;
                for (Product p : existing) {
                    if (p.getCategory() == null) {
                        String name = p.getName();
                        if (name.contains("Oil")) {
                            p.setCategory("Oils");
                        } else if (name.contains("Ulundhu") || name.contains("Paruppu")) {
                            p.setCategory("Staples");
                        } else {
                            p.setCategory("Rice");
                        }
                        if (p.getOriginalPrice() == null && (name.contains("500ml") || name.contains("1L") || name.contains("26kg") || name.contains("10kg") || name.contains("5kg"))) {
                            p.setOriginalPrice(p.getPrice().multiply(new BigDecimal("1.15")).setScale(0, java.math.RoundingMode.HALF_UP));
                        }
                        updated = true;
                    }
                }
                if (updated) {
                    products.saveAll(existing);
                }
                return;
            }
            String oilImg = "/images/sample-oil.svg";
            String riceImg = "/images/sample-rice.svg";
            List<Product> batch = new ArrayList<>();
            batch.add(p("Coconut Oil 200ml", "100", oilImg));
            batch.add(p("Coconut Oil 500ml", "220", oilImg));
            batch.add(p("Groundnut Oil 1L", "250", oilImg));
            batch.add(p("Groundnut Oil 2L", "500", oilImg));
            batch.add(p("Groundnut Oil 5L", "1250", oilImg));
            batch.add(p("Sesame Oil 500ml", "190", oilImg));
            batch.add(p("Sesame Oil 1L", "390", oilImg));
            batch.add(p("Rice — Z Royal 26kg", "1750", riceImg));
            batch.add(p("Rice — Z Gold 26kg", "1700", riceImg));
            batch.add(p("Rice — Malligai 26kg", "1700", riceImg));
            batch.add(p("Rice — Kathir 26kg", "1700", riceImg));
            batch.add(p("Rice — Nellai 26kg", "1600", riceImg));
            batch.add(p("Rice — Thenmalar 26kg", "1550", riceImg));
            batch.add(p("Rice — Roja 26kg", "1500", riceImg));
            batch.add(p("Rice — Raja Gopuram 26kg", "1400", riceImg));
            batch.add(p("Rice — Triple Horse 26kg", "1350", riceImg));
            batch.add(p("Rice — Madurai Gate 26kg", "1300", riceImg));
            batch.add(p("Rice — Cow Brand 26kg", "1250", riceImg));
            batch.add(p("Rice — Sivaji 26kg", "1200", riceImg));
            batch.add(p("Rice — Muyal 26kg", "1150", riceImg));
            batch.add(p("Rice — Sugam New", "1100", riceImg));
            batch.add(p("Rice — Muthamizh New 75kg", "3700", riceImg));
            batch.add(p("Rice — Mahaveeran New 75kg", "4500", riceImg));
            batch.add(p("Rice — Veera Sivaji 10kg", "600", riceImg));
            batch.add(p("Rice — Veera Sivaji 5kg", "300", riceImg));
            batch.add(p("Rice — Bullet 10kg", "580", riceImg));
            batch.add(p("Rice — Bullet 5kg", "290", riceImg));
            batch.add(p("Rice — Ulundhu 25kg", "3250", riceImg));
            batch.add(p("Rice — Paruppu 25kg", "3000", riceImg));
            products.saveAll(batch);
        };
    }

    private static Product p(String name, String price, String image) {
        Product x = new Product();
        x.setName(name);
        BigDecimal currentPrice = new BigDecimal(price);
        x.setPrice(currentPrice);
        x.setImageUrl(image);
        x.setDescription("Premium quality from Sri Devi Modern Rice Mill. Order online for fresh delivery.");
        x.setStockQuantity(100);

        if (name.contains("Oil")) {
            x.setCategory("Oils");
        } else if (name.contains("Ulundhu") || name.contains("Paruppu")) {
            x.setCategory("Staples");
        } else {
            x.setCategory("Rice");
        }

        // Add a demo discount (original price 15% higher) for select products
        if (name.contains("500ml") || name.contains("1L") || name.contains("26kg") || name.contains("10kg") || name.contains("5kg")) {
            x.setOriginalPrice(currentPrice.multiply(new BigDecimal("1.15")).setScale(0, java.math.RoundingMode.HALF_UP));
        }

        return x;
    }
}
