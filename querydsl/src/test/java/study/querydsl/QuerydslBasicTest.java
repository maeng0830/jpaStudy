package study.querydsl;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.dsl.CaseBuilder;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.JPAExpressions;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.PersistenceUnit;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.entity.Member;
import study.querydsl.entity.QMember;
import study.querydsl.entity.QTeam;
import study.querydsl.entity.Team;

@SpringBootTest
@Transactional
public class QuerydslBasicTest {

	@Autowired
	EntityManager em;

	JPAQueryFactory queryFactory;

	@BeforeEach
	public void before() {
		queryFactory = new JPAQueryFactory(em);
		Team teamA = new Team("teamA");
		Team teamB = new Team("teamB");
		em.persist(teamA);
		em.persist(teamB);

		Member member1 = new Member("member1", 10, teamA);
		Member member2 = new Member("member2", 20, teamA);
		Member member3 = new Member("member3", 30, teamB);
		Member member4 = new Member("member4", 40, teamB);
		em.persist(member1);
		em.persist(member2);
		em.persist(member3);
		em.persist(member4);
	}

	@Test
	public void startJPQL() {
		//member1 조회
		String qlString = "select m from Member m where m.username = :username";

		// sql을 문자열로 작성 => 클라이언트가 해당 메소드를 실행할 때 예외 발생
		Member findMember = em.createQuery(qlString, Member.class)
				.setParameter("username", "member1")
				.getSingleResult();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	public void startQuerydsl() {
//		QMember m = new QMember("m"); // 별칭 직접 사용
//		QMember member = QMember.member; // 기본 인스턴스 사용

		// 메소드를 통해 sql을 작성 => 컴파일 시점에서 예외발생, 파라미터 바인딩을 자동으로 해줌
		Member findMember = queryFactory
				.select(member) // static 변수 사용
				.from(member)
				.where(member.username.eq("member1")) // 파라미터 바인딩 처리
				.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
	}

	@Test
	public void search() {
		Member findMember = queryFactory
				.selectFrom(member)
				.where(member.username.eq("member1").and(member.age.eq(10)))
				.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
		assertThat(findMember.getAge()).isEqualTo(10);
	}

	@Test
	public void searchAndParam() {
		Member findMember = queryFactory
				.selectFrom(member)
				.where(
						member.username.eq("member1"), // , 처리는 and로 묶인다.
						member.age.eq(10)
				)
				.fetchOne();

		assertThat(findMember.getUsername()).isEqualTo("member1");
		assertThat(findMember.getAge()).isEqualTo(10);
	}

	@Test
	public void resultFetch() {
		// fetch() => list로 조회
		// 데이터가 없으면 빈 리스트 반환
		List<Member> fetch = queryFactory
				.selectFrom(member)
				.fetch();

		// fetchOne() => 단건 조회
		// 데이터가 없으면 null, 둘 이상이면 NonUniqueResultException
		Member fetchOne = queryFactory
				.selectFrom(member)
				.fetchOne();

		// fetchFirst() => limit(1).fetchOne()
		Member fetchFirst = queryFactory
				.selectFrom(member)
				.fetchFirst();

		// fetchResults => fetch() + 페이징 정보, total count 쿼리 추가 실행
		QueryResults<Member> fetchResults = queryFactory
				.selectFrom(member)
				.fetchResults();

		long totalCount = fetchResults.getTotal();
		List<Member> results = fetchResults.getResults();

		// fetchCount() => count 쿼리로 변경하여 count 수 조회
		long totalCount2 = queryFactory
				.selectFrom(member)
				.fetchCount();
	}

	/**
	 * 회원 정렬 순서
	 * 1. 회원 나이 내림차순(desc)
	 * 2. 회원 이름 올림차순(asc)
	 * 단 2에서 회원 이름이 없으면 마지막에 출력(nulls last)
	 */
	@Test
	public void sort() {
		em.persist(new Member(null, 100));
		em.persist(new Member("member5", 100));
		em.persist(new Member("member6", 100));

		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.eq(100))
				.orderBy(member.age.desc(), member.username.asc().nullsLast())
				.fetch();

		Member member5 = result.get(0);
		Member member6 = result.get(1);
		Member memberNull = result.get(2);
		assertThat(member5.getUsername()).isEqualTo("member5");
		assertThat(member6.getUsername()).isEqualTo("member6");
		assertThat(memberNull.getUsername()).isNull();
	}

