package main;

import domain.User;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.Persistence;
import java.time.LocalDateTime;

// 영속 컨텍스트
// DB에서 불러온 것이든, 어플리케이션에서 새로 생성된 것이든 엔티티를 메모리에 보관해두는 공간이다.
// 트랜잭션 범위 내에서 엔티티에 대한 어떤 변경 사항(DB에 저장된 해당 엔티티와 비교했을 때) 발생하는지 추척하고, 변경사항이 있을 경우 커밋 시점에 DB에 반영한다.

public class UserSaveMain {
    public static void main(String[] args) {
        // emf는 영속성 단위 기준으로 초기화 된다. <- DB
        // 어플리케이션 구동 시 1번만 생성된다. 그리고 어플리케이션이 종료될 때 emf도 종료된다.
        EntityManagerFactory emf =
            Persistence.createEntityManagerFactory("jpabegin");

        // em은 DB 작업을 위한 핵심 객체이다. DB 작업이 필요할 때마다 생성된다.
        EntityManager entityManager = emf.createEntityManager();

        // et는 트랜잭션이 필요한 작업을 실시할 때 필요하다. 커밋 또는 롤백을 실행하는 객체이다.
        EntityTransaction transaction = entityManager.getTransaction();
        try {
            transaction.begin();
            User user = new User("user@user2.com", "user", LocalDateTime.now());
            entityManager.persist(user);
            transaction.commit(); // 실제 insert문이 실행되는 함수 호출
        } catch (Exception ex) {
            ex.printStackTrace();
            transaction.rollback();
        } finally {
            entityManager.close();
        }

        emf.close();
    }
}