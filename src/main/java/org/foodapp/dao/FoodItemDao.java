package org.foodapp.dao;

import org.hibernate.query.Query;
import org.foodapp.model.FoodItem;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import java.util.List;
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

    public List<FoodItem> findByFilters(String search, Integer price, List<String> keywords) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        String hql = "SELECT f FROM FoodItem f WHERE 1=1";

        if (search != null && !search.isBlank()) {
            hql += " AND (lower(f.name) LIKE :search OR lower(f.description) LIKE :search)";
        }

        if (price != null) {
            hql += " AND f.price <= :price";
        }

        Query<FoodItem> query = session.createQuery(hql, FoodItem.class);

        if (search != null && !search.isBlank()) {
            query.setParameter("search", "%" + search.toLowerCase() + "%");
        }

        if (price != null) {
            query.setParameter("price", price);
        }

        List<FoodItem> results = query.list();
        session.close();

        // فیلتر دستی روی keywordها
        if (keywords != null && !keywords.isEmpty()) {
            return results.stream()
                    .filter(f -> f.getKeywords() != null &&
                            f.getKeywords().stream().anyMatch(k -> keywords.contains(k)))
                    .toList();
        }

        return results;
    }

}

