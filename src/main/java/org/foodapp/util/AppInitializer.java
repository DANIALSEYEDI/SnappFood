package org.foodapp.util;
import org.foodapp.dao.CreateAdminDao;
import org.foodapp.model.Admin;
public class AppInitializer {
    public static void initializeAdminAccount() {
        CreateAdminDao adminDao = new CreateAdminDao();
        Admin admin = adminDao.findByUsername("admin");
        if (admin == null) {
            admin = new Admin();
            admin.setUsername("admin");
            admin.setPassword("admin");
            adminDao.save(admin);
        }
        String token = JwtUtil.generateToken(admin.getId().toString(), "ADMIN");
        System.out.println("🔐 Admin token: " + token);
    }
}