	@Test
	public void paging1() {
		List<Member> result = queryFactory
				.selectFrom(member)
				.orderBy(member.username.desc())
				.offset(1)
				.limit(2)
				.fetch();

		assertThat(result.size()).isEqualTo(2);
	}

	@Test
	public void paging2() {
		QueryResults<Member> queryResults = queryFactory
				.selectFrom(member)
				.orderBy(member.username.desc())
				.offset(1)
				.limit(2)
				.fetchResults();

		assertThat(queryResults.getTotal()).isEqualTo(4);
		assertThat(queryResults.getLimit()).isEqualTo(2);
		assertThat(queryResults.getOffset()).isEqualTo(1);
		assertThat(queryResults.getResults()).hasSize(2);
	}

	@Test
	public void aggregation() {
		List<Tuple> result = queryFactory
				.select(
						member.count(),
						member.age.sum(),
						member.age.avg(),
						member.age.max(),
						member.age.min()
				)
				.from(member)
				.fetch();

		Tuple tuple = result.get(0);
		assertThat(tuple.get(member.count())).isEqualTo(4);
		assertThat(tuple.get(member.age.sum())).isEqualTo(100);
		assertThat(tuple.get(member.age.avg())).isEqualTo(25);
		assertThat(tuple.get(member.age.max())).isEqualTo(40);
		assertThat(tuple.get(member.age.min())).isEqualTo(10);
	}

	/**
	 * 팀의 이름과 각 팀의 평균 연령을 구해라
	 */
	@Test
	void group() {
		// given
		List<Tuple> result = queryFactory
				.select(team.name, member.age.avg())
				.from(member)
				.join(member.team, team)
				.groupBy(team.name)
				.fetch();

		Tuple teamA = result.get(0);
		Tuple teamB = result.get(1);

		assertThat(teamA.get(team.name)).isEqualTo("teamA");
		assertThat(teamA.get(member.age.avg())).isEqualTo(15);

		assertThat(teamB.get(team.name)).isEqualTo("teamB");
		assertThat(teamB.get(member.age.avg())).isEqualTo(35);

		// when

		// then
	}

	/**
	 * 팀 A에 소속된 모든 회원을 찾아라
	 */
	@Test
	public void join() {
		List<Member> result = queryFactory
				.selectFrom(member)
				.leftJoin(member.team, team)
				.where(team.name.eq("teamA"))
				.fetch();

		List<Member> result2 = queryFactory
				.select(member)
				.from(member)
				.where(member.team.name.eq("teamA"))
				.fetch();

		assertThat(result)
				.extracting("username")
				.containsExactlyInAnyOrder("member1", "member2");

		assertThat(result2)
				.extracting("username")
				.containsExactlyInAnyOrder("member1", "member2");
	}

