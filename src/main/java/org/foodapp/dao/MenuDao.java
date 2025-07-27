package org.foodapp.dao;
import org.foodapp.model.Menu;
import org.foodapp.model.Restaurant;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import java.util.List;

public class MenuDao {
    public void save(Menu menu) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.persist(menu);
        session.getTransaction().commit();
        session.close();
    }

    public Menu findByRestaurantAndTitleWithItems(Long restaurantId, String title) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            List<Menu> menus = session.createQuery(
                            "SELECT DISTINCT m FROM Menu m " +
                                    "LEFT JOIN FETCH m.items " +
                                    "WHERE m.restaurant.id = :restaurantId AND LOWER(m.title) = LOWER(:title)", Menu.class)
                    .setParameter("restaurantId", restaurantId)
                    .setParameter("title", title)
                    .list();

            return menus.isEmpty() ? null : menus.get(0);
        } finally {
            session.close();
        }
    }

    public void update(Menu menu) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.merge(menu);
            tx.commit();
        }
    }

    public Menu findByTitleAndRestaurant(String title, Restaurant restaurant) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Menu m WHERE m.title = :title AND m.restaurant = :restaurant", Menu.class
                    )
                    .setParameter("title", title)
                    .setParameter("restaurant", restaurant)
                    .uniqueResult();
        }
    }

    public void delete(Menu menu) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction tx = session.beginTransaction();
            session.remove(menu);
            tx.commit();
        }
    }

    public List<Menu> findByRestaurantId(Long restaurantId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            return session.createQuery(
                            "FROM Menu m WHERE m.restaurant.id = :restaurantId", Menu.class
                    )
                    .setParameter("restaurantId", restaurantId)
                    .list();
        }
    }

}