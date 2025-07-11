package org.foodapp.dao;

import org.foodapp.model.Menu;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

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
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.merge(menu);
        session.getTransaction().commit();
        session.close();
    }


    public void delete(Menu menu) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.remove(menu);
        session.getTransaction().commit();
        session.close();
    }
}