	/**
	 * 회원과 팀을 조인하면서, 팀 이름이 teamA인 팀만 조인, 회원은 모두 조회
	 * JPQL: select m, t from Member m left join m.team t on t.name = 'teamA'
	 */
	@Test
	public void join_on_filtering() {
		List<Tuple> result = queryFactory
				.select(member, team)
				.from(member)
				.leftJoin(member.team, team).on(team.name.eq("teamA"))
				.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
//			tuple = [Member(id=3, username=member1, age=10), Team(id=1, name=teamA)]
//			tuple = [Member(id=4, username=member2, age=20), Team(id=1, name=teamA)]
//			tuple = [Member(id=5, username=member3, age=30), null]
//			tuple = [Member(id=6, username=member4, age=40), null]
		}
	}

	/**
	 * 2. 연관관계 없는 엔티티 외부 조인
	 * 예) 회원의 이름과 팀의 이름이 같은 대상 외부 조인
	 * JPQL: SELECT m, t FROM Member m LEFT JOIN Team t on m.username = t.name
	 * SQL: SELECT m.*, t.* FROM Member m LEFT JOIN Team t ON m.username = t.name
	 */
	@Test
	public void join_on_no_relation() {
		em.persist(new Member("teamA"));
		em.persist(new Member("teamB"));

		List<Tuple> result = queryFactory
				.select(member, team)
				.from(member)
				.leftJoin(team).on(member.username.eq(team.name))
				.fetch();

		for (Tuple tuple : result) {
			System.out.println("t=" + tuple);
		}
	}

	@PersistenceUnit
	EntityManagerFactory emf;

	@Test
	public void fetchJoinNo() {
		em.flush();
		em.clear();

		Member findMember = queryFactory
				.selectFrom(member)
				.where(member.username.eq("member1"))
				.fetchOne();

		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("페치 조인 미적용").isFalse();

	}

	@Test
	public void fetchJoinUse() {
		em.flush();
		em.clear();

		Member findMember = queryFactory
				.selectFrom(member)
				.join(member.team, team).fetchJoin()
				.where(member.username.eq("member1"))
				.fetchOne();

		boolean loaded = emf.getPersistenceUnitUtil().isLoaded(findMember.getTeam());
		assertThat(loaded).as("페치 조인 적용").isTrue();
	}

	/**
	 * 나이가 가장 많은 회원을 조회
	 */
	@Test
	public void subQuery() {
		// 메인 쿼리와 서브 쿼리의 엘리어스가 중복되면 안되므로 새로운 엘리어스를 만든다.
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.eq(
						select(memberSub.age.max())
								.from(memberSub)
				))
				.fetch();

		assertThat(result).extracting("age")
				.containsExactlyInAnyOrder(40);
	}

	/**
	 * 나이가 평균보다 많은 회원을 조회
	 */
	@Test
	public void subQueryGoe() {
		// 메인 쿼리와 서브 쿼리의 엘리어스가 중복되면 안되므로 새로운 엘리어스를 만든다.
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.goe(
						select(memberSub.age.avg())
								.from(memberSub)
				))
				.fetch();

		assertThat(result).extracting("age")
				.containsExactlyInAnyOrder(30, 40);
	}

	/**
	 * 10살 이상인 나이를 가진 회원 조회
	 */
	@Test
	public void subQueryIn() {
		// 메인 쿼리와 서브 쿼리의 엘리어스가 중복되면 안되므로 새로운 엘리어스를 만든다.
		QMember memberSub = new QMember("memberSub");

		List<Member> result = queryFactory
				.selectFrom(member)
				.where(member.age.in(
						select(memberSub.age)
								.from(memberSub)
								.where(memberSub.age.gt(10))
				))
				.fetch();

		assertThat(result).extracting("age")
				.containsExactlyInAnyOrder(20, 30, 40);
	}

	/**
	 * 회원명과 나이 평균을 조회한다.
	 */
	@Test
	public void selectSubQuery() {
		// 메인 쿼리와 서브 쿼리의 엘리어스가 중복되면 안되므로 새로운 엘리어스를 만든다.
		QMember memberSub = new QMember("memberSub");

		List<Tuple> fetch = queryFactory
				.select(member.username,
						select(memberSub.age.avg())
								.from(memberSub)
				).from(member)
				.fetch();

		for (Tuple tuple : fetch) {
			System.out.println("username = " + tuple.get(member.username));
			System.out.println("age = " +
					tuple.get(select(memberSub.age.avg())
							.from(memberSub)));
		}
	}

	@Test
	public void basicCase() {
		List<String> result = queryFactory
				.select(member.age
						.when(10).then("열살")
						.when(20).then("스무살")
						.otherwise("기타"))
				.from(member)
				.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
//			s = 열살
//			s = 스무살
//			s = 기타
//			s = 기타
		}
	}

	@Test
	public void complexCase() {
		List<String> result = queryFactory
				.select(new CaseBuilder()
						.when(member.age.between(0, 20)).then("0~20살")
						.when(member.age.between(21, 30)).then("21~30살")
						.otherwise("기타"))
				.from(member)
				.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
//			s = 0~20살
//			s = 0~20살
//			s = 21~30살
//			s = 기타
		}
	}

	@Test
	public void constant() {
		List<Tuple> result = queryFactory
				.select(member.username, Expressions.constant("A"))
				.from(member)
				.fetch();

		for (Tuple tuple : result) {
			System.out.println("tuple = " + tuple);
		}
	}

	@Test
	public void concat() {
		String result = queryFactory
				.select(member.username.concat("_").concat(member.age.stringValue()))
				.from(member)
				.where(member.username.eq("member1"))
				.fetchOne();

		System.out.println("result = " + result);
	}
}
