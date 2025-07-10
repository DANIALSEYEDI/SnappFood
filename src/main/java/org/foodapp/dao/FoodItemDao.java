package org.foodapp.dao;

import org.foodapp.model.FoodItem;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;

public class FoodItemDao {

    public void save(FoodItem item) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.persist(item);
        session.getTransaction().commit();
        session.close();
    }

    public FoodItem findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        FoodItem item = session.get(FoodItem.class, id);
        session.close();
        return item;
    }

    public void update(FoodItem item) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.merge(item);
        session.getTransaction().commit();
        session.close();
    }

    public void delete(FoodItem item) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.remove(item);
        session.getTransaction().commit();
        session.close();
    }
}

