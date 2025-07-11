package org.foodapp.dao;

import org.foodapp.model.Restaurant;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;

import java.util.List;

public class RestaurantDao {

    public void save(Restaurant restaurant) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.persist(restaurant);
        session.getTransaction().commit();
        session.close();
    }

    public List<Restaurant> findBySeller(Long sellerId) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Query<Restaurant> query = session.createQuery("FROM Restaurant WHERE seller.id = :sellerId", Restaurant.class);
        query.setParameter("sellerId", sellerId);
        List<Restaurant> restaurants = query.list();
        session.close();
        return restaurants;
    }

    public Restaurant findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Restaurant restaurant = session.get(Restaurant.class, id);
        session.close();
        return restaurant;
    }


    public void update(Restaurant restaurant) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.merge(restaurant);
        session.getTransaction().commit();
        session.close();
    }

    public List<Restaurant> findByFilters(String search, List<String> keywords) {
        Session session = HibernateUtil.getSessionFactory().openSession();

        // ساخت HQL بر اساس فیلتر search
        String hql = "SELECT DISTINCT r FROM Restaurant r " +
                "LEFT JOIN FETCH r.menus m " +
                "LEFT JOIN FETCH m.items i " +
                "WHERE 1=1";

        if (search != null && !search.isBlank()) {
            hql += " AND (lower(r.name) LIKE :search OR lower(r.address) LIKE :search)";
        }

        Query<Restaurant> query = session.createQuery(hql, Restaurant.class);

        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        List<Restaurant> results = query.list();
        session.close();

        // اگر فیلتر keyword وجود دارد، در Java فیلتر شود
        if (keywords != null && !keywords.isEmpty()) {
            return results.stream()
                    .filter(r -> r.getMenus().stream()
                            .flatMap(menu -> menu.getItems().stream())
                            .anyMatch(item -> item.getKeywords() != null &&
                                    item.getKeywords().stream().anyMatch(keywords::contains)))
                    .toList();
        }

        return results;
    }



}

