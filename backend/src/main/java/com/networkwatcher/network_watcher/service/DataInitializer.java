package com.networkwatcher.network_watcher.service;

import com.networkwatcher.network_watcher.model.Role;
import com.networkwatcher.network_watcher.model.User;
import com.networkwatcher.network_watcher.repository.RoleRepository;
import com.networkwatcher.network_watcher.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import java.util.HashSet;
import java.util.Set;

@Component
public class DataInitializer implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private RoleRepository roleRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (roleRepository.count() == 0) {
            Role admin = new Role();
            admin.setName("ROLE_ADMIN");
            admin.setDescription("Administrator with full access");
            roleRepository.save(admin);

            Role analyst = new Role();
            analyst.setName("ROLE_ANALYST");
            analyst.setDescription("Security analyst");
            roleRepository.save(analyst);

            Role operator = new Role();
            operator.setName("ROLE_OPERATOR");
            operator.setDescription("Network operator");
            roleRepository.save(operator);

            Role viewer = new Role();
            viewer.setName("ROLE_VIEWER");
            viewer.setDescription("Read-only viewer");
            roleRepository.save(viewer);
        }

        if (userRepository.count() == 0) {
            User adminUser = new User();
            adminUser.setUsername("admin");
            adminUser.setPassword(passwordEncoder.encode("admin123"));
            adminUser.setEmail("admin@networkwatcher.com");
            adminUser.setEnabled(true);
            
            Set<Role> roles = new HashSet<>();
            roles.add(roleRepository.findByName("ROLE_ADMIN").orElseThrow());
            adminUser.setRoles(roles);
            
            userRepository.save(adminUser);
        }
    }
}
