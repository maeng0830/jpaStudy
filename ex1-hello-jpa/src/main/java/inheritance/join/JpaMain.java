package inheritance.join;

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
			Movie movie = new Movie();
			movie.setDirector("aaaa");
			movie.setActor("bbbb");
			movie.setName("영화제목");
			movie.setPrice(10000);

			// item 테이블 데이터에 대한 insert 쿼리
			// movie 테이블 데이터에 대한 insert 쿼리
			// 두 테이블 id 컬럼이 있는데, item 테이블은 pk, movie 테이블은 pk와 fk 역할을 한다.
			em.persist(movie);

			tx.commit();
		} catch (Exception e) {
			tx.rollback();
		} finally {
			em.close();
		}

		emf.close();
	}
}
