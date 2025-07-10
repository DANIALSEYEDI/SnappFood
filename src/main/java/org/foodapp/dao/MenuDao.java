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

    public Menu findByRestaurantIdAndTitle(Long restaurantId, String title) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Query<Menu> query = session.createQuery(
                "FROM Menu WHERE restaurant.id = :rid AND title = :title", Menu.class);
        query.setParameter("rid", restaurantId);
        query.setParameter("title", title);
        Menu menu = query.uniqueResult();
        session.close();
        return menu;
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

