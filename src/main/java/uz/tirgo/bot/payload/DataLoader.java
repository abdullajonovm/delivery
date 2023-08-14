package uz.tirgo.bot.payload;

import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import uz.tirgo.bot.entity.Role;
import uz.tirgo.bot.entity.enums.RoleEnum;
import uz.tirgo.bot.repository.RoleRepository;
import uz.tirgo.bot.repository.UserRepository;

import java.util.HashSet;
import java.util.Set;

@RequiredArgsConstructor
@Component
public class DataLoader implements CommandLineRunner {
    @Value("${spring.sql.init.mode}")
    private String mode; //always

    @Value("${spring.jpa.hibernate.ddl-auto}")
    private String ddl; //create

    private final RoleRepository roleRepository;
    private final UserRepository userRepository;

    @Override
    public void run(String... args) throws Exception {
        if (mode.equals("always") && ddl.equals("create")) {
            Role admin = new Role();
            admin.setName(RoleEnum.ADMIN);
            Role superadmin = new Role();
            superadmin.setName(RoleEnum.SUPER_ADMIN);
            Role user_role = new Role();
            user_role.setName(RoleEnum.CUSTOMER);

            roleRepository.save(admin);
            roleRepository.save(superadmin);
            roleRepository.save(user_role);


            Set<Role> roles = new HashSet<>();
            roles.add(admin);
            roles.add(superadmin);
            roles.add(user_role);

//
//            User user = new User();
//            user.setRoles(superadmin);
//            user.setLastName("Abdullajonov");
//            user.setFirstName("Muhammadqodir");
//
//            userRepository.save(user);
//
//
//            User user1 = new User();
//            user1.setRoles(admin);
//            user1.setLastName("Davlatov");
//            user1.setFirstName("Feruzbek");
//            userRepository.save(user1);
//
//
//
//            User user2 = new User();
//            user2.setRoles(user_role);
//            user2.setLastName("Yodgorbekov");
//            user2.setFirstName("Yodgorbekov");
//            userRepository.save(user2);

        }
    }
}
