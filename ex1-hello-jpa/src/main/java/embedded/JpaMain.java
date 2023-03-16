package embedded;

import java.time.LocalDateTime;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

	public static void main(String[] args) {
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");
		EntityManager em = emf.createEntityManager();
		EntityTransaction tx = em.getTransaction();

		tx.begin();

		try {
			Member member = new Member();
			member.setWorkPeriod(new WorkPeriod(LocalDateTime.now().minusDays(20L), LocalDateTime.now()));
			member.setHomeAddress(new Address("춘천시", "서부대성로", "12345"));

			em.persist(member);

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		EntityManager em2 = emf.createEntityManager();
		EntityTransaction tx2 = em2.getTransaction();

		tx2.begin();

		// 임베디드 타입(객체 타입)의 위험성
		try {
			Address address = new Address("춘천시", "서부대성로", "12345");

			Member member1 = new Member();
			member1.setWorkPeriod(new WorkPeriod(LocalDateTime.now().minusDays(20L), LocalDateTime.now()));
			member1.setHomeAddress(address);
			em2.persist(member1);

			Member member2 = new Member();
			member2.setWorkPeriod(new WorkPeriod(LocalDateTime.now().minusDays(20L), LocalDateTime.now()));
			member2.setHomeAddress(address);
			em2.persist(member2);

			//member1, member2가 동일한 address를 참조하고 있다.
			//member1에 대해 address.city의 값을 변경하면, member2의 address.city 값도 변경된다.
			//임베디드 타입 객체를 여러 엔티티 객체에서 공유하면 매우 위험한 것이다. 임베디드 타입은 불변 객체로 설계 해야한다
			//불변 객체: 생성 시점 이후 절대 속성 값을 변경할 수 없는 객체
			//setter 메소드를 생성하지말거나 private으로 만들어, 외부에서는 생성자로만 임베디드 타입 객체의 속성 값을 설정할 수 있도록 해야한다.
//			member1.getHomeAddress().setCity("new city");

			tx2.commit();
		} catch (Exception e) {
			tx2.rollback();
		} finally {
			em2.close();
		}

		emf.close();
	}
}
