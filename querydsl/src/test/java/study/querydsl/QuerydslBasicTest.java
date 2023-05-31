package study.querydsl;

import static com.querydsl.jpa.JPAExpressions.*;
import static org.assertj.core.api.Assertions.*;
import static study.querydsl.entity.QMember.*;
import static study.querydsl.entity.QTeam.*;

import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.QueryResults;
import com.querydsl.core.Tuple;
import com.querydsl.core.types.ExpressionUtils;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.BooleanExpression;
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
import org.springframework.test.annotation.Commit;
import org.springframework.transaction.annotation.Transactional;
import study.querydsl.dto.MemberDto;
import study.querydsl.dto.QMemberDto;
import study.querydsl.dto.UserDto;
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

	///////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////   Projection    /////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void simpleProjection() {
		List<String> result = queryFactory
				.select(member.username)
				.from(member)
				.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	// tuple은 리포지토리 계층에서만 사용하자.
	// 서비스 계층에는 DTO로 변환하여 반환하는 것이 좋다.
	@Test
	public void tupleProjection() {
		List<Tuple> result = queryFactory
				.select(member.username, member.age)
				.from(member)
				.fetch();

		for (Tuple tuple : result) {
			String username = tuple.get(member.username);
			Integer age = tuple.get(member.age);
			System.out.print("username = " + username);
			System.out.println("age = " + age);

		}
	}

	// JPQL에서의 DTO 조회
	// 생성자 방식만 지원, DTO의 패키지명 까지 작성해줘야함
	@Test
	public void findDtoByJPQL() {
		List<MemberDto> result = em.createQuery(
						"select new study.querydsl.dto.MemberDto(m.username, m.age) from Member m",
						MemberDto.class)
				.getResultList();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	// querydsl에서의 DTO 조회
	// 프로퍼티 접근 방법(setter)
	// 기본 생성자 필요
	@Test
	public void findDtoBySetter() {
		List<MemberDto> result = queryFactory
				.select(Projections.bean(MemberDto.class,
						member.username, member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	// querydsl에서의 DTO 조회
	// 필드 접근 방법(setter)
	// 기본 생성자 필요
	@Test
	public void findDtoByField() {
		List<MemberDto> result = queryFactory
				.select(Projections.fields(MemberDto.class,
						member.username, member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	// querydsl에서의 DTO 조회
	// 생성자 방법
	// 사용하고자하는 생성자가 정의되어있어야함.
	@Test
	public void findDtoByConstructor() {
		List<MemberDto> result = queryFactory
				.select(Projections.constructor(MemberDto.class,
						member.username, member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	@Test
	public void findUserDtoByFields() {
		QMember memberSub = new QMember("memberSub");
		// 매핑하고자하는 필드명이 다를 경우, 처리를 해줘야함.
		// username <-> name
		// 서브쿼리의 결과 <-> age
		List<UserDto> result = queryFactory
				.select(Projections.fields(UserDto.class,
						member.username.as("name"),
						ExpressionUtils.as(JPAExpressions
								.select(memberSub.age.max())
								.from(memberSub), "age")))
				.from(member)
				.fetch();

		for (UserDto userDto : result) {
			System.out.println("userDto = " + userDto);
		}
	}

	@Test
	public void findUserDtoByConstructor() {
		List<UserDto> result = queryFactory
				.select(Projections.constructor(UserDto.class,
						member.username, member.age))
				.from(member)
				.fetch();

		for (UserDto userDto : result) {
			System.out.println("userDto = " + userDto);
		}
	}

	// @QueryProjection <= Dto 생성자를 Q타입으로 생성해야함
	// 컴파일 시점에 인자 오류를 확인할 수 있음
	// DTO가 querydsl에 의존성을 갖게 된다. DTO는 여러 계층에 돌아다닌다. 즉 여러 계층이 querydsl에 의존하게 된다.
	@Test
	public void findDtoByQueryProjection() {
		List<MemberDto> result = queryFactory
				.select(new QMemberDto(member.username, member.age))
				.from(member)
				.fetch();

		for (MemberDto memberDto : result) {
			System.out.println("memberDto = " + memberDto);
		}
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////   동 적 쿼 리    /////////////////////////////////////////
	//////////////////////////////////////////////////////////////////////////////////////////////

	// 동적 쿼리
	// BooleanBuilder
	@Test
	public void dynamicQuery_BooleanBuilder() {
		String usernameParam = "member1";
		Integer ageParam = null;

		List<Member> result = searchMember1(usernameParam, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}

	// BooleanBuilder
	private List<Member> searchMember1(String usernameCond, Integer ageCond) {
		BooleanBuilder builder = new BooleanBuilder();

		if (usernameCond != null) {
			builder.and(member.username.eq(usernameCond));
		}

		if (ageCond != null) {
			builder.and(member.age.eq(ageCond));
		}

		return queryFactory
				.selectFrom(member)
				.where(builder) // and, or로 조합 가능
				.fetch();
	}

	// 동적 쿼리
	// ** WhereParam **
	@Test
	public void dynamicQuery_WhereParam() {
		String usernameParam = "member1";
		Integer ageParam = null;

		List<Member> result = searchMember2(usernameParam, ageParam);
		assertThat(result.size()).isEqualTo(1);
	}

	// WhereParam
	private List<Member> searchMember2(String usernameCond, Integer ageCond) {
		return queryFactory
				.selectFrom(member)
//				.where(allEq(usernameCond, ageCond))
				.where(usernameEq(usernameCond), ageEq(ageCond))
				.fetch();
	}

	private BooleanExpression usernameEq(String usernameCond) {
		if (usernameCond == null) {
			return null; // 무시
		} else {
			return member.username.eq(usernameCond); // 값이 있을 때 조건
		}
	}

	private BooleanExpression ageEq(Integer ageCond) {
		if (ageCond == null) {
			return null;
		} else {
			return member.age.eq(ageCond);
		}
	}

	private Predicate allEq(String usernameCond, Integer ageCond) {
		return usernameEq(usernameCond).and(ageEq(ageCond));
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////   벌 크 연 산    /////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////

	@Test
//	@Commit
	public void bulkUpdate() {
		// 영 속 성 컨 텍 스 트     <->      D B
		// member1 = member1, 10 <-> member1 = member1, 10
		// member2 = member2, 20 <-> member2 = member2, 20
		// member3 = member3, 30 <-> member3 = member3, 30
		// member4 = member4, 40 <-> member4 = member4, 40

		long count = queryFactory
				.update(member)
				.set(member.username, "비회원")
				.where(member.age.lt(28))
				.execute();

		// 영 속 성 컨 텍 스 트     <->      D B
		// member1 = member1, 10 <-> member1 = 비회원, 10
		// member2 = member2, 20 <-> member2 = 비회원, 20
		// member3 = member3, 30 <-> member3 = member3, 30
		// member4 = member4, 40 <-> member4 = member4, 40

		// 벌크 연산은 영속성 컨텍스트를 거치지 않고 바로 DB에 영향을 준다.
		// DB에서 데이터를 조회해도 해당하는 데이터가 영속성 컨텍스트에 이미 존재하면, DB에서 가져온 데이터를 그냥 무시한다.
		List<Member> result = queryFactory
				.selectFrom(member)
				.fetch();

		for (Member member1 : result) {
			System.out.println("member1 = " + member1);
//			member1 = Member(id=3, username=member1, age=10)
//			member1 = Member(id=4, username=member2, age=20)
//			member1 = Member(id=5, username=member3, age=30)
//			member1 = Member(id=6, username=member4, age=40)
		}

		// 벌크 연산 후, 다시 데이터를 조회할 때는 영속성 컨텍스트를 초기화 해버리자.
		em.flush();
		em.clear();

		List<Member> result2 = queryFactory
				.selectFrom(member)
				.fetch();

		for (Member member1 : result2) {
			System.out.println("member1 = " + member1);
//			member1 = Member(id=3, username=비회원, age=10)
//			member1 = Member(id=4, username=비회원, age=20)
//			member1 = Member(id=5, username=member3, age=30)
//			member1 = Member(id=6, username=member4, age=40)
		}
	}

	@Test
	public void bulkAdd() {
		long count = queryFactory
				.update(member)
//				.set(member.age, member.age.add(1))
//				.set(member.age, member.age.add(-1))
//				.set(member.age, member.age.multiply(2))
				.set(member.age, member.age.divide(2))
				.execute();

		em.flush();
		em.clear();

		List<Member> result = queryFactory
				.selectFrom(member)
				.fetch();

		for (Member member1 : result) {
			System.out.println("member1 = " + member1);
		}
	}

	@Test
	public void bulkDelete() {
		long count = queryFactory
				.delete(member)
				.where(member.age.gt(18))
				.execute();
	}

	///////////////////////////////////////////////////////////////////////////////////////////////
	/////////////////////////////////////   SQL Function   ////////////////////////////////////////
	///////////////////////////////////////////////////////////////////////////////////////////////

	@Test
	public void sqlFunction() {
		List<String> result = queryFactory
				.select(
						Expressions.stringTemplate("function('replace', {0}, {1}, {2})",
								member.username, "member", "M"))
				.from(member)
				.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}

	@Test
	public void sqlFunction2() {
		List<String> result = queryFactory
				.select(member.username)
				.from(member)
//				.where(member.username.eq(Expressions.stringTemplate("function('lower', {0})",
//						member.username))) // dialect에 등록된 DB 함수를 호출
				.where(member.username.eq(member.username.lower())) // ansi 표준 함수는 querydsl에 내장되어 있다.
				.fetch();

		for (String s : result) {
			System.out.println("s = " + s);
		}
	}
}
