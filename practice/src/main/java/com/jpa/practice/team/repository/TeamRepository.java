package com.jpa.practice.team.repository;

import com.jpa.practice.team.domain.TeamForEager;
import com.jpa.practice.team.domain.TeamForLazy;
import jakarta.persistence.EntityManager;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@RequiredArgsConstructor
@Repository
public class TeamRepository {

	private final EntityManager em;

	public void saveTE(TeamForEager team) {
		em.persist(team);
	}

	public void saveTL(TeamForLazy team) {
		em.persist(team);
	}

	public List<TeamForLazy> teamsWithFetchJoin() {
		return em.createQuery("select t from TeamForLazy t"
				+ " join fetch t.members", TeamForLazy.class)
				.getResultList();
	}

	public List<TeamForLazy> teamsWithFetchJoinDistinct() {
		return em.createQuery("select distinct t from TeamForLazy t"
						+ " join fetch t.members", TeamForLazy.class)
				.getResultList();
	}
}
