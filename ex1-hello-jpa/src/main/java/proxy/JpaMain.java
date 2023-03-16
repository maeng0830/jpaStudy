package proxy;

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
			member.setName("memberA");
			em.persist(member);

			em.flush();
			em.clear();

			// em.getReference()를 호출할 때는 select 쿼리가 실행되지 않는다.
			System.out.println("=== em.getReference(Member.class, member.getId()); ===");
			Member findMember = em.getReference(Member.class, member.getId());

			// findMember.getClass() = class proxy.Member$HibernateProxy$SKwmBJri : id 값만 있는 가짜(프록시) 객체
			// 프록시는 실제 클래스를 상속 받아서 만들어 진다. 그래서 실제 클래스와 겉 모양이 같다.
			// 프록시 객체는 실제 객체의 참조(target)을 보관한다. 프록시 객체를 호출하면 프록시 객체는 실제 객체의 메소드를 호출한다.
			System.out.println("findMember.getClass() = " + findMember.getClass());

			// 조회한 데이터의 엔티티가 실제로 사용될 때 쿼리가 실행된다.
			System.out.println("=== System.out.println(\"findMember.getId() = \" + findMember.getId()); ===");
			System.out.println("findMember.getId() = " + findMember.getName());

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();
	}
}
