package main;

import domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;

public class UserUpdateMain {
    public static void main(String[] args) {
        EntityManagerFactory emf = Persistence.createEntityManagerFactory("jpabegin");

        EntityManager entityManager = emf.createEntityManager();
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            User user = entityManager.find(User.class, "user@user.com"); // select문 실행 함수 호출
            if (user == null) {
                System.out.println("User 없음");
            } else {
                String newName = "이름" + (System.currentTimeMillis() % 100);
                user.changeName(newName);
            }
            transaction.commit(); // update문 실행 함수 호출
        } catch (Exception ex) {
            ex.printStackTrace();
            transaction.rollback();
        } finally {
            entityManager.close();
        }
        emf.close();
    }
}