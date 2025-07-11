package org.foodapp.dao;
import org.foodapp.model.Order;
import org.foodapp.model.OrderStatus;
import org.foodapp.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.query.Query;
import java.util.List;
public class OrderDao {
    public List<Order> findByRestaurantWithFilters(Long restaurantId, String status,
                                                   String search, String user, String courier) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        String hql = "FROM Order o WHERE o.restaurant.id = :rid";
        if (status != null) hql += " AND o.status = :status";
        if (user != null) hql += " AND o.buyer.phoneNumber = :user";
        if (courier != null) hql += " AND o.courier.phoneNumber = :courier";
        if (search != null) hql += " AND (o.buyer.name LIKE :search OR o.id LIKE :search)";
        Query<Order> q = session.createQuery(hql, Order.class);
        q.setParameter("rid", restaurantId);
        if (status != null) q.setParameter("status", OrderStatus.valueOf(status.toUpperCase()));
        if (user != null) q.setParameter("user", user);
        if (courier != null) q.setParameter("courier", courier);
        if (search != null) q.setParameter("search", "%"+search+"%");
        List<Order> list = q.list();
        session.close();
        return list;
    }

    public Order findById(Long id) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        Order o = session.get(Order.class, id);
        session.close();
        return o;
    }

    public void update(Order order) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        session.beginTransaction();
        session.merge(order);
        session.getTransaction().commit();
        session.close();
    }
}

