package hellojpa;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;

public class JpaMain {

	public static void main(String[] args) {
		/**
		 * META-INF/persistence.xml의 정보를 읽어 EntityManagerFactory를 생성한다.
		 * 어플리케이션 구동 시점에 딱 1개의 emf만 생성
		 */
		EntityManagerFactory emf = Persistence.createEntityManagerFactory("hello");

		/**
		 * EntityManagerFactory에서 entityManager를 생성한다.
		 * 트랜잭션 단위의 작업이 실행 될 때마다, em을 생성
		 * 해당 em 내부에 영속성 컨텍스트가 생성된다.
		 */
		EntityManager em = emf.createEntityManager();

		// 트랜잭션 획득 및 시작
		// JPA의 모든 데이터 변경은 트랜잭션 안에서 실행되어야 한다.
		EntityTransaction tx = em.getTransaction();
		tx.begin();

		// 저장
		try {
			// 비영속 상태
			Member member = new Member();
			member.setId(101L);
			member.setUsername("HelloJPA");

			// 영속 상태
			// em 내부의 영속성 컨텍스트에 entity가 저장된다.
			System.out.println("=== em.persist(member) ===");
			em.persist(member);

			// DB에 select 쿼리 전달 X, 영속성 컨텍스트의 1차 캐시에 저장된 데이터를 가져오기 때문이다.
			System.out.println("=== em.find(Member.class, 101L) ===");
			Member findMember = em.find(Member.class, 101L);
			System.out.println("findMember.id = " + findMember.getId());

			System.out.println("=== tx.commit() ===");
			// DB에 쿼리 전달
			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			// 리소스 정리
			em.close();
		}

		EntityManager em2 = emf.createEntityManager();
//		EntityTransaction tx2 = em2.getTransaction();
		try {
			// DB에 select 쿼리 전달 O, 영속성 컨텍스트의 1차 캐시에 해당 엔티티가 저장되어있지 않기 때문이다.
			// DB에서 조회된 데이터는 엔티티화 되어 1차 캐시에 저장된다.
			System.out.println("=== 첫번째 em.find(Member.class, 101L) ===");
			Member findMember1 = em2.find(Member.class, 101L);
			System.out.println("findMember1.id = " + findMember1.getId());

			// DB에 select 쿼리 전달 X, 영속성 컨텍스트의 1차 캐시에 해당 엔티티가 저장되어있기 때문이다.
			System.out.println("=== 두번째 em.find(Member.class, 101L) ===");
			Member findMember2 = em2.find(Member.class, 101L);
			System.out.println("findMember2.id = " + findMember2.getId());

			// 1차 캐시를 통해 반복 가능 읽기 등급의 트랜잭션 격리 수준을 DB가 아닌 어플리케이션에서 제공 가능하다.
			// 마치 자바 컬렉션에서 객체를 조회했던 것 처럼, 1차 캐시에서 조회된 객체는 동일성이 보장된다.
			System.out.println("=== 영속 엔티티의 동일성 보장 ===");
			System.out.println(findMember1 == findMember2); // true

			System.out.println("=== tx.commit() ===");
			// 조회만 하므로 불필요
			// tx2.commit();
		} catch (Exception e) {
			// tx2.rollback();
		} finally {
			// 리소스 정리
			em2.close();
		}

		EntityManager em3 = emf.createEntityManager();
		EntityTransaction tx3 = em3.getTransaction();
		tx3.begin();
		try {
			Member memberA = new Member(123L, "MemberA");
			Member memberB = new Member(456L, "MemberB");

			/**
			 * !!쓰기 지연
			 * em3.persist(entity)가 실행되면, 생성된 insert 쿼리들은 영속성 컨텍스트 내부의 쓰기 지연 SQL 저장소에 저장된다.
			 * 그리고 해당 엔티티는 1차 캐시에 저장된다.
			 */
			System.out.println("=== em3.persist(entity) ===");
			em3.persist(memberA);
			em3.persist(memberB);

			// tx.commit() 시, 쓰기 지연 SQL 저장소에 저장된 insert 쿼리들이 DB에 전달된다. = flush
			// 그리고 한번에 commit 된다 = commit
			System.out.println("=== tx3.commit() ===");
			tx3.commit();
		} catch (Exception e) {
			tx3.rollback();
		} finally {
			em3.close();
		}

		EntityManager em4 = emf.createEntityManager();
		EntityTransaction tx4 = em4.getTransaction();
		tx4.begin();
		try {
			// DB로부터 해당 데이터를 조회하고, 엔티티화 하여 1차 캐시에 저장한다. 그리고 해당 영속 엔티티를 반환한다.
			System.out.println("=== Member findMember = em4.find(Member.class, 101L) ===");
			Member findMember = em4.find(Member.class, 101L);

			// !!변경 감지(Dirty Checking)
			// 자바 컬렉션에서 획득한 객체에 변경사항이 있을 경우, 컬렉션에 저장된 객체도 변경되는 것 처럼,
			// 1차 캐시에서 획득한 영속 엔티티에 변경사항이 있을 경우, 1차 캐시에 저장된 영속 엔티티도 변경된다.
			// 1차 캐시에 저장된 해당 영속 엔티티의 스냅샷(기존 상태)과 현재 영속 엔티티간의 차이가 있을 경우,
			// update 쿼리가 쓰기 지연 SQL 저장소에 저장된다.
			System.out.println("=== findMember.setName(\"modMember\") ===");
			findMember.setUsername("modMember");

			// 그러므로 em.update(findMember)와 같은 별도의 update 쿼리를 생성하는 코드가 필요 없다.

			// 쓰기 지연 SQL 저장소에 저장된 update 쿼리를 DB에 전달한다. = flush
			// 그리고 커밋한다.
			System.out.println("=== tx4.commit() ===");
			tx4.commit();
		} catch (Exception e) {
			tx4.rollback();
		} finally {
			em4.close();
		}

		emf.close();

	}
}
